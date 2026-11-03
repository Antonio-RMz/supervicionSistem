package com.team.gestorproyectos.historia.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.historia.domain.model.Analista;
import com.team.gestorproyectos.historia.domain.model.FaseParaHistoria;
import com.team.gestorproyectos.historia.domain.model.FaseParaHistoria.Opcion;
import com.team.gestorproyectos.historia.domain.model.HistoriaGuardada;
import com.team.gestorproyectos.historia.domain.model.HistoriaImportada;
import com.team.gestorproyectos.historia.domain.model.NuevaHistoria;
import com.team.gestorproyectos.historia.domain.model.PreparacionHistoria.UsuarioDelSistema;
import com.team.gestorproyectos.historia.domain.model.ResultadoImportacion;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaRepositoryPort;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.AvisosPort;

class ImportarHistoriasTest {

    private static final Long PROYECTO = 1L;
    private static final Long FASE = 10L;
    private static final Long ALMA = 7L;

    private final HistoriasEnMemoria historias = new HistoriasEnMemoria();
    private final AvisosEnMemoria avisos = new AvisosEnMemoria();
    private final CrearHistoriaService service = new CrearHistoriaService(historias, (usuario, permiso) -> true, avisos);

    @Test
    void creaTodasEnBorradorConLosRepositoriosDeLaFase() {
        ResultadoImportacion resultado = service.importar(PROYECTO, 1L, false, FASE, ALMA, List.of(
                new HistoriaImportada("Registrar escuela", "Administrador", "dar de alta una escuela",
                        "empezar a usar el sistema", "Alta", 3, List.of("Capturo nombre", "Clave repetida avisa")),
                new HistoriaImportada("Registrar periodo", "Escuela", "crear periodos", null, null, null, null)));

        assertThat(resultado.creadas()).isEqualTo(2);
        assertThat(resultado.incompletas()).isEqualTo(1);
        assertThat(historias.creadas).allSatisfy(h -> {
            assertThat(h.borrador()).isTrue();
            assertThat(h.faseId()).isEqualTo(FASE);
            assertThat(h.analistaId()).isEqualTo(ALMA);
            assertThat(h.moduloIds()).containsExactlyInAnyOrder(100L, 101L);
        });
        assertThat(historias.creadas.get(0).prioridad()).isEqualTo("ALTA");
        // Un solo aviso al responsable por todas las importadas, no uno por historia.
        assertThat(avisos.importadas).containsExactly("1 → 7: 2 historias");
    }

    @Test
    void siUnaFallaNoCreaNingunaYDiceCual() {
        assertThatThrownBy(() -> service.importar(PROYECTO, 1L, false, FASE, ALMA, List.of(
                new HistoriaImportada("Bien", "Escuela", null, null, null, null, null),
                new HistoriaImportada("Sin como", " ", null, null, null, null, null))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("Historia 2: falta \"como\"");
        assertThat(historias.creadas).isEmpty();
        assertThat(avisos.importadas).isEmpty();
    }

    @Test
    void rechazaValoresFueraDeLaEscala() {
        assertThatThrownBy(() -> service.importar(PROYECTO, 1L, false, FASE, ALMA, List.of(
                new HistoriaImportada("Puntos raros", "Escuela", null, null, null, 4, null))))
                .hasMessageStartingWith("Historia 1: La estimación");
    }

    @Test
    void elAnalistaDebeSerDelEquipo() {
        assertThatThrownBy(() -> service.importar(PROYECTO, 1L, false, FASE, 99L, List.of(
                new HistoriaImportada("Algo", "Escuela", null, null, null, null, null))))
                .hasMessageContaining("Analista o un Líder");
    }

    private static final class HistoriasEnMemoria implements HistoriaRepositoryPort {

        final List<NuevaHistoria> creadas = new ArrayList<>();

        @Override
        public Optional<String> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin) {
            return Optional.of("Sistema Escolar");
        }

        @Override
        public int siguienteNumero(Long proyectoId) {
            return creadas.size() + 1;
        }

        @Override
        public List<FaseParaHistoria> fasesAbiertas(Long proyectoId) {
            return List.of(new FaseParaHistoria(FASE, 1, "Primera versión", "PLANEADA",
                    List.of(new Opcion(100L, "API", "api"), new Opcion(101L, "WEB", "web"))));
        }

        @Override
        public List<Analista> equipo(Long proyectoId) {
            return List.of(new Analista(ALMA, "Alma", List.of("ANALISTA")), new Analista(8L, "Marco", List.of("DESARROLLADOR")));
        }

        @Override
        public List<UsuarioDelSistema> usuariosDelSistema(Long proyectoId) {
            return List.of();
        }

        @Override
        public Long crear(Long proyectoId, int numero, NuevaHistoria nueva, Long usuarioId) {
            creadas.add(nueva);
            return (long) creadas.size();
        }

        @Override
        public Optional<HistoriaGuardada> guardada(Long proyectoId, Long historiaId) {
            return Optional.empty();
        }

        @Override
        public void actualizar(Long historiaId, NuevaHistoria cambios, HistoriaGuardada antes, Long usuarioId) {
        }
    }

    /** Guarda los avisos de historias importadas como "actor → destinatario: N historias". */
    private static final class AvisosEnMemoria implements AvisosPort {

        final List<String> importadas = new ArrayList<>();

        @Override
        public void avisar(Tipo tipo, Long ticketId, Long actorId, Collection<Long> destinatarios) {
        }

        @Override
        public void historiaAsignada(Long historiaId, Papel papel, Long actorId, Collection<Long> destinatarios) {
        }

        @Override
        public void historiasAsignadas(List<Long> historiaIds, Long actorId, Long destinatario) {
            importadas.add(actorId + " → " + destinatario + ": " + historiaIds.size() + " historias");
        }
    }
}
