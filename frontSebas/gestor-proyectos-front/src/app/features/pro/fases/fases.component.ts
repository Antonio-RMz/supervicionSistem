import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../../core/services/session.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { Fase } from '../../../core/models/proyecto.model';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge/estado-badge.component';

const MOCK_FASES: Fase[] = [
  { id: 1, numero: 1, nombre: 'Análisis y Levantamiento', descripcion: 'Relevamiento de requerimientos', color: '#4361ee', progreso: 100, historiasCompletadas: 8,  historiasTotal: 8,  actividadesCompletadas: 12, actividadesTotal: 12, estado: 'COMPLETADA'  },
  { id: 2, numero: 2, nombre: 'Desarrollo',               descripcion: 'Backend y frontend',             color: '#7209b7', progreso: 65,  historiasCompletadas: 5,  historiasTotal: 10, actividadesCompletadas: 9,  actividadesTotal: 16, estado: 'EN_PROGRESO' },
  { id: 3, numero: 3, nombre: 'Pruebas',                  descripcion: 'QA y pruebas de regresión',      color: '#0d9488', progreso: 0,   historiasCompletadas: 0,  historiasTotal: 6,  actividadesCompletadas: 0,  actividadesTotal: 10, estado: 'PENDIENTE'   },
  { id: 4, numero: 4, nombre: 'Implementación',           descripcion: 'Puesta en producción',           color: '#e67e22', progreso: 0,   historiasCompletadas: 0,  historiasTotal: 4,  actividadesCompletadas: 0,  actividadesTotal: 6,  estado: 'PENDIENTE'   },
];

@Component({
  selector: 'app-fases',
  standalone: true,
  imports: [EstadoBadgeComponent],
  templateUrl: './fases.component.html',
  styleUrl: './fases.component.scss',
})
export class FasesComponent implements OnInit {
  protected readonly session     = inject(SessionService);
  private  readonly proyectoSvc = inject(ProyectoService);
  private  readonly route       = inject(ActivatedRoute);
  private  readonly router      = inject(Router);

  protected readonly proyectoId     = signal(0);
  protected readonly proyectoNombre = computed(() => this.session.proyectoActivo()?.nombre ?? 'Proyecto');
  protected readonly fases          = signal<Fase[]>([]);
  protected readonly mostrarModal   = signal(false);

  private readonly rol = computed(() => this.session.usuario()?.rol ?? '');

  protected readonly puedeCrear = computed(() =>
    this.rol() === 'ADMINISTRADOR' || this.rol() === 'LÍDER_DE_PROYECTO',
  );
  protected readonly puedeEditar  = computed(() => this.puedeCrear());
  protected readonly puedeEliminar = computed(() => this.rol() === 'ADMINISTRADOR');

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigate(['/pro']); return; }
    this.proyectoId.set(id);

    const activo = this.session.proyectoActivo();
    if (!activo || activo.id !== id) {
      this.proyectoSvc.getById(id).subscribe({
        next:  p => this.session.seleccionarProyecto(p),
        error: () => this.router.navigate(['/pro']),
      });
    }

    this.fases.set(MOCK_FASES);
  }

  protected verDetalle(fase: Fase): void {
    this.router.navigate(['/pro', this.proyectoId(), 'fases', fase.id]);
  }

  protected abrirNuevaFase(): void {
    this.mostrarModal.set(true);
  }

  protected volver(): void {
    this.router.navigate(['/pro', this.proyectoId()]);
  }
}
