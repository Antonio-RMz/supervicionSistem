import { Component, computed, input, signal } from '@angular/core';
import { DetalleHistoria } from '../../models/historia-detalle.models';

interface PasoGit {
  titulo: string;
  comandos: string;
}

/**
 * Pestaña Git de una historia con Desarrolladores. En un proyecto Normal sin PR no hay comandos: todo va directo
 * a develop. Con PR (o en Gitflow): la misma rama en cada repositorio y un PR por repositorio.
 */
@Component({
  selector: 'app-git-historia',
  template: `
    <!-- ─── Los repositorios de la historia, con su enlace ─── -->
    <section class="tarjeta bloque">
      <h2>Repositorios</h2>
      <ul class="repos">
        @for (r of desarrollo().repositorios; track r.id) {
          <li>
            <i class="bi bi-github"></i>
            <div class="datos">
              <strong>{{ r.nombre }}</strong>
              <small>{{ r.tipo }}{{ r.tecnologia ? ' · ' + r.tecnologia : '' }} · rama base <code>{{ r.ramaBase }}</code></small>
            </div>
            @if (r.enlace) {
              <a class="boton boton-secundario chico" [href]="r.enlace" target="_blank" rel="noopener">
                <i class="bi bi-box-arrow-up-right"></i> Abrir
              </a>
              <button type="button" class="boton boton-secundario chico" (click)="copiar(r.url!, 'url-' + r.id)"
                      [title]="'Copiar ' + r.url">
                <i class="bi" [class.bi-clipboard]="copiado() !== 'url-' + r.id" [class.bi-check2]="copiado() === 'url-' + r.id"></i>
                {{ copiado() === 'url-' + r.id ? 'Copiado' : 'Copiar para clonar' }}
              </button>
            } @else {
              <small class="sin-enlace">Sin enlace: agrégalo en Repositorios del proyecto</small>
            }
          </li>
        } @empty {
          <li class="vacio">La historia aún no tiene repositorios.</li>
        }
      </ul>
    </section>

    <section class="tarjeta bloque">
      <h2>Cómo subir tus cambios</h2>
      @if (!desarrollo().requierePr) {
        <p class="mensaje">
          <i class="bi bi-cloud-arrow-up"></i>
          <span>
            Sube tus cambios directo a <strong>{{ bases() }}</strong>, con commits que empiecen con
            <code>{{ codigo() }}:</code> (ej. <code>{{ codigo() }}: agrega la pantalla de login</code>).
            Cuando termines, marca tu parte en <strong>Desarrollo</strong>.
          </span>
        </p>
      } @else {
        <p class="pista">
          Usa la misma rama en cada repositorio: <code>{{ rama() }}</code>. Al terminar, abre un PR por repositorio;
          el Líder lo revisa antes de que la valide la Analista.
        </p>
        @for (r of desarrollo().repositorios; track r.id) {
          <h3>
            @if (r.enlace) {
              <a [href]="r.enlace" target="_blank" rel="noopener">{{ r.nombre }} <i class="bi bi-box-arrow-up-right"></i></a>
            } @else {
              {{ r.nombre }}
            }
            <small>{{ r.tipo }}{{ r.tecnologia ? ' · ' + r.tecnologia : '' }}</small>
          </h3>
          <ol class="pasos">
            @for (p of pasos(r.ramaBase); track p.titulo; let i = $index) {
              <li>
                <strong>{{ i + 1 }}. {{ p.titulo }}</strong>
                <div class="consola">
                  <pre><code>{{ p.comandos }}</code></pre>
                  <button type="button" (click)="copiar(p.comandos, r.id + '-' + i)"
                          [title]="copiado() === r.id + '-' + i ? 'Copiado' : 'Copiar'">
                    <i class="bi" [class.bi-clipboard]="copiado() !== r.id + '-' + i" [class.bi-check2]="copiado() === r.id + '-' + i"></i>
                    <span class="solo-lectores">Copiar comandos</span>
                  </button>
                </div>
              </li>
            }
          </ol>
        }
      }
    </section>
  `,
  styleUrl: './git-historia.scss',
})
export class GitHistoria {
  readonly historia = input.required<DetalleHistoria>();

  protected readonly copiado = signal<string | null>(null);
  protected readonly desarrollo = computed(() => this.historia().desarrollo!);
  /** "HU-0002" */
  protected readonly codigo = computed(() => this.historia().codigo);
  /** develop (o las ramas base distintas de cada repositorio) */
  protected readonly bases = computed(() =>
    [...new Set(this.desarrollo().repositorios.map((r) => r.ramaBase))].join(' / ') || 'develop',
  );
  /** "feature/hu-0002-iniciar-sesion-como-coordinador" */
  protected readonly rama = computed(() => {
    const h = this.historia();
    let slug = h.titulo
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '');
    if (slug.length > 40) slug = slug.slice(0, 40).replace(/-[^-]*$/, '');
    return `feature/${h.codigo.toLowerCase()}${slug ? '-' + slug : ''}`;
  });

  protected pasos(base: string): PasoGit[] {
    const rama = this.rama();
    const titulo = `${this.codigo()}: ${this.historia().titulo}`.replace(/"/g, "'");
    return [
      {
        titulo: 'Crear tu rama',
        comandos: [`git checkout ${base}`, `git pull origin ${base}`, `git checkout -b ${rama}`, `git push -u origin ${rama}`].join('\n'),
      },
      { titulo: 'Abrir el PR', comandos: `gh pr create --base ${base} --head ${rama} --title "${titulo}"` },
    ];
  }

  protected copiar(texto: string, clave: string): void {
    navigator.clipboard?.writeText(texto).then(() => {
      this.copiado.set(clave);
      setTimeout(() => this.copiado.set(null), 1500);
    });
  }
}
