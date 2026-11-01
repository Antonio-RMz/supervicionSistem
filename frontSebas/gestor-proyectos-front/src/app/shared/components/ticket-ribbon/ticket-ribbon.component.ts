import { Component, computed, input } from '@angular/core';

interface RibbonCfg {
  label: string;
  icon:  string;
  color: string;
  bg:    string;
}

const TIPOS: Record<string, RibbonCfg> = {
  FASE: { label: 'Fase',          icon: 'bi-layers',          color: '#7c3aed', bg: '#ede9fe' },
  HIST: { label: 'Historia',      icon: 'bi-journal-text',    color: '#2563eb', bg: '#dbeafe' },
  ACT:  { label: 'Actividad',     icon: 'bi-check2-square',   color: '#059669', bg: '#d1fae5' },
  BUG:  { label: 'Bug',           icon: 'bi-bug',             color: '#dc2626', bg: '#fee2e2' },
  MEJ:  { label: 'Mejora',        icon: 'bi-arrow-up-circle', color: '#d97706', bg: '#fef3c7' },
  TEC:  { label: 'Técnica',       icon: 'bi-gear',            color: '#475569', bg: '#f1f5f9' },
  INV:  { label: 'Investigación', icon: 'bi-search',          color: '#0891b2', bg: '#ecfeff' },
  DOC:  { label: 'Documentación', icon: 'bi-file-text',       color: '#1e40af', bg: '#eff6ff' },
};

const DEFAULT: RibbonCfg = { label: 'Ticket', icon: 'bi-tag', color: '#6c757d', bg: '#f8f9fa' };

@Component({
  selector: 'app-ticket-ribbon',
  standalone: true,
  templateUrl: './ticket-ribbon.component.html',
  styleUrl:    './ticket-ribbon.component.scss',
})
export class TicketRibbonComponent {
  readonly tipo = input.required<string>();

  protected readonly cfg = computed<RibbonCfg>(() =>
    TIPOS[(this.tipo() ?? '').toUpperCase()] ?? DEFAULT,
  );
}
