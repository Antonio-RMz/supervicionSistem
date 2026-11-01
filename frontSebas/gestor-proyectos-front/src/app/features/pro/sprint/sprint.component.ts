import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../../core/services/session.service';
import { ProyectoService } from '../../../core/services/proyecto.service';

interface Card {
  id: number;
  codigo: string;
  titulo: string;
  puntos: number;
  assignee: string;
  color: string;
}

interface KanbanCol {
  id: string;
  label: string;
  icon: string;
  colorClass: string;
  cards: Card[];
}

@Component({
  selector: 'app-sprint',
  standalone: true,
  imports: [],
  templateUrl: './sprint.component.html',
  styleUrl: './sprint.component.scss',
})
export class SprintComponent implements OnInit {
  protected readonly session   = inject(SessionService);
  private readonly proyectoSvc = inject(ProyectoService);
  private readonly route       = inject(ActivatedRoute);
  private readonly router      = inject(Router);

  protected readonly proyectoId = signal<number>(0);

  protected readonly proyectoNombre = computed(
    () => this.session.proyectoActivo()?.nombre ?? 'Proyecto',
  );

  protected readonly columnas = signal<KanbanCol[]>([
    {
      id: 'sin_iniciar', label: 'Sin iniciar', icon: 'bi-circle', colorClass: 'col-pending',
      cards: [
        { id: 4, codigo: 'HIS-004', titulo: 'Ver perfil y editar datos',                puntos: 2, assignee: 'U', color: '#4361ee' },
        { id: 7, codigo: 'HIS-007', titulo: 'Filtrar proyectos por estado',              puntos: 2, assignee: 'D', color: '#0d9488' },
        { id: 8, codigo: 'HIS-008', titulo: 'Historial de cambios por HIS',             puntos: 3, assignee: 'T', color: '#f72585' },
      ],
    },
    {
      id: 'en_proceso', label: 'En proceso', icon: 'bi-arrow-repeat', colorClass: 'col-progress',
      cards: [
        { id: 3, codigo: 'HIS-003', titulo: 'Gestionar roles del sistema',              puntos: 5, assignee: 'L', color: '#7209b7' },
        { id: 5, codigo: 'HIS-005', titulo: 'Asignar tareas al equipo',                puntos: 3, assignee: 'D', color: '#0d9488' },
      ],
    },
    {
      id: 'qa', label: 'Prueba QA', icon: 'bi-bug', colorClass: 'col-qa',
      cards: [
        { id: 6, codigo: 'HIS-006', titulo: 'Corregir error 500 en reportes',           puntos: 1, assignee: 'T', color: '#f72585' },
      ],
    },
    {
      id: 'listo', label: 'Listo', icon: 'bi-check-circle-fill', colorClass: 'col-done',
      cards: [
        { id: 1, codigo: 'HIS-001', titulo: 'Iniciar sesión con credenciales',          puntos: 3, assignee: 'L', color: '#7209b7' },
        { id: 2, codigo: 'HIS-002', titulo: 'Recuperar contraseña por correo',          puntos: 2, assignee: 'U', color: '#4361ee' },
      ],
    },
  ]);

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
  }

  protected volver(): void {
    this.router.navigate(['/pro', this.proyectoId()]);
  }

  protected totalPuntos(col: KanbanCol): number {
    return col.cards.reduce((s, c) => s + c.puntos, 0);
  }
}
