package com.team.gestorproyectos.historia.domain.model;

import java.util.List;

import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Ambiente;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.HistoriaDeFase;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.ModuloConTickets;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Revision;

/**
 * La vista Integrar historia (PDF pág. 28) para quien la ve.
 *
 * @param rol            "Líder" o "Admin" si puede integrarla; null si solo la ve
 * @param impedimentos   lo que falta para poder integrar; vacío si ya se puede
 * @param prefijosRama   "feature/", "fix/" de las ramas que se mezclan
 * @param prsPorIntegrar cuántos PR registrados entran a develop
 */
public record IntegracionHistoria(
        Long historiaId,
        Long proyectoId,
        String proyectoNombre,
        Long faseId,
        int faseNumero,
        String version,
        String codigo,
        String titulo,
        String estado,
        String analistaNombre,
        String rol,
        List<String> impedimentos,
        List<String> prefijosRama,
        int prsPorIntegrar,
        int ticketsConCodigo,
        List<ModuloConTickets> modulos,
        List<Revision> revision,
        List<Ambiente> ambientes,
        List<HistoriaDeFase> historiasDeFase,
        List<String> pasos) {
}
