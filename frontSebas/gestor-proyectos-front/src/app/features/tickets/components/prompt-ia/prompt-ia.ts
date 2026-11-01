import { Component, computed, input, signal } from '@angular/core';
import { DetalleTicket } from '../../models/ticket-detalle.models';

type Tarea = 'implementar' | 'pruebas' | 'revisar' | 'explicar';
type Seccion = 'historia' | 'criterios' | 'detalle' | 'subtareas';

const TAREAS: { id: Tarea; texto: string }[] = [
  { id: 'implementar', texto: 'Implementar' },
  { id: 'pruebas', texto: 'Casos de prueba' },
  { id: 'revisar', texto: 'Revisar mi PR' },
  { id: 'explicar', texto: 'Explicar el código existente' },
];

const SECCIONES: { id: Seccion; texto: string }[] = [
  { id: 'historia', texto: 'Historia' },
  { id: 'criterios', texto: 'Criterios' },
  { id: 'detalle', texto: 'Detalle técnico' },
  { id: 'subtareas', texto: 'Subtareas' },
];

/**
 * "Trabajar con IA" del detalle del ticket (PDF pág. 29): arma un prompt con los datos del ticket
 * para pegarlo en el asistente que use el Desarrollador. Se genera aquí mismo; no sale nada del navegador
 * y no lleva credenciales ni datos del cliente.
 */
@Component({
  selector: 'app-prompt-ia',
  templateUrl: './prompt-ia.html',
  styleUrl: './prompt-ia.scss',
})
export class PromptIa {
  readonly ticket = input.required<DetalleTicket>();

  protected readonly tareas = TAREAS;
  protected readonly secciones = SECCIONES;
  protected readonly tarea = signal<Tarea>('implementar');
  protected readonly incluir = signal(new Set<Seccion>(SECCIONES.map((s) => s.id)));
  protected readonly copiado = signal(false);

  protected readonly prompt = computed(() => armarPrompt(this.ticket(), this.tarea(), this.incluir()));

  protected alternar(seccion: Seccion): void {
    this.incluir.update((actual) => {
      const copia = new Set(actual);
      if (!copia.delete(seccion)) copia.add(seccion);
      return copia;
    });
  }

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
    enlace.download = `${this.ticket().codigo.toLowerCase()}-${this.tarea()}.md`;
    enlace.click();
    URL.revokeObjectURL(enlace.href);
  }
}

function armarPrompt(t: DetalleTicket, tarea: Tarea, incluir: Set<Seccion>): string {
  const modulo = t.modulo;
  const tecnologia = modulo?.tecnologia ?? 'el stack del proyecto';
  const partes: string[] = [];

  partes.push('# Rol', `Eres desarrollador senior de ${tecnologia}${modulo ? ` en el repositorio ${modulo.nombre}` : ''}.`);

  const queHacer: Record<Tarea, string> = {
    implementar: t.titulo,
    pruebas: `Escribir los casos de prueba de: ${t.titulo}`,
    revisar: `Revisar el PR de: ${t.titulo}. Señala errores, casos sin cubrir y riesgos, en orden de gravedad.`,
    explicar: `Explicar el código existente relacionado con: ${t.titulo}, para poder modificarlo con seguridad.`,
  };
  partes.push('', `# Tarea · ${t.codigo}`, queHacer[tarea]);

  if (incluir.has('historia') && t.historia) {
    const h = t.historia;
    partes.push('', `# Contexto (${h.codigo})`,
      [h.como && `Como ${h.como}`, h.quiero && `quiero ${h.quiero}`, h.para && `para ${h.para}`]
        .filter(Boolean).join(', ') + '.');
  }
  if (incluir.has('criterios') && t.criterios.length) {
    partes.push('', '# Debe cumplir', ...t.criterios.map((c) => `- ${c.descripcion}`));
  }
  if (incluir.has('detalle') && (t.descripcion || t.capas.length)) {
    partes.push('', '# Detalle técnico');
    if (t.descripcion) partes.push(t.descripcion);
    if (t.capas.length) partes.push(`Capas: ${t.capas.join(' · ')}`);
  }
  if (incluir.has('subtareas') && t.subtareas.length) {
    partes.push('', '# Subtareas', ...t.subtareas.map((s) => `- [${s.hecha ? 'x' : ' '}] ${s.descripcion}`));
  }

  const entrega: Record<Tarea, string> = {
    implementar: `${t.rama ? `Rama ${t.rama}. ` : ''}Código y pruebas.\nAl final, lista breve de archivos cambiados y cómo probarlo.`,
    pruebas: 'Casos con datos de entrada, resultado esperado y si es prueba unitaria o de integración.',
    revisar: 'Lista de observaciones con el archivo y la línea, y una sugerencia concreta para cada una.',
    explicar: 'Explicación paso a paso y qué partes tocar para este ticket.',
  };
  partes.push('', '# Entrega', entrega[tarea]);
  return partes.join('\n');
}
