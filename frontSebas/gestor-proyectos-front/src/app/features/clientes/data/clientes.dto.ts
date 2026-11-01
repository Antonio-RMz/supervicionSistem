// JSON de /api/equipo/clientes (ver ClientesDtos.java).

export interface ClienteDto {
  id: number;
  razonSocial: string;
  nombreCorto: string;
  iniciales: string;
  rfc: string | null;
  giro: string | null;
  ciudad: string | null;
  contactoNombre: string | null;
  contactoCorreo: string | null;
  contactoTelefono: string | null;
  notas: string | null;
  activo: boolean;
  proyectos: { id: number; nombre: string }[];
}

export interface ListadoClientesDto {
  clientes: ClienteDto[];
  puedeGestionar: boolean;
}

export interface ClienteRequestDto {
  razonSocial: string;
  nombreCorto: string | null;
  rfc: string | null;
  giro: string | null;
  ciudad: string | null;
  contactoNombre: string | null;
  contactoCorreo: string | null;
  contactoTelefono: string | null;
  notas: string | null;
  activo: boolean;
}
