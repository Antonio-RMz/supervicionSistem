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
import { finalize } from 'rxjs/operators';
import { TicketService } from '../../../../core/services/ticket.service';
import { PrioridadProyectoService } from '../../../../core/services/prioridad-proyecto.service';
import { ProyectoService } from '../../../../core/services/proyecto.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { PrioridadProyecto } from '../../../../core/models/configuracion.model';
import { FaseProyectoResponse } from '../../../../core/models/proyecto.model';
import { CrearHistoriaRequest, ResponsableDisponibleResponse, TicketDetalleApi } from '../../../../core/models/ticket.model';

@Component({
  selector: 'app-crear-historia-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './crear-historia-modal.component.html',
  styleUrl: './crear-historia-modal.component.scss',
})
export class CrearHistoriaModalComponent implements OnInit {
  // Signal inputs — consistent with project pattern (ver crear-fase-modal)
  readonly faseId     = input<number | null>(null);
  readonly proyectoId = input<number | null>(null);

  readonly historiaCreada = output<TicketDetalleApi>();
  readonly cerrar         = output<void>();

  private readonly fb           = inject(FormBuilder);
  private readonly ticketSvc    = inject(TicketService);
  private readonly prioridadSvc = inject(PrioridadProyectoService);
  private readonly proyectoSvc  = inject(ProyectoService);
  private readonly toast        = inject(ToastService);

  readonly prioridades          = signal<PrioridadProyecto[]>([]);
  readonly fases                = signal<FaseProyectoResponse[]>([]);
  readonly responsables         = signal<ResponsableDisponibleResponse[]>([]);
  readonly cargandoResponsables = signal(false);
  readonly errorResponsables    = signal<string | null>(null);
  readonly enviando             = signal(false);
  readonly errorMsg             = signal<string | null>(null);

  readonly form = this.fb.group(
    {
      faseId:               [null as number | null],
      titulo:               ['', [Validators.required, Validators.maxLength(255)]],
      descripcion:          ['', Validators.required],
      idPrioridadTicket:    [null as number | null, Validators.required],
      idUsuarioResponsable: [null as number | null],
      fechaInicio:          [''],
      fechaVencimiento:     [''],
    },
    { validators: CrearHistoriaModalComponent.validarFechas },
  );

  get faseControl()   { return this.form.get('faseId')!; }
  get titulo()        { return this.form.get('titulo')!; }
  get descripcion()   { return this.form.get('descripcion')!; }
  get prioridadCtrl() { return this.form.get('idPrioridadTicket')!; }
  get fechaVencCtrl() { return this.form.get('fechaVencimiento')!; }

  // Getter reads the signal input with ()
  get mostrarSelectorFase(): boolean {
    const fId = this.faseId();
    return !fId || fId <= 0;
  }

  ngOnInit(): void {
    const pid = this.proyectoId();
    const fid = this.faseId();

    this.prioridadSvc.getAll().subscribe({
      next: lista => this.prioridades.set(lista),
    });

    if (pid && pid > 0) {
      this.cargarResponsables(pid);
    }

    if (this.mostrarSelectorFase) {
      this.faseControl.setValidators(Validators.required);
      this.faseControl.updateValueAndValidity();

      if (pid && pid > 0) {
        this.proyectoSvc.obtenerEstructura(pid).subscribe({
          next: estructura => this.fases.set(estructura.fases),
        });
      }

      this.faseControl.valueChanges.subscribe(() => {
        this.form.get('idUsuarioResponsable')!.setValue(null);
      });
    }
  }

  private cargarResponsables(proyectoId: number): void {
    if (!proyectoId || proyectoId <= 0) {
      this.responsables.set([]);
      return;
    }
    this.cargandoResponsables.set(true);
    this.errorResponsables.set(null);

    this.proyectoSvc
      .obtenerResponsablesDisponibles(proyectoId)
      .pipe(finalize(() => this.cargandoResponsables.set(false)))
      .subscribe({
        next: lista => this.responsables.set(lista ?? []),
        error: err => {
          this.responsables.set([]);
          this.errorResponsables.set(
            err?.error?.mensaje ?? 'No fue posible cargar los responsables disponibles.',
          );
        },
      });
  }

  protected submit(): void {
    if (this.form.invalid || this.enviando()) return;

    const v   = this.form.getRawValue();
    const fId = this.faseId();
    const faseIdFinal = fId && fId > 0
      ? fId
      : v.faseId ? Number(v.faseId) : null;

    if (!faseIdFinal) {
      this.errorMsg.set('No fue posible identificar la fase.');
      return;
    }

    this.enviando.set(true);
    this.errorMsg.set(null);

    const body: CrearHistoriaRequest = {
      titulo:               v.titulo!.trim(),
      descripcion:          v.descripcion!.trim(),
      idPrioridadTicket:    Number(v.idPrioridadTicket),
      idUsuarioResponsable: v.idUsuarioResponsable ? Number(v.idUsuarioResponsable) : null,
      fechaInicio:          v.fechaInicio ? `${v.fechaInicio}T00:00:00` : null,
      fechaVencimiento:     v.fechaVencimiento ? `${v.fechaVencimiento}T00:00:00` : null,
    };

    this.ticketSvc.crearHistoria(faseIdFinal, body).subscribe({
      next: historia => {
        this.enviando.set(false);
        this.toast.success('La Historia de Usuario se creó como borrador.');
        this.historiaCreada.emit(historia);
        this.cerrar.emit();
      },
      error: err => {
        this.enviando.set(false);
        const mensaje =
          err?.error?.mensaje ??
          err?.error?.message ??
          err?.message ??
          'No fue posible crear la Historia de Usuario.';
        this.errorMsg.set(mensaje);
      },
    });
  }

  protected onOverlayClick(e: MouseEvent): void {
    if ((e.target as HTMLElement).classList.contains('ch-overlay') && !this.enviando()) {
      this.cerrar.emit();
    }
  }

  private static validarFechas(group: AbstractControl): ValidationErrors | null {
    const inicio = group.get('fechaInicio')?.value as string;
    const fin    = group.get('fechaVencimiento')?.value as string;
    if (inicio && fin && fin < inicio) return { fechaVencAnterior: true };
    return null;
  }
}
