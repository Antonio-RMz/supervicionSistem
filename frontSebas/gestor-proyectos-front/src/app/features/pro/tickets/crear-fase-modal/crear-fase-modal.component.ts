import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import {
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { environment } from '../../../../../environments/environment';
import { TicketService } from '../../../../core/services/ticket.service';
import { PrioridadProyectoService } from '../../../../core/services/prioridad-proyecto.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { PrioridadProyecto } from '../../../../core/models/configuracion.model';
import { TicketProyectoResumenResponse } from '../../../../core/models/proyecto.model';
import { CrearFaseRequest } from '../../../../core/models/ticket.model';

@Component({
  selector: 'app-crear-fase-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './crear-fase-modal.component.html',
  styleUrl: './crear-fase-modal.component.scss',
})
export class CrearFaseModalComponent implements OnInit {
  readonly proyectoId = input.required<number>();

  readonly faseCreadaEvent = output<TicketProyectoResumenResponse>();
  readonly cerrar          = output<void>();

  private readonly fb           = inject(FormBuilder);
  private readonly ticketSvc    = inject(TicketService);
  private readonly prioridadSvc = inject(PrioridadProyectoService);
  private readonly toast        = inject(ToastService);

  readonly prioridades = signal<PrioridadProyecto[]>([]);
  readonly enviando    = signal(false);
  readonly errorMsg    = signal<string | null>(null);

  readonly form = this.fb.group(
    {
      titulo:              ['', [Validators.required, Validators.maxLength(150)]],
      descripcion:         ['', [Validators.required, Validators.maxLength(1000)]],
      prioridadId:         [null as number | null, Validators.required],
      fechaInicioEstimada: ['', Validators.required],
      fechaFinEstimada:    ['', Validators.required],
    },
    { validators: CrearFaseModalComponent.validarFechas },
  );

  get titulo()     { return this.form.get('titulo')!;              }
  get descripcion(){ return this.form.get('descripcion')!;         }
  get prioridadId(){ return this.form.get('prioridadId')!;         }
  get fechaInicio(){ return this.form.get('fechaInicioEstimada')!; }
  get fechaFin()   { return this.form.get('fechaFinEstimada')!;    }

  ngOnInit(): void {
    this.prioridadSvc.getAll().subscribe({
      next: lista => this.prioridades.set(lista),
    });
  }

  protected submit(): void {
    if (this.form.invalid || this.enviando()) return;

    this.enviando.set(true);
    this.errorMsg.set(null);

    const v = this.form.getRawValue();
    const body: CrearFaseRequest = {
      proyectoId:          this.proyectoId(),
      titulo:              v.titulo!.trim(),
      descripcion:         v.descripcion!.trim(),
      prioridadId:         Number(v.prioridadId),
      fechaInicioEstimada: v.fechaInicioEstimada!,
      fechaFinEstimada:    v.fechaFinEstimada!,
    };

    const url = `${environment.apiUrl}/tickets/fases`;
    const camposInvalidos = Object.entries(body)
      .filter(([, v]) => v === undefined || v === null || (typeof v === 'number' && isNaN(v)))
      .map(([k]) => k);

    console.group('POST — Nueva Fase');
    console.log('URL:',               url);
    console.log('Método:',            'POST');
    console.log('Body:',              body);
    console.log('Body JSON:',         JSON.stringify(body, null, 2));
    console.log('proyectoId:',        body.proyectoId,          '| tipo:', typeof body.proyectoId);
    console.log('prioridadId:',       body.prioridadId,         '| tipo:', typeof body.prioridadId);
    console.log('fechaInicio:',       body.fechaInicioEstimada, '| tipo:', typeof body.fechaInicioEstimada);
    console.log('fechaFin:',          body.fechaFinEstimada,    '| tipo:', typeof body.fechaFinEstimada);
    console.log('Campos inválidos:',  camposInvalidos.length ? camposInvalidos : '✓ ninguno');
    console.log('Content-Type:',      'application/json (Angular HttpClient por defecto)');
    console.groupEnd();

    this.ticketSvc.crearFase(body).subscribe({
      next: fase => {
        this.enviando.set(false);
        this.toast.success('Fase creada correctamente.');
        this.faseCreadaEvent.emit(fase);
        this.cerrar.emit();
      },
      error: err => {
        this.enviando.set(false);
        const status: number = err?.status ?? 0;
        if (status === 400) {
          this.errorMsg.set(this.extraer400(err.error));
        } else if (status === 403) {
          this.errorMsg.set('No tienes permiso para crear fases.');
        } else if (status === 409) {
          this.errorMsg.set(err.error?.mensaje ?? err.error?.message ?? 'Ya existe un conflicto con los datos enviados.');
        } else {
          this.errorMsg.set('No fue posible crear la fase. Intenta nuevamente.');
        }
      },
    });
  }

  protected onOverlayClick(e: MouseEvent): void {
    if ((e.target as HTMLElement).classList.contains('cf-overlay')) {
      this.cerrar.emit();
    }
  }

  private extraer400(body: unknown): string {
    if (!body) return 'Datos inválidos. Revisa los campos e intenta nuevamente.';
    if (typeof body === 'string') return body;
    const b = body as Record<string, unknown>;
    if (Array.isArray(b['errores'])) return (b['errores'] as string[]).join(' ');
    if (typeof b['mensaje'] === 'string') return b['mensaje'];
    if (typeof b['message'] === 'string') return b['message'];
    return 'Datos inválidos. Revisa los campos e intenta nuevamente.';
  }

  private static validarFechas(group: AbstractControl): ValidationErrors | null {
    const inicio = group.get('fechaInicioEstimada')?.value as string;
    const fin    = group.get('fechaFinEstimada')?.value as string;
    if (inicio && fin && fin < inicio) return { fechaFinAnterior: true };
    return null;
  }
}
