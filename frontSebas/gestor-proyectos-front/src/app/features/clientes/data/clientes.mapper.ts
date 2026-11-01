import { tonoDeAvatar } from '../../../shared/utils/roles';
import { Cliente, DatosCliente, ListadoClientes } from '../models/clientes.models';
import { ClienteDto, ClienteRequestDto, ListadoClientesDto } from './clientes.dto';

export function aListado(dto: ListadoClientesDto): ListadoClientes {
  return { clientes: dto.clientes.map(aCliente), puedeGestionar: dto.puedeGestionar };
}

export function aCliente(dto: ClienteDto): Cliente {
  return {
    id: dto.id,
    razonSocial: dto.razonSocial,
    nombreCorto: dto.nombreCorto,
    iniciales: dto.iniciales,
    rfc: dto.rfc ?? '',
    giro: dto.giro ?? '',
    ciudad: dto.ciudad ?? '',
    contactoNombre: dto.contactoNombre ?? '',
    contactoCorreo: dto.contactoCorreo ?? '',
    contactoTelefono: dto.contactoTelefono ?? '',
    notas: dto.notas ?? '',
    activo: dto.activo,
    proyectos: dto.proyectos,
    tono: tonoDeAvatar(dto.id, dto.activo),
  };
}

export function aRequest(datos: DatosCliente): ClienteRequestDto {
  const texto = (valor: string) => valor.trim() || null;
  return {
    razonSocial: datos.razonSocial.trim(),
    nombreCorto: texto(datos.nombreCorto),
    rfc: texto(datos.rfc),
    giro: datos.giro || null,
    ciudad: texto(datos.ciudad),
    contactoNombre: texto(datos.contactoNombre),
    contactoCorreo: texto(datos.contactoCorreo),
    contactoTelefono: texto(datos.contactoTelefono),
    notas: texto(datos.notas),
    activo: datos.activo,
  };
}
