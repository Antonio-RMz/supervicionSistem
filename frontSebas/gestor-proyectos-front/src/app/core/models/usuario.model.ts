export interface RolItem {
  id: number;
  nombreRol: string;
  DescripcionRol: string;
}

export interface UsuarioListItem {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  fechaRegistro: string;
  nombreRol: string;
  passTemporal: string | null;
}

export interface UsuarioRequest {
  nombre: string;
  apellido: string;
  email: string;
  idRol: number;
}
