import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { formatear, numerosDeLinea } from '../../../../shared/utils/json';
import { textoEstado } from '../../../../shared/utils/tickets';
import { aJson, leerJson } from '../../data/importacion.parser';
import { HistoriasService } from '../../data/historias.service';
import { PreparacionHistoria } from '../../models/historias.models';
import { CamposJson, HistoriasDeFase } from '../../models/importacion.models';

type Pestana = 'prompt' | 'json' | 'copiar';

/** Un pedazo del prompt; los datos de la fase se pintan en ámbar. */
interface Parte {
  texto: string;
  dato?: boolean;
}

/** Los botones de "Incluir" de la pestaña JSON de historias. */
const CAMPOS: { id: keyof CamposJson; texto: string }[] = [
  { id: 'ids', texto: 'IDs' },
  { id: 'estado', texto: 'Estado' },
  { id: 'criterios', texto: 'Criterios' },
  { id: 'prioridad', texto: 'Prioridad y puntos' },
  { id: 'figma', texto: 'Figma' },
];

/**
 * Importar historias: el prompt para pasarle a la IA con el requerimiento y las imágenes del diseño, y el JSON que
 * devuelve. Si el formato es correcto, crea todas las historias en Borrador dentro de la fase.
 * La pestaña JSON de historias hace lo contrario: copia las que ya tiene la fase (para pasarlas a la IA o a otra fase).
 */
@Component({
  selector: 'app-importar-historias-page',
  imports: [RouterLink],
  templateUrl: './importar-historias-page.html',
  styleUrl: './importar-historias-page.scss',
})
export class ImportarHistoriasPage implements OnInit {
  private readonly service = inject(HistoriasService);
  private readonly router = inject(Router);
  private readonly usuario = inject(AuthService).usuario;

  /** :id del proyecto */
  readonly id = input.required<string>();
  readonly faseId = input.required<string>();
  /** ?ver=copiar: abre directo la pestaña JSON de historias */
  readonly ver = input<string>();

  protected readonly preparacion = signal<PreparacionHistoria | null>(null);
  protected readonly errorCarga = signal<string | null>(null);
  protected readonly pestana = signal<Pestana>('prompt');
  /** La fase ya no recibe historias: solo se pueden copiar las que tiene. */
  protected readonly soloCopiar = signal(false);
  protected readonly json = signal('');
  protected readonly analistaId = signal<number | null>(null);
  protected readonly copiado = signal(false);
  protected readonly enviando = signal(false);
  protected readonly errorEnvio = signal<string | null>(null);

  // ─── JSON de historias (copiar las de la fase) ───
  protected readonly campos = CAMPOS;
  protected readonly deFase = signal<HistoriasDeFase | null>(null);
  protected readonly errorDeFase = signal<string | null>(null);
  /** '' = todas; si no, un estado. */
  protected readonly cuales = signal('');
  protected readonly incluir = signal<CamposJson>({ ids: true, estado: true, criterios: true, prioridad: true, figma: false });
  protected readonly copiadoJson = signal(false);

  /** "Todas (9)", "Borrador (3)"… */
  protected readonly opcionesCuales = computed(() => {
    const historias = this.deFase()?.historias ?? [];
    const porEstado = new Map<string, number>();
    historias.forEach((h) => porEstado.set(h.estado, (porEstado.get(h.estado) ?? 0) + 1));
    return [
      { valor: '', texto: `todas (${historias.length})` },
      ...[...porEstado].map(([estado, n]) => ({ valor: estado, texto: `${textoEstado(estado)} (${n})` })),
    ];
  });

  protected readonly jsonDeFase = computed(() => {
    const datos = this.deFase();
    if (!datos) return '';
    const cuales = this.cuales();
    return aJson(datos, datos.historias.filter((h) => !cuales || h.estado === cuales), this.incluir());
  });

  protected readonly numerosDeFase = computed(() => numerosDeLinea(this.jsonDeFase()));

  protected readonly fase = computed(
    () => this.preparacion()?.fases.find((f) => f.id === Number(this.faseId())) ?? null,
  );

  protected readonly lectura = computed(() => leerJson(this.json()));

  /** Historias cuyo "Como" no es uno de los usuarios del sistema (solo aviso: se crean igual). */
  protected readonly comoDesconocidos = computed(() => {
    const lectura = this.lectura();
    const usuarios = this.preparacion()?.usuariosDelSistema ?? [];
    if (!lectura.valido || !usuarios.length) return [];
    const nombres = new Set(usuarios.map((u) => u.nombre.toLowerCase()));
    return [...new Set(lectura.historias.map((h) => h.como).filter((c) => !nombres.has(c.toLowerCase())))];
  });
  /** "1\n2\n3…": una por renglón del JSON. */
  protected readonly numerosDeLinea = computed(() => numerosDeLinea(this.json()));

  protected readonly prompt = computed<Parte[]>(() => {
    const p = this.preparacion();
    const f = this.fase();
    if (!p || !f) return [];
    const modulos = f.modulos.map((m) => m.corto).join(' · ') || 'sin repositorios';
    const usuarios = p.usuariosDelSistema;
    return [
      { texto: '# Rol\nEres analista de software.\n\n# Contexto\nProyecto: ' },
      { texto: p.proyectoNombre, dato: true },
      { texto: ' · Fase: ' },
      { texto: f.titulo, dato: true },
      { texto: '\nMódulos de la fase: ' },
      { texto: modulos, dato: true },
      ...(usuarios.length
        ? [
            { texto: '\n\n# Usuarios del sistema\nQuiénes usan el sistema; "como" debe ser exactamente uno de estos nombres:\n' },
            {
              texto: usuarios.map((u) => `- ${u.nombre}${u.descripcion ? ': ' + u.descripcion : ''}`).join('\n'),
              dato: true,
            },
          ]
        : []),
      { texto: PROMPT_RESTO },
      { texto: '[pega aquí tu requerimiento y adjunta las imágenes]', dato: true },
    ];
  });

  ngOnInit(): void {
    this.service.preparar(Number(this.id())).subscribe({
      next: (p) => {
        this.preparacion.set(p);
        if (this.ver() === 'copiar' || !this.fase()) this.elegirPestana('copiar');
        if (!this.fase()) {
          this.soloCopiar.set(true);
          return;
        }
        const yo = p.analistas.find((a) => a.id === this.usuario()?.id);
        this.analistaId.set(yo?.id ?? p.analistas.find((a) => a.esAnalista)?.id ?? p.analistas[0]?.id ?? null);
      },
      error: (e) => this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar la fase.')),
    });
  }

  protected elegirPestana(pestana: Pestana): void {
    this.pestana.set(pestana);
    // Las historias de la fase se piden la primera vez que se abre la pestaña.
    if (pestana === 'copiar' && !this.deFase()) {
      this.errorDeFase.set(null);
      this.service.deFase(Number(this.id()), Number(this.faseId())).subscribe({
        next: (d) => this.deFase.set(d),
        error: (e) => this.errorDeFase.set(mensajeDeError(e, 'No pudimos cargar las historias de la fase.')),
      });
    }
  }

  protected alternarCampo(campo: keyof CamposJson): void {
    this.incluir.update((c) => ({ ...c, [campo]: !c[campo] }));
  }

  protected copiarJson(): void {
    navigator.clipboard.writeText(this.jsonDeFase()).then(() => {
      this.copiadoJson.set(true);
      setTimeout(() => this.copiadoJson.set(false), 2000);
    });
  }

  protected copiar(): void {
    const texto = this.prompt().map((p) => p.texto).join('');
    navigator.clipboard.writeText(texto).then(() => {
      this.copiado.set(true);
      setTimeout(() => this.copiado.set(false), 2000);
    });
  }

  protected formatear(): void {
    this.json.set(formatear(this.json()));
  }

  /** Los números de línea se mueven junto con el texto. */
  protected alDesplazar(texto: HTMLTextAreaElement, numeros: HTMLElement): void {
    numeros.scrollTop = texto.scrollTop;
  }

  protected ejecutar(): void {
    const lectura = this.lectura();
    const analistaId = this.analistaId();
    if (!lectura.valido || !analistaId || this.enviando()) return;
    this.enviando.set(true);
    this.errorEnvio.set(null);
    const proyectoId = Number(this.id());
    const faseId = Number(this.faseId());
    this.service.importar(proyectoId, faseId, analistaId, lectura.historias).subscribe({
      next: (resultado) =>
        this.router.navigate(['/proyectos', proyectoId, 'fases', faseId], { state: { importacion: resultado } }),
      error: (e) => {
        this.enviando.set(false);
        this.errorEnvio.set(mensajeDeError(e, 'No pudimos crear las historias.'));
      },
    });
  }
}

/** Lo que no cambia de fase a fase. Formato v1: si cambia, también cambia leerJson y el back. */
const PROMPT_RESTO = `

# Material
Te adjunto imágenes del diseño de las pantallas. Basa las historias en lo que se ve: una historia por pantalla o acción del usuario, y criterios a partir de campos, botones y mensajes.

# Tarea
Con las imágenes y el requerimiento de abajo, crea las historias de usuario de esta fase. No incluyas actividades técnicas.

# Formato de respuesta
Responde SOLO con un JSON válido que siga EXACTAMENTE esta plantilla (mismos nombres de campos, sin texto extra):
{
  "historias": [
    {
      "titulo": "texto corto",
      "como": "rol del usuario",
      "quiero": "lo que quiere hacer",
      "para": "el beneficio",
      "prioridad": "Alta | Media | Baja",
      "puntos": 1 | 2 | 3 | 5 | 8 | 13,
      "criterios": ["criterio 1", "criterio 2"]
    }
  ]
}

# Reglas
- Obligatorios: titulo y como
- "criterios" es una lista: 2 a 5 textos verificables

# Requerimiento
`;
