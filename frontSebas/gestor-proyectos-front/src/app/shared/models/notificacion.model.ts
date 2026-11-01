export type NotificacionTipo = 'proyecto' | 'estado' | 'usuario' | 'sistema';

export interface Notificacion {
  id: number;
  tipo: NotificacionTipo;
  titulo: string;
  descripcion: string;
  fechaRelativa: string;
  leida: boolean;
}
