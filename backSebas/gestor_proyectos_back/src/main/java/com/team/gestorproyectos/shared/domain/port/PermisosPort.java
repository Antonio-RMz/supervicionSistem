package com.team.gestorproyectos.shared.domain.port;

/**
 * Si alguien tiene un permiso que no depende de un proyecto (roles + permisos extra en "Todos los proyectos").
 * Lo implementa el módulo de usuarios; lo usan los demás módulos.
 */
public interface PermisosPort {

    String GESTIONAR_CLIENTES = "cliente.gestionar";
    String CREAR_PROYECTOS = "proyecto.crear";
    String EDITAR_PROYECTOS = "proyecto.editar";
    String ARMAR_EQUIPO = "proyecto.equipo";
    String CREAR_FASES = "fase.crear";
    String GESTIONAR_FASES = "fase.gestionar";
    String REABRIR_FASES = "fase.reabrir";
    String MARCAR_CRITERIOS = "fase.marcar_criterio";
    String CREAR_HISTORIAS = "historia.crear";
    String MARCAR_HISTORIA_LISTA = "historia.marcar_lista";
    String INICIAR_HISTORIAS = "historia.iniciar";
    String ACEPTAR_HISTORIAS = "historia.aceptar";
    String DESCARTAR_HISTORIAS = "historia.descartar";
    String INTEGRAR_HISTORIAS = "historia.integrar";
    String CREAR_TICKETS = "ticket.crear";
    String APROBAR_QA = "ticket.aprobar_qa";
    String PUBLICAR_RELEASE = "release.publicar";
    String REVERTIR_RELEASE = "release.revertir";
    String PUBLICAR_PARCHES = "parche.publicar";
    String REPORTAR_BUGS = "bug.reportar";

    boolean puede(Long usuarioId, String permisoCodigo);
}
