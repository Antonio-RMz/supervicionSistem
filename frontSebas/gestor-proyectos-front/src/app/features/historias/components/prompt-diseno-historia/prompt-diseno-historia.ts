import { Component, computed, input, signal } from '@angular/core';
import { DetalleHistoria } from '../../models/historia-detalle.models';

/**
 * Prompt de diseño de la historia: para que la IA diseñe su pantalla en Figma (como escuela/prompt_figma_fase_1.md,
 * pero de una sola historia). Solo lo ven el Líder y la Analista. Se genera aquí mismo; no sale nada del navegador.
 */
@Component({
  selector: 'app-prompt-diseno-historia',
  template: `
    <label class="estilo">
      <span>Indicaciones de estilo <small>(opcional: colores, menú, logo, datos de ejemplo…)</small></span>
      <textarea rows="3" [value]="estilo()" (input)="estilo.set($any($event.target).value)"
                placeholder="Menú lateral oscuro con el logo del sistema&#10;Fondo #f1f5f9 con tarjetas blancas"></textarea>
    </label>
    <pre class="prompt">{{ prompt() }}</pre>
    <div class="pie">
      <button type="button" class="boton boton-primario chico" (click)="copiar()">
        <i class="bi" [class.bi-clipboard]="!copiado()" [class.bi-clipboard-check]="copiado()"></i>
        {{ copiado() ? 'Copiado' : 'Copiar prompt' }}
      </button>
      <button type="button" class="boton boton-secundario chico" (click)="descargar()">
        <i class="bi bi-download"></i> Descargar .md
      </button>
    </div>
  `,
  styles: `
    :host { display: block; }
    .estilo {
      display: flex; flex-direction: column; gap: 0.35rem; margin-bottom: 0.9rem;
      span { color: var(--tinta); font-weight: 500; }
      small { color: var(--texto-suave); font-weight: 400; }
      textarea {
        padding: 0.6rem 0.8rem; border: 1px solid var(--borde); border-radius: 10px;
        background: var(--blanco); color: var(--tinta); font: inherit; resize: vertical;
      }
    }
    .prompt {
      margin: 0; max-height: 55vh; overflow: auto; padding: 1rem 1.1rem; border-radius: 10px;
      background: var(--consola); color: var(--consola-texto); font-family: var(--fuente-mono);
      font-size: 0.82rem; line-height: 1.6; white-space: pre-wrap;
    }
    .pie { display: flex; gap: 0.6rem; margin-top: 1rem; }
    .boton.chico { padding: 0.45rem 0.9rem; }
  `,
})
export class PromptDisenoHistoria {
  readonly historia = input.required<DetalleHistoria>();

  protected readonly estilo = signal('');
  protected readonly copiado = signal(false);

  protected readonly prompt = computed(() => armarPromptDiseno(this.historia(), this.estilo()));

  protected copiar(): void {
    navigator.clipboard?.writeText(this.prompt()).then(() => {
      this.copiado.set(true);
      setTimeout(() => this.copiado.set(false), 2000);
    });
  }

  protected descargar(): void {
    const archivo = new Blob([this.prompt()], { type: 'text/markdown;charset=utf-8' });
    const enlace = document.createElement('a');
    enlace.href = URL.createObjectURL(archivo);
    enlace.download = `prompt-diseno-${this.historia().codigo.toLowerCase()}.md`;
    enlace.click();
    URL.revokeObjectURL(enlace.href);
  }
}

/** El frame de la historia, su estilo y lo que debe verse (sus criterios). */
function armarPromptDiseno(h: DetalleHistoria, estilo: string): string {
  const titulo = `${h.codigo} · ${h.titulo}`;
  const p: string[] = [
    `Diseña la pantalla de la historia "${titulo}" del sistema web "${h.proyectoNombre}" (${h.faseTitulo}).`,
  ];
  if (h.figmaUrl) p.push('', `**Archivo de Figma:** ${h.figmaUrl}`);

  p.push('', '# Cómo organizar el diseño',
    '- Un frame de escritorio de 1440 px de ancho.',
    `- Arriba del frame, fuera de la pantalla, pon un título grande (48 px, negrita) exactamente así: "${titulo}".`
      + ' Debajo, en texto chico gris, la frase "Como…, quiero…, para…".',
    '- Nombra el frame igual que su título.',
    '- Si necesita un modal, un aviso o un estado de error, ponlo en otro frame al lado con el mismo título y el sufijo'
      + ' " · Modal" o " · Error".');

  p.push('', '# Estilo');
  const indicaciones = estilo.trim();
  if (indicaciones) {
    p.push(...indicaciones.split('\n').map((l) => (l.trim().startsWith('-') ? l : `- ${l}`)));
  } else {
    p.push('- Limpio, moderno y orientado a datos.',
      '- Si el sistema ya tiene pantallas diseñadas en el archivo, sigue su mismo estilo (menú, barra superior, colores).');
  }
  p.push('- Usa datos de ejemplo realistas.', '- Todos los textos en español de México.');

  p.push('', '# La historia');
  const frase = [h.como && `Como ${h.como}`, h.quiero && `quiero ${h.quiero}`, h.para && `para ${h.para}`]
    .filter(Boolean).join(', ');
  if (frase) p.push(frase + '.');
  if (h.criterios.length) {
    p.push('', 'Lo que debe verse en la pantalla:', ...h.criterios.map((c) => `- ${c.descripcion}`));
  }
  return p.join('\n');
}
