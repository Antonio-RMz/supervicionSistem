// Modelos que usa la app. Son nuestros: si el back cambia un nombre, solo se toca el mapper.

export type TipoUsuario = 'EQUIPO' | 'CLIENTE';

export interface Usuario {
  id: number;
  nombre: string;
  nombreCompleto: string;
  iniciales: string;
  correo: string;
  tipo: TipoUsuario;
  clienteId: number | null;
  clienteNombre: string | null;
  cambiarPassword: boolean;
  roles: string[];
}

export interface Sesion {
  token: string;
  usuario: Usuario;
}
