import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TitleCasePipe } from '@angular/common';
import { SessionService } from '../../../core/services/session.service';
import { ProyectoService } from '../../../core/services/proyecto.service';

interface Historia {
  id: number;
  codigo: string;
  titulo: string;
  puntos: number;
  prioridad: 'alta' | 'media' | 'baja';
  tipo: string;
}

const PRIORIDAD_CLASS: Record<string, string> = {
  alta:  'prio-alta',
  media: 'prio-media',
  baja:  'prio-baja',
};

const PLACEHOLDER_HISTORIAS: Historia[] = [
  { id: 1, codigo: 'HIS-001', titulo: 'Como usuario quiero iniciar sesión con credenciales',    puntos: 3, prioridad: 'alta',  tipo: 'Feature' },
  { id: 2, codigo: 'HIS-002', titulo: 'Como usuario quiero recuperar mi contraseña por correo', puntos: 2, prioridad: 'alta',  tipo: 'Feature' },
  { id: 3, codigo: 'HIS-003', titulo: 'Como admin quiero gestionar roles del sistema',           puntos: 5, prioridad: 'media', tipo: 'Feature' },
  { id: 4, codigo: 'HIS-004', titulo: 'Como usuario quiero ver mi perfil y editarlo',            puntos: 2, prioridad: 'baja',  tipo: 'Feature' },
  { id: 5, codigo: 'HIS-005', titulo: 'Como líder quiero asignar tareas al equipo',             puntos: 3, prioridad: 'media', tipo: 'Feature' },
  { id: 6, codigo: 'HIS-006', titulo: 'Corregir error 500 en endpoint de reportes',              puntos: 1, prioridad: 'alta',  tipo: 'Bug'     },
];

@Component({
  selector: 'app-backlog',
  standalone: true,
  imports: [TitleCasePipe],
  templateUrl: './backlog.component.html',
  styleUrl: './backlog.component.scss',
})
export class BacklogComponent implements OnInit {
  protected readonly session   = inject(SessionService);
  private readonly proyectoSvc = inject(ProyectoService);
  private readonly route       = inject(ActivatedRoute);
  private readonly router      = inject(Router);

  protected readonly proyectoId = signal<number>(0);
  protected readonly historias = signal<Historia[]>([]);

  protected readonly proyectoNombre = computed(
    () => this.session.proyectoActivo()?.nombre ?? 'Proyecto',
  );

  protected readonly prioridadClass = (p: string) => PRIORIDAD_CLASS[p] ?? '';

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

    this.historias.set(PLACEHOLDER_HISTORIAS);
  }

  protected volver(): void {
    this.router.navigate(['/pro', this.proyectoId()]);
  }
}
