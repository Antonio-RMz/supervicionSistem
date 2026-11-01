import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

const ESTADO_MAP: Record<string, { label: string; css: string; dot: string }> = {
  // Estados de proyecto / fase
  ACTIVO:      { label: 'Activo',      css: 'estado-activo',    dot: '#10b981' },
  EN_PROGRESO: { label: 'En progreso', css: 'estado-progreso',  dot: '#3b82f6' },
  EN_PAUSA:    { label: 'En pausa',    css: 'estado-pausa',     dot: '#f59e0b' },
  PENDIENTE:   { label: 'Pendiente',   css: 'estado-pendiente', dot: '#f97316' },
  CANCELADO:   { label: 'Cancelado',   css: 'estado-cerrado',   dot: '#9ca3af' },
  CERRADO:     { label: 'Cerrado',     css: 'estado-cerrado',   dot: '#9ca3af' },
  COMPLETADA:  { label: 'Completada',  css: 'estado-activo',    dot: '#10b981' },
  // Estados de actividad (workflow)
  POR_HACER:            { label: 'Por hacer',            css: 'estado-por-hacer',    dot: '#9ca3af' },
  EN_DESARROLLO:        { label: 'En desarrollo',        css: 'estado-en-desarrollo', dot: '#3b82f6' },
  REVISION_QA:          { label: 'Revisión de QA',       css: 'estado-revision-qa',  dot: '#f59e0b' },
  LISTO_PARA_INTEGRAR:  { label: 'Listo para integrar',  css: 'estado-listo',        dot: '#8b5cf6' },
  PR_EN_REVISION:       { label: 'PR en revisión',       css: 'estado-pr',           dot: '#a855f7' },
  INTEGRADO:            { label: 'Integrado',            css: 'estado-integrado',    dot: '#10b981' },
};

@Component({
  selector: 'app-estado-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="estado-badge" [class]="data().css" [class.sm]="size() === 'sm'">
      <span class="estado-dot" [style.background]="data().dot"></span>
      {{ data().label }}
    </span>
  `,
  styleUrl: './estado-badge.component.scss',
})
export class EstadoBadgeComponent {
  readonly estado = input.required<string>();
  readonly size   = input<'sm' | 'md'>('md');

  protected readonly data = computed(() => {
    return ESTADO_MAP[this.estado()] ?? { label: this.estado(), css: 'estado-activo', dot: '#10b981' };
  });
}
