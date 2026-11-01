export interface UsuarioResponse {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: string;
  activo: boolean;
  uuidSesion: string;
  fechaInicioSesion: string;
  fechaExpiracionToken: string;
}
