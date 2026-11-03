import { Component, computed, input, signal } from '@angular/core';
import { DetalleHistoria } from '../../models/historia-detalle.models';

/**
 * Prompt de desarrollo de una historia con Desarrolladores: para que la IA la implemente completa
 * (como escuela/prompt_ia_hu0001.md). Se genera aquí mismo; no sale nada del navegador.
 * Solo lo ven el Líder y sus Desarrolladores.
 */
@Component({
  selector: 'app-prompt-historia',
  template: `
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
    .prompt {
      margin: 0; max-height: 60vh; overflow: auto; padding: 1rem 1.1rem; border-radius: 10px;
      background: var(--consola); color: var(--consola-texto); font-family: var(--fuente-mono);
      font-size: 0.82rem; line-height: 1.6; white-space: pre-wrap;
    }
    .pie { display: flex; gap: 0.6rem; margin-top: 1rem; }
    .boton.chico { padding: 0.45rem 0.9rem; }
  `,
})
export class PromptHistoria {
  readonly historia = input.required<DetalleHistoria>();

  protected readonly copiado = signal(false);
  protected readonly prompt = computed(() => armarPrompt(this.historia()));

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
    enlace.download = `prompt-${this.historia().codigo.toLowerCase()}.md`;
    enlace.click();
    URL.revokeObjectURL(enlace.href);
  }
}

function armarPrompt(h: DetalleHistoria): string {
  const d = h.desarrollo!;
  const p: string[] = [];
  p.push(`# ${h.codigo} · ${h.titulo}`, '',
    `Vas a implementar completa la historia **${h.codigo} · ${h.titulo}** del proyecto **${h.proyectoNombre}** ` +
    `(${h.faseTitulo}). Haz tú todo el trabajo: código, migraciones, pruebas y commits.`);

  if (d.repositorios.length) {
    p.push('', '## Repositorios', '', '| Repositorio | Qué es | Tecnología | Rama base | Enlace |', '|---|---|---|---|---|');
    for (const r of d.repositorios) {
      p.push(`| \`${r.nombre}\` | ${r.tipo} | ${r.tecnologia ?? '—'} | \`${r.ramaBase}\` | ${r.url ?? '—'} |`);
    }
  }

  const base = d.repositorios[0]?.ramaBase ?? 'develop';
  p.push('', '## Cómo se trabaja con git (importante)', '');
  if (!d.requierePr) {
    p.push(`- Se trabaja **directo en \`${base}\`**, en cada repositorio. **No crees ramas ni pull requests.**`,
      `- Antes de empezar: \`git checkout ${base}\` y \`git pull origin ${base}\`.`);
  } else {
    p.push(`- Trabaja en una rama \`feature/${h.codigo.toLowerCase()}-…\` que sale de \`${base}\`, con el mismo nombre en cada repositorio.`,
      `- Al terminar, sube la rama y abre un PR por repositorio hacia \`${base}\`.`);
  }
  p.push(`- Haz **commits chicos**; cada mensaje empieza con el código de la historia: \`${h.codigo}: <qué hace el cambio>\`.`,
    '- Los commits van en español, en presente, y cada uno compila.',
    '- No hagas commit de `node_modules`, `dist`, `target` ni de contraseñas reales.');
  if (!d.requierePr) p.push(`- Al terminar: \`git push origin ${base}\` en cada repositorio.`);

  p.push('', '## La historia', '');
  const frase = [h.como && `**Como** ${h.como}`, h.quiero && `**quiero** ${h.quiero}`, h.para && `**para** ${h.para}`]
    .filter(Boolean).join(', ');
  if (frase) p.push(frase + '.');
  if (h.criterios.length) {
    p.push('', '**Criterios de aceptación** (al final, todos deben poder comprobarse):');
    h.criterios.forEach((c, i) => p.push(`${i + 1}. ${c.descripcion}`));
  }
  if (h.figmaUrl) p.push('', `**Diseño:** ${h.figmaUrl} (úsalo como referencia visual).`);

  if (d.desarrolladores.length > 1) {
    p.push('', '## Quién hace qué', '');
    for (const dev of d.desarrolladores) p.push(`- ${dev.nombre}: ${dev.repositorio ?? 'todos los repositorios'}`);
  }

  if (d.trabajo.length) {
    p.push('', '## Lo que ya está anotado', '', 'Lo marcado ya está hecho; lo demás es la guía de lo que falta:');
    for (const t of d.trabajo) p.push(`- [${t.hecho ? 'x' : ' '}] ${t.descripcion}${t.repositorio ? ` (${t.repositorio})` : ''}`);
  }

  p.push('', '## Cómo terminar', '',
    '1. Cada repositorio compila y sus pruebas pasan.',
    '2. Cada criterio de aceptación se puede comprobar.',
    '3. Al final, dame una lista breve de lo que hiciste (para anotarlo en "Lo que hice") y cómo probarlo.');
  return p.join('\n');
}
