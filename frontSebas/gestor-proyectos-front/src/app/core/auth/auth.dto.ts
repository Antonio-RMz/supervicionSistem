// DTOs: la forma exacta del JSON que manda y recibe el back (/api/auth).

export interface LoginRequestDto {
  correo: string;
  password: string;
}

export interface UsuarioDto {
  id: number;
  nombre: string;
  nombreCompleto: string;
  iniciales: string;
  correo: string;
  tipo: string;
  clienteId: number | null;
  clienteNombre: string | null;
  cambiarPassword: boolean;
  roles: string[] | null;
}

export interface LoginResponseDto {
  token: string;
  usuario: UsuarioDto;
}
