export interface ClienteRequest {
  nombre: string;
  empresa: string;
  email: string;
  telefono: string;
  estado: boolean;
}

export interface ClienteResponse {
  id: number;
  nombre: string;
  empresa: string;
  email: string;
  telefono: string;
  estado: boolean;
  createdAt: string;
  updatedAt: string;
}
