import { Component, computed, input, signal } from '@angular/core';
import { ramaSugerida, TONO_ROL } from '../../../../shared/utils/flujos';
import { ticketSinEmpezar } from '../../../../shared/utils/tickets';
import { DetalleTicket } from '../../models/ticket-detalle.models';

interface PasoGit {
  titulo: string;
  rol: 'Desarrollador';
  /** Qué hacer en la app al terminar el paso, si algo */
  nota: string;
  comandos: string;
}

/** Trabajando; lo demás ya envió su PR. (Antes de Iniciar, la sección Desarrollo no se muestra.) */
const TRABAJANDO = ['EN_PROGRESO', 'EN_CORRECCION', 'BLOQUEADA'];

/**
 * Los dos comandos de git del Desarrollador: crear su rama y crear el PR, con el paso en que va.
 * La app solo guarda el nombre de la rama: los comandos se corren en la copia del repositorio.
 */
@Component({
  selector: 'app-comandos-git',
  template: `
    <ol class="pasos">
      @for (p of pasos(); track p.titulo; let i = $index) {
        <li [class.hecho]="i < actual()" [class.actual]="i === actual()">
          <div class="paso-cabecera">
            <span class="numero">
              @if (i < actual()) { <i class="bi bi-check-lg"></i> } @else { {{ i + 1 }} }
            </span>
            <strong>{{ p.titulo }}</strong>
            <span class="rol tono-{{ tonoRol[p.rol] }}">{{ p.rol }}</span>
          </div>
          <div class="consola">
            <pre><code>{{ p.comandos }}</code></pre>
            <button type="button" (click)="copiar(p.comandos, i)" [title]="copiado() === i ? 'Copiado' : 'Copiar'">
              <i class="bi" [class.bi-clipboard]="copiado() !== i" [class.bi-check2]="copiado() === i"></i>
              <span class="solo-lectores">Copiar comandos</span>
            </button>
          </div>
          @if (p.nota) {
            <small>{{ p.nota }}</small>
          }
        </li>
      }
    </ol>
  `,
  styles: `
    .pasos {
      display: flex;
      flex-direction: column;
      gap: 0.9rem;
      margin: 0.9rem 0 0;
      padding: 0;
      list-style: none;
    }
    li { display: flex; flex-direction: column; gap: 0.45rem; }
    li:not(.actual):not(.hecho) { opacity: 0.75; }
    .paso-cabecera { display: flex; align-items: center; gap: 0.5rem; }
    .numero {
      display: grid;
      place-items: center;
      width: 22px;
      height: 22px;
      border: 2px solid var(--borde-fuerte);
      border-radius: 50%;
      color: var(--texto-suave);
      font-size: 0.72rem;
      font-weight: 700;
    }
    .hecho .numero { border-color: var(--verde); background: var(--verde); color: var(--sobre-color); }
    .actual .numero { border-color: var(--morado); background: var(--morado); color: var(--sobre-color); }
    strong { color: var(--tinta); font-weight: 600; }
    .actual strong { color: var(--morado); }
    .rol {
      padding: 0.05rem 0.45rem;
      border-radius: 999px;
      background: var(--tono-fondo);
      color: var(--tono-texto);
      font-size: 0.72rem;
      font-weight: 600;
    }
    .consola {
      display: flex;
      align-items: flex-start;
      gap: 0.6rem;
      padding: 0.6rem 0.9rem;
      border-radius: 10px;
      background: var(--consola);
      color: var(--consola-texto);
    }
    pre {
      flex: 1;
      min-width: 0;
      margin: 0;
      overflow-x: auto;
      font-family: var(--fuente-mono);
      font-size: 0.83rem;
      line-height: 1.55;
    }
    button { border: 0; background: none; color: inherit; cursor: pointer; }
    small { color: var(--texto-suave); font-size: 0.82rem; }
  `,
})
export class ComandosGit {
  readonly ticket = input.required<DetalleTicket>();

  protected readonly tonoRol = TONO_ROL;
  protected readonly copiado = signal<number | null>(null);

  protected readonly pasos = computed<PasoGit[]>(() => {
    const t = this.ticket();
    const rama = t.rama ?? ramaSugerida(t.tipo, t.numero, t.titulo, t.hotfix);
    // El hotfix sale de main y regresa a main; lo demás, de la rama base del repositorio.
    const base = t.hotfix ? 'main' : (t.modulo?.ramaBase ?? 'develop');
    const titulo = `${t.codigo}: ${t.titulo}`.replace(/"/g, "'");
    return [
      {
        titulo: 'Crear tu rama',
        rol: 'Desarrollador',
        nota: '',
        comandos: [`git checkout ${base}`, `git pull origin ${base}`, `git checkout -b ${rama}`, `git push -u origin ${rama}`].join('\n'),
      },
      {
        titulo: 'Crear el PR',
        rol: 'Desarrollador',
        nota: 'Luego presiona Enviar PR en Acciones. (También puedes abrirlo desde GitHub.)',
        comandos: `gh pr create --base ${base} --head ${rama} --title "${titulo}"`,
      },
    ];
  });

  /** En qué paso va según el estado del ticket. */
  protected readonly actual = computed(() => {
    const estado = this.ticket().estado;
    if (ticketSinEmpezar(this.ticket().tipo, estado)) return 0;
    if (TRABAJANDO.includes(estado)) return 1;
    return 2;
  });

  protected copiar(texto: string, paso: number): void {
    navigator.clipboard?.writeText(texto).then(() => {
      this.copiado.set(paso);
      setTimeout(() => this.copiado.set(null), 1500);
    });
  }
}
