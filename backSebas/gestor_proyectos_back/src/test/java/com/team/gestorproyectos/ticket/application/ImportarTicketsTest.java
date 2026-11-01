package com.team.gestorproyectos.ticket.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.FaseDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.HistoriaDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.ModuloDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.PersonaDeEquipo;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.TicketAbierto;
import com.team.gestorproyectos.ticket.domain.model.TicketEditable;
import com.team.gestorproyectos.ticket.domain.model.TicketImportado;
import com.team.gestorproyectos.ticket.domain.model.TipoTicket;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort;

class ImportarTicketsTest {

    private static final Long PROYECTO = 1L;
    private static final Long HISTORIA = 20L;
    private static final Long MARCO = 8L;
    private static final Long API = 100L;
    private static final Long WEB = 101L;

    private final TicketsEnMemoria tickets = new TicketsEnMemoria(Set.of());
    private final CrearTicketService service = new CrearTicketService(tickets, (usuario, permiso) -> true);

    @Test
    void creaActividadesConElResponsableYElRepositorioPorTipoONombre() {
        int creados = service.importar(PROYECTO, 1L, false, HISTORIA, MARCO, List.of(
                new TicketImportado("Endpoint de alta", "POST /escuelas", "api",
                        List.of("Crear tabla escuela con clave única", " ", "EscuelaController y EscuelaBusiness")),
                new TicketImportado("Pantalla de alta", null, "escolar-web", null)));

        assertThat(creados).isEqualTo(2);
        assertThat(tickets.creados).extracting(NuevoTicket::moduloId).containsExactly(API, WEB);
        assertThat(tickets.creados).allSatisfy(t -> {
            assertThat(t.tipo()).isEqualTo(TipoTicket.ACTV);
            assertThat(t.historiaId()).isEqualTo(HISTORIA);
            assertThat(t.responsableId()).isEqualTo(MARCO);
        });
        assertThat(tickets.numeros).containsExactly(41, 42);
        // Las subtareas vacías se descartan; las demás quedan en orden.
        assertThat(tickets.creados.get(0).subtareas())
                .containsExactly("Crear tabla escuela con clave única", "EscuelaController y EscuelaBusiness");
        assertThat(tickets.creados.get(1).subtareas()).isEmpty();
    }

    @Test
    void conUnSoloRepositorioPuedeFaltar() {
        TicketsEnMemoria conUno = new TicketsEnMemoria(Set.of(API));
        new CrearTicketService(conUno, (usuario, permiso) -> true)
                .importar(PROYECTO, 1L, false, HISTORIA, MARCO, List.of(new TicketImportado("Algo", null, null, null)));
        assertThat(conUno.creados).extracting(NuevoTicket::moduloId).containsExactly(API);
    }

    @Test
    void siUnoFallaNoCreaNingunoYDiceCual() {
        assertThatThrownBy(() -> service.importar(PROYECTO, 1L, false, HISTORIA, MARCO, List.of(
                new TicketImportado("Bien", null, "API", null),
                new TicketImportado("Sin repositorio", null, null, null))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("Ticket 2: falta \"repositorio\" (API, WEB)");
        assertThatThrownBy(() -> service.importar(PROYECTO, 1L, false, HISTORIA, MARCO, List.of(
                new TicketImportado("Otro", null, "Movil", null))))
                .hasMessage("Ticket 1: el repositorio \"Movil\" no es de la historia (API, WEB)");
        assertThat(tickets.creados).isEmpty();
    }

    @Test
    void elResponsableDebeSerDesarrollador() {
        assertThatThrownBy(() -> service.importar(PROYECTO, 1L, false, HISTORIA, 7L, List.of(
                new TicketImportado("Algo", null, "API", null))))
                .hasMessageContaining("Desarrollador");
    }

    private static final class TicketsEnMemoria implements TicketEscrituraPort {

        final List<NuevoTicket> creados = new ArrayList<>();
        final List<Integer> numeros = new ArrayList<>();
        private final Set<Long> modulosDeHistoria;

        TicketsEnMemoria(Set<Long> modulosDeHistoria) {
            this.modulosDeHistoria = modulosDeHistoria;
        }

        @Override
        public Optional<ProyectoDeTicket> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin) {
            return Optional.of(new ProyectoDeTicket(PROYECTO, "Sistema Escolar", 1L));
        }

        @Override
        public List<FaseDeTicket> fasesConHistorias(Long proyectoId) {
            return List.of(new FaseDeTicket(10L, 1, "Primera versión",
                    List.of(new ModuloDeTicket(API, "API", "escolar-api", "Java", null, "develop"),
                            new ModuloDeTicket(WEB, "WEB", "escolar-web", "Angular", null, "develop")),
                    List.of(new HistoriaDeTicket(HISTORIA, 1, "Registrar escuela", "LISTA", "Admin", "dar de alta",
                            "usar el sistema", "Alma", modulosDeHistoria, 0, List.of()))));
        }

        @Override
        public List<PersonaDeEquipo> equipo(Long proyectoId) {
            return List.of(new PersonaDeEquipo(MARCO, "Marco", List.of("DESARROLLADOR")),
                    new PersonaDeEquipo(7L, "Alma", List.of("ANALISTA")));
        }

        @Override
        public Map<TipoTicket, Integer> siguientesNumeros(Long proyectoId) {
            return Map.of(TipoTicket.ACTV, 41);
        }

        @Override
        public List<TicketAbierto> abiertos(Long proyectoId) {
            return List.of();
        }

        @Override
        public Long crear(Long proyectoId, int numero, NuevoTicket nuevo, Long usuarioId) {
            creados.add(nuevo);
            numeros.add(numero);
            return (long) creados.size();
        }

        @Override
        public Optional<TicketEditable> editable(Long proyectoId, Long ticketId) {
            return Optional.empty();
        }

        @Override
        public void actualizar(Long ticketId, NuevoTicket datos) {
        }
    }
}
