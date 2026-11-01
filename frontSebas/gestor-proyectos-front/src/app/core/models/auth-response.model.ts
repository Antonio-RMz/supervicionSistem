export interface ModuloBackend {
  ref: string;
  tipo: string;
  requiereProyecto: boolean;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  expiracion: number;
  modulos?: ModuloBackend[];
}
