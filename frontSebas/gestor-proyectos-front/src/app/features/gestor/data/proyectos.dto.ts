// JSON de GET /api/equipo/proyectos

export interface ProyectoResumenDto {
  id: number;
  codigo: string;
  nombre: string;
  estado: string;
  clienteNombre: string;
}
