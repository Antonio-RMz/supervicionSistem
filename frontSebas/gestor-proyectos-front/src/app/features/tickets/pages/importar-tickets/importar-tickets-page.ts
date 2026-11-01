import { Component, computed, inject, input, OnInit, signal, WritableSignal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { formatear, numerosDeLinea } from '../../../../shared/utils/json';
import { HistoriaDetalleService } from '../../../historias/data/historia-detalle.service';
import { DetalleHistoria } from '../../../historias/models/historia-detalle.models';
import { aJsonTickets, leerJsonTickets } from '../../data/importacion-tickets.parser';
import { NuevoTicketService } from '../../data/nuevo-ticket.service';
import { CamposTickets, RepositorioDeHistoria } from '../../models/importacion-tickets.models';

type Pestana = 'prompt' | 'json' | 'copiar';

/** Un pedazo del prompt; los datos de la historia se pintan en ámbar. */
interface Parte {
  texto: string;
  dato?: boolean;
}

/** Los botones de "Incluir" de la pestaña JSON de tickets. */
const CAMPOS: { id: keyof CamposTickets; texto: string }[] = [
  { id: 'ids', texto: 'IDs' },
  { id: 'estado', texto: 'Estado' },
  { id: 'repositorio', texto: 'Repositorio' },
  { id: 'responsable', texto: 'Responsable' },
];

/** Lo que no cambia de historia a historia. Si cambia el formato, también cambian leerJsonTickets y el back. */
const TAREA_Y_FORMATO = `

# Tarea
Divide la historia en tickets de trabajo técnicos. Cada ticket toca un solo repositorio y se puede desarrollar y
revisar por separado. Entre todos deben cubrir todos los criterios de aceptación. Lo más importante son las
subtareas: el trabajo técnico concreto que hará el desarrollador, paso por paso.

# Formato de respuesta
Responde SOLO con un JSON válido que siga EXACTAMENTE esta plantilla (mismos nombres de campos, sin texto extra):
{
  "tickets": [
    {
      "titulo": "acción + qué, en corto",
      "descripcion": "qué se hace, dónde y cómo se sabe que está listo",
      "repositorio": "`;

const FORMATO_RESTO = `",
      "subtareas": ["paso técnico 1", "paso técnico 2", "paso técnico 3"]
    }
  ]
}

# Reglas
- Obligatorio: titulo
`;

const REGLAS_Y_EJEMPLOS = `
- "subtareas": de 3 a 8 pasos técnicos concretos y verificables. Di qué se crea o modifica (tabla, vista, trigger,
  endpoint, Controller/Business, servicio, componente, pantalla, validación, correo, configuración) y con qué regla
  o condición. Nada genérico como "hacer el backend" o "probar".
- Si algo necesita investigarse antes de programarlo, crea un ticket de "Análisis de …" cuyas subtareas sean lo que
  hay que revisar y decidir.
- Escribe en español, empezando con la acción: "Creación de…", "Modificación de…", "Implementación de…",
  "Análisis de…", "Configuración de…".

# Ejemplos del nivel de detalle esperado (de otro proyecto)
- Modificación de view_bolsa_postulados para agregar bolsa_grupo_proceso_vinculacion.
- Creación de lógica en backend para que, al realizar una compra, se verifique si el WS de pagos está disponible;
  si lo está continúa con el pago y, si no, muestra un mensaje indicando que no es posible completar la transacción.
- Implementación de lógica para que, al detectar un error SSL en la API de pagos, se envíe automáticamente un correo
  de notificación a los desarrolladores.
- Modificación de los triggers considerando las membresías generales y la restricción de vacantes disponibles en
  productos individuales.
- Creación de Controller y Business para que cada organización vea sus membresías y productos individuales.

# Notas
`;

/**
 * Importar tickets de trabajo: lo mismo que Importar historias, pero dentro de una historia. El prompt lleva la
 * historia con sus criterios y repositorios; el JSON pide lo necesario para crear cada Actividad (título,
 * descripción y repositorio) y, sobre todo, sus subtareas técnicas. El responsable se elige abajo. La pestaña JSON de tickets copia los que ya tiene.
 */
@Component({
  selector: 'app-importar-tickets-page',
  imports: [RouterLink],
  templateUrl: './importar-tickets-page.html',
  styleUrl: './importar-tickets-page.scss',
})
export class ImportarTicketsPage implements OnInit {
  private readonly tickets = inject(NuevoTicketService);
  private readonly historias = inject(HistoriaDetalleService);
  private readonly router = inject(Router);

  /** :id del proyecto */
  readonly id = input.required<string>();
  readonly historiaId = input.required<string>();
  /** ?ver=copiar: abre directo la pestaña JSON de tickets */
  readonly ver = input<string>();

  protected readonly historia = signal<DetalleHistoria | null>(null);
  protected readonly repositorios = signal<RepositorioDeHistoria[]>([]);
  protected readonly desarrolladores = signal<{ id: number; nombreCompleto: string }[]>([]);
  protected readonly errorCarga = signal<string | null>(null);
  /** La historia ya no recibe tickets (o no eres quien los crea): solo se copian los que tiene. */
  protected readonly soloCopiar = signal(false);
  protected readonly pestana = signal<Pestana>('prompt');
  protected readonly json = signal('');
  protected readonly responsableId = signal<number | null>(null);
  protected readonly copiado = signal(false);
  protected readonly enviando = signal(false);
  protected readonly errorEnvio = signal<string | null>(null);

  protected readonly lectura = computed(() => leerJsonTickets(this.json(), this.repositorios()));
  protected readonly numerosDeLinea = computed(() => numerosDeLinea(this.json()));

  // ─── JSON de tickets (copiar los de la historia) ───
  protected readonly campos = CAMPOS;
  protected readonly incluir = signal<CamposTickets>({ ids: true, estado: true, repositorio: true, responsable: false });
  protected readonly copiadoJson = signal(false);
  protected readonly jsonDeHistoria = computed(() => {
    const h = this.historia();
    return h ? aJsonTickets(h.proyectoNombre, `${h.codigo} · ${h.titulo}`, h.tickets, this.incluir()) : '';
  });
  protected readonly numerosDeHistoria = computed(() => numerosDeLinea(this.jsonDeHistoria()));

  protected readonly prompt = computed<Parte[]>(() => {
    const h = this.historia();
    if (!h) return [];
    const repos = this.repositorios();
    const enunciado = [h.como && `Como ${h.como}`, h.quiero && `quiero ${h.quiero}`, h.para && `para ${h.para}`]
      .filter(Boolean)
      .join(', ');
    const criterios = h.criterios.length
      ? h.criterios.map((c, i) => `${i + 1}. ${c.descripcion}`).join('\n')
      : '(sin criterios todavía)';
    const lista = repos.map((r) => `- ${r.corto}: ${r.nombre}${r.tecnologia ? ' · ' + r.tecnologia : ''}`).join('\n');
    const opciones = repos.map((r) => r.corto).join(' | ');
    const regla = repos.length > 1
      ? `- "repositorio" es uno de: ${repos.map((r) => r.corto).join(', ')}`
      : '- "repositorio" puede omitirse: la historia trabaja en uno solo';
    return [
      { texto: '# Rol\nEres desarrollador líder de software.\n\n# Contexto\nProyecto: ' },
      { texto: h.proyectoNombre, dato: true },
      { texto: ' · ' },
      { texto: h.faseTitulo, dato: true },
      { texto: '\nHistoria: ' },
      { texto: `${h.codigo} · ${h.titulo}`, dato: true },
      { texto: '\n' },
      { texto: enunciado || '(sin Como / Quiero / Para)', dato: true },
      { texto: '\n\nCriterios de aceptación:\n' },
      { texto: criterios, dato: true },
      { texto: '\n\nRepositorios de la historia:\n' },
      { texto: lista || '(sin repositorios)', dato: true },
      { texto: TAREA_Y_FORMATO },
      { texto: opciones || 'API', dato: true },
      { texto: FORMATO_RESTO },
      { texto: regla },
      { texto: REGLAS_Y_EJEMPLOS },
      { texto: '[pega aquí notas técnicas si las hay]', dato: true },
    ];
  });

  ngOnInit(): void {
    const proyectoId = Number(this.id());
    const historiaId = Number(this.historiaId());
    forkJoin({
      historia: this.historias.detalle(proyectoId, historiaId),
      preparacion: this.tickets.preparar(proyectoId),
    }).subscribe({
      next: ({ historia, preparacion }) => {
        this.historia.set(historia);
        const fase = preparacion.fases.find((f) => f.historias.some((h) => h.id === historiaId));
        const deTicket = fase?.historias.find((h) => h.id === historiaId);
        if (!fase || !deTicket || !historia.puedeCrearTicket) {
          this.soloCopiar.set(true);
          this.pestana.set('copiar');
          return;
        }
        this.repositorios.set(
          fase.modulos
            .filter((m) => !deTicket.moduloIds.length || deTicket.moduloIds.includes(m.id))
            .map((m) => ({ corto: m.corto, tipo: m.tipo, nombre: m.nombre, tecnologia: m.tecnologia })),
        );
        const devs = preparacion.equipo.filter((p) => p.roles.includes('DESARROLLADOR'));
        this.desarrolladores.set(devs);
        this.responsableId.set(devs[0]?.id ?? null);
        if (this.ver() === 'copiar') this.pestana.set('copiar');
      },
      error: (e) => this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar la historia.')),
    });
  }

  protected copiar(): void {
    this.alPortapapeles(this.prompt().map((p) => p.texto).join(''), this.copiado);
  }

  protected copiarJson(): void {
    this.alPortapapeles(this.jsonDeHistoria(), this.copiadoJson);
  }

  protected formatear(): void {
    this.json.set(formatear(this.json()));
  }

  protected alternarCampo(campo: keyof CamposTickets): void {
    this.incluir.update((c) => ({ ...c, [campo]: !c[campo] }));
  }

  /** Los números de línea se mueven junto con el texto. */
  protected alDesplazar(texto: HTMLElement, numeros: HTMLElement): void {
    numeros.scrollTop = texto.scrollTop;
  }

  protected ejecutar(): void {
    const lectura = this.lectura();
    const responsableId = this.responsableId();
    if (!lectura.valido || !responsableId || this.enviando()) return;
    this.enviando.set(true);
    this.errorEnvio.set(null);
    const proyectoId = Number(this.id());
    const historiaId = Number(this.historiaId());
    this.tickets.importar(proyectoId, historiaId, responsableId, lectura.tickets).subscribe({
      next: (creados) =>
        this.router.navigate(['/proyectos', proyectoId, 'historias', historiaId], { state: { ticketsImportados: creados } }),
      error: (e) => {
        this.enviando.set(false);
        this.errorEnvio.set(mensajeDeError(e, 'No pudimos crear los tickets.'));
      },
    });
  }

  private alPortapapeles(texto: string, aviso: WritableSignal<boolean>): void {
    navigator.clipboard.writeText(texto).then(() => {
      aviso.set(true);
      setTimeout(() => aviso.set(false), 2000);
    });
  }
}
