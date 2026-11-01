/**
 * AVANZAR = la acción principal (Iniciar, Enviar a QA, Aprobar QA...). RECHAZAR = quien revisa lo regresa.
 * BLOQUEAR, RETIRAR (de QA o revisión) y CANCELAR viven en el detalle del ticket (PDF pág. 22);
 * NO_REPRODUCIBLE y REABRIR, en el de un bug. PUBLICAR_PARCHE va por su propio endpoint (PDF pág. 23).
 */
export type TipoMovimiento =
  | 'AVANZAR'
  | 'RECHAZAR'
  | 'BLOQUEAR'
  | 'RETIRAR'
  | 'CANCELAR'
  | 'NO_REPRODUCIBLE'
  | 'REABRIR'
  | 'PUBLICAR_PARCHE';

export interface Movimiento {
  ticketId: number;
  tipo: TipoMovimiento;
  /** El estado que se ve en pantalla. Si alguien más ya lo movió, el back lo rechaza. */
  estadoEsperado: string;
  motivo?: string;
  /** Al bloquear: el ticket que lo detiene (se desbloquea solo cuando ese se termina) */
  bloqueadoPorId?: number;
  /** Al bloquear: quién puede destrabarlo */
  destrabadorId?: number;
  /** Al confirmar un bug: CRITICA, ALTA, MEDIA o BAJA */
  severidad?: string;
  /** Al confirmar un bug o aprobar una mejora: el Desarrollador que lo hará */
  responsableId?: number;
}

export interface ResultadoMovimiento {
  accion: string;
  estado: string;
  /** La rama que se creó al iniciar el trabajo */
  rama: string | null;
}
