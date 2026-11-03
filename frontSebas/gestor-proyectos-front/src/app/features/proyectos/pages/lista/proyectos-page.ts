import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { IconoProyecto } from '../../components/icono-proyecto/icono-proyecto';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { MODULOS, TIPOS_MODULO } from '../../../../shared/utils/proyectos';
import { ProyectosDetalleService } from '../../data/proyectos-detalle.service';
import { ProyectoTarjeta } from '../../models/proyectos.models';

type FiltroEstado = 'activos' | 'EN_PAUSA' | 'TERMINADO' | 'CANCELADO' | 'todos';

/** "Activos" son los que se están trabajando o por empezar. */
const ACTIVOS = ['EN_CURSO', 'PLANEACION'];

/** Proyectos (PDF pág. 13). Solo los proyectos donde soy miembro (todos si soy Admin). */
@Component({
  selector: 'app-proyectos-page',
  imports: [RouterLink, Avatar, IconoProyecto, BarraAvance, ChipModulo, EtiquetaEstado],
  templateUrl: './proyectos-page.html',
  styleUrl: './proyectos-page.scss',
})
export class ProyectosPage {
  private readonly service = inject(ProyectosDetalleService);

  protected readonly proyectos = signal<ProyectoTarjeta[]>([]);
  protected readonly puedeCrear = signal(false);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);

  protected readonly busqueda = signal('');
  protected readonly estado = signal<FiltroEstado>('activos');
  protected readonly cliente = signal('');
  protected readonly modulo = signal('');
  protected readonly prioridad = signal('');

  protected readonly modulosCatalogo = TIPOS_MODULO.map((tipo) => ({ tipo, nombre: MODULOS[tipo].largo }));

  /** "4 activos · 1 en pausa" */
  protected readonly cuentas = computed(() => {
    const lista = this.proyectos();
    const activos = lista.filter((p) => ACTIVOS.includes(p.estado)).length;
    const enPausa = lista.filter((p) => p.estado === 'EN_PAUSA').length;
    return [
      `${activos} ${activos === 1 ? 'activo' : 'activos'}`,
      ...(enPausa ? [`${enPausa} en pausa`] : []),
    ].join(' · ');
  });

  protected readonly clientes = computed(() => {
    const vistos = new Map(this.proyectos().map((p) => [p.clienteId, p.clienteNombre]));
    return [...vistos].map(([id, nombre]) => ({ id: String(id), nombre })).sort((a, b) => a.nombre.localeCompare(b.nombre));
  });

  protected readonly visibles = computed(() => {
    const texto = normalizar(this.busqueda());
    const estado = this.estado();
    return this.proyectos().filter(
      (p) =>
        (estado === 'todos' || (estado === 'activos' ? ACTIVOS.includes(p.estado) : p.estado === estado)) &&
        (!this.cliente() || String(p.clienteId) === this.cliente()) &&
        (!this.modulo() || p.modulos.some((m) => m.tipo === this.modulo())) &&
        (!this.prioridad() || p.prioridad === this.prioridad()) &&
        (!texto || normalizar(p.nombre).includes(texto) || normalizar(p.clienteNombre).includes(texto) ||
          p.codigo.toLowerCase().includes(texto)),
    );
  });

  constructor() {
    this.service.listar().subscribe({
      next: ({ proyectos, puedeCrear }) => {
        this.proyectos.set(proyectos);
        this.puedeCrear.set(puedeCrear);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  protected plural(n: number, uno: string, varios: string): string {
    return `${n} ${n === 1 ? uno : varios}`;
  }
}

function normalizar(texto: string): string {
  return texto.trim().toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
}
