import { HttpErrorResponse } from '@angular/common/http';

/** El mensaje que manda el back (ErrorResponse.mensaje), o uno genérico si no hubo respuesta. */
export function mensajeDeError(error: unknown, generico: string): string {
  if (error instanceof HttpErrorResponse && typeof error.error?.mensaje === 'string') {
    return error.error.mensaje;
  }
  return generico;
}
