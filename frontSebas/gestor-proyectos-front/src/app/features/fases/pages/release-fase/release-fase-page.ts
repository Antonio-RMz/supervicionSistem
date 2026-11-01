import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { Etiqueta } from '../../../../shared/models/etiqueta.model';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ConfirmacionFaseModal } from '../../components/confirmacion-fase/confirmacion-fase-modal';
import { FaseDetalleService } from '../../data/fase-detalle.service';
import {
  AccionConConfirmacion,
  AccionDisponible,
  AccionFase,
  Criterio,
  DatosAccion,
  DetalleFase,
  ReleaseFase,
} from '../../models/fase-detalle.models';

/** Cómo se ve el release según el estado de la fase. */
const ETIQUETAS: Record<string, Etiqueta> = {
  PLANEADA: { texto: 'En preparación', tono: 'gris' },
  EN_CURSO: { texto: 'En preparación', tono: 'morado' },
  PAUSADA: { texto: 'Fase pausada', tono: 'naranja' },
  LISTA_PARA_RELEASE: { texto: 'Lista para subir a main', tono: 'verde' },
  EN_PUBLICACION: { texto: 'En main · falta desplegar', tono: 'turquesa' },
  PUBLICADA: { texto: 'En producción', tono: 'verde' },
  REVERTIDA: { texto: 'Revertida', tono: 'rojo' },
  CANCELADA: { texto: 'Cancelada', tono: 'gris' },
};

interface Paso {
  titulo: string;
  detalle: string;
  avance: 'hecho' | 'actual' | 'pendiente';
}

/**
 * Release de la fase (PDF pág. 27): cerrar la fase es publicar. develop se sube a main por módulo,
 * se crea el tag y se despliega a producción. Lo hace el Líder; las notas se editan antes de publicar.
 */
@Component({
  selector: 'app-release-fase-page',
  imports: [RouterLink, ConfirmacionFaseModal, EtiquetaEstado, TipoTicket],
  templateUrl: './release-fase-page.html',
  styleUrl: './release-fase-page.scss',
})
export class ReleaseFasePage implements OnInit {
  private readonly service = inject(FaseDetalleService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  /** :id del proyecto */
  readonly id = input.required<string>();
  readonly faseId = input.required<string>();

  protected readonly fase = signal<DetalleFase | null>(null);
  protected readonly release = signal<ReleaseFase | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  protected readonly enviando = signal<string | null>(null);
  protected readonly confirmando = signal<AccionConConfirmacion | null>(null);
  /** El texto de las notas mientras se editan; null si no se están editando. */
  protected readonly editando = signal<string | null>(null);

  protected readonly etiqueta = computed(() => ETIQUETAS[this.fase()?.estado ?? ''] ?? { texto: '', tono: 'gris' });

  protected readonly aceptadas = computed(
    () => this.fase()?.historias.filter((h) => ['ACEPTADA', 'INTEGRADA', 'CERRADA'].includes(h.estado)).length ?? 0,
  );

  /** Los 5 pasos de arriba: criterios, historias, staging, merge a main y producción. */
  protected readonly pasos = computed<Paso[]>(() => {
    const f = this.fase();
    if (!f) return [];
    const enMain = ['EN_PUBLICACION', 'PUBLICADA'].includes(f.estado);
    const enProduccion = f.estado === 'PUBLICADA';
    const criteriosOk = f.criteriosCumplidos === f.criterios.length;
    const integradasOk = f.historiasQueCuentan > 0 && f.historiasIntegradas === f.historiasQueCuentan;
    const aceptadasOk = f.historiasQueCuentan > 0 && this.aceptadas() === f.historiasQueCuentan;
    const listas = criteriosOk && integradasOk && aceptadasOk;
    return [
      { titulo: 'Criterios de cierre', detalle: `${f.criteriosCumplidos} de ${f.criterios.length}`, avance: criteriosOk ? 'hecho' : 'actual' },
      { titulo: 'Historias integradas', detalle: `${f.historiasIntegradas} de ${f.historiasQueCuentan}`, avance: integradasOk ? 'hecho' : 'actual' },
      { titulo: 'Pruebas en staging', detalle: `${this.aceptadas()} aceptadas por el Analista`, avance: aceptadasOk ? 'hecho' : 'actual' },
      { titulo: 'Merge a main + tag', detalle: enMain ? 'hecho' : 'al subir a main', avance: enMain ? 'hecho' : listas ? 'actual' : 'pendiente' },
      { titulo: 'Producción', detalle: enProduccion ? 'publicada' : 'después del merge', avance: enProduccion ? 'hecho' : enMain ? 'actual' : 'pendiente' },
    ];
  });

  /** Subir a main o Desplegar (o Publicar de nuevo); Revertir va en el Plan de reversa. */
  protected readonly accionPrincipal = computed(() =>
    this.fase()?.acciones.find((a) => ['SUBIR_A_MAIN', 'DESPLEGAR', 'PUBLICAR_DE_NUEVO'].includes(a.accion)) ?? null,
  );
  protected readonly revertir = computed(() => this.fase()?.acciones.find((a) => a.accion === 'REVERTIR') ?? null);
  protected readonly esLider = computed(() => this.fase()?.rol !== null && this.fase()?.rol !== undefined);

  ngOnInit(): void {
    this.cargar(true);
  }

  protected presionar(boton: AccionDisponible): void {
    if (boton.impedimentos.length) return;
    if (boton.accion === 'SUBIR_A_MAIN' || boton.accion === 'REVERTIR') {
      this.confirmando.set(boton.accion);
    } else {
      this.ejecutar(boton.accion);
    }
  }

  protected ejecutar(accion: AccionFase, datos: DatosAccion = {}): void {
    const f = this.fase();
    if (!f) return;
    this.enviando.set(accion);
    this.aviso.set(null);
    this.service.ejecutar(f.proyectoId, f.id, accion, f.estado, datos).subscribe({
      next: (r) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        const bug = r.bugCreado ? ` Se creó ${r.bugCreado}.` : '';
        this.aviso.set({ texto: `${r.accion}: listo.${bug}`, error: false });
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos actualizar el release.'), error: true });
        this.cargar();
      },
    });
  }

  /** Un criterio manual, como el despliegue en staging: igual que en el detalle de la fase. */
  protected marcar(criterio: Criterio): void {
    const f = this.fase();
    if (!f) return;
    this.enviando.set(`criterio-${criterio.id}`);
    this.aviso.set(null);
    this.service.marcarCriterio(f.proyectoId, f.id, criterio.id).subscribe({
      next: (r) => {
        this.enviando.set(null);
        const lista = r.estadoNuevo === 'LISTA_PARA_RELEASE' ? ' Se cumplieron todos: la fase pasó a Lista para release.' : '';
        this.aviso.set({ texto: `"${criterio.descripcion}" quedó cumplido.${lista}`, error: false });
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos marcar el criterio.'), error: true });
        this.cargar();
      },
    });
  }

  protected guardarNotas(notas: string, enviarAlCliente: boolean): void {
    const f = this.fase();
    if (!f) return;
    this.enviando.set('notas');
    this.service.guardarNotas(f.proyectoId, f.id, notas, enviarAlCliente).subscribe({
      next: () => {
        this.enviando.set(null);
        this.editando.set(null);
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos guardar las notas.'), error: true });
      },
    });
  }

  private cargar(primeraVez = false): void {
    const proyectoId = Number(this.id());
    const faseId = Number(this.faseId());
    forkJoin([this.service.detalle(proyectoId, faseId), this.service.release(proyectoId, faseId)]).subscribe({
      next: ([fase, release]) => {
        this.fase.set(fase);
        this.release.set(release);
        this.cargando.set(false);
        if (primeraVez) this.abrirAccionPedida(fase);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar el release.'));
        this.cargando.set(false);
      },
    });
  }

  /** Desde la pestaña Releases del proyecto, "Revertir" llega con ?accion=revertir (PDF pág. 36). */
  private abrirAccionPedida(fase: DetalleFase): void {
    if (this.route.snapshot.queryParamMap.get('accion') !== 'revertir') return;
    this.router.navigate([], { queryParams: { accion: null }, queryParamsHandling: 'merge', replaceUrl: true });
    const revertir = fase.acciones.find((a) => a.accion === 'REVERTIR');
    if (revertir) this.presionar(revertir);
  }
}
