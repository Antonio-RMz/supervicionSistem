/** Un ticket del JSON que devuelve la IA: lo necesario para crearlo y sus subtareas. */
export interface TicketImportado {
  titulo: string;
  descripcion: string | null;
  /** El tipo ("API", "Web") o el nombre del repositorio; null si la historia tiene uno solo. */
  repositorio: string | null;
  /** El trabajo técnico concreto, paso por paso: lo que de verdad hará el Desarrollador. */
  subtareas: string[];
}

/** Cómo quedó el JSON pegado: los tickets que trae o por qué no se puede ejecutar. */
export type LecturaTickets = { valido: true; tickets: TicketImportado[] } | { valido: false; error: string };

/** Un repositorio de la historia, como se nombra en el JSON. */
export interface RepositorioDeHistoria {
  /** "API", "Web" */
  corto: string;
  tipo: string;
  nombre: string;
  tecnologia: string;
}

/** Qué campos lleva el JSON de tickets que se copia. El título siempre va. */
export interface CamposTickets {
  ids: boolean;
  estado: boolean;
  repositorio: boolean;
  responsable: boolean;
}
