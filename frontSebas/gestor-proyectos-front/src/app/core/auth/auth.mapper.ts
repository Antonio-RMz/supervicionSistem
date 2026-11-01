import { LoginResponseDto, UsuarioDto } from './auth.dto';
import { Sesion, TipoUsuario, Usuario } from './auth.models';

/** DTO del back -> modelo de la app. */
export function aSesion(dto: LoginResponseDto): Sesion {
  return { token: dto.token, usuario: aUsuario(dto.usuario) };
}

function aUsuario(dto: UsuarioDto): Usuario {
  return {
    id: dto.id,
    nombre: dto.nombre,
    nombreCompleto: dto.nombreCompleto,
    iniciales: dto.iniciales,
    correo: dto.correo,
    tipo: dto.tipo as TipoUsuario,
    clienteId: dto.clienteId,
    clienteNombre: dto.clienteNombre,
    cambiarPassword: dto.cambiarPassword,
    roles: dto.roles ?? [],
  };
}
