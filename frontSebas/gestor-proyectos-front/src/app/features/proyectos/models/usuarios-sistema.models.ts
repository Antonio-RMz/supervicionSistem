/** Alguien que usa el software que se construye (Administrador, Escuela, Alumno...), no alguien del equipo. */
export interface UsuarioDelSistema {
  nombre: string;
  /** Qué hace en el sistema; ayuda a la IA a proponer historias. */
  descripcion: string;
}

export interface UsuariosDelSistema {
  usuarios: UsuarioDelSistema[];
  /** El Líder, un Admin o quien crea historias. */
  puedeEditar: boolean;
}
