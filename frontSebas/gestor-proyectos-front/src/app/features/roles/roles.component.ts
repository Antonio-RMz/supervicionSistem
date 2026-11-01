import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { RolesService } from './roles.service';
import { RolItem } from '../../core/models/usuario.model';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [],
  templateUrl: './roles.component.html',
  styleUrl: './roles.component.scss',
})
export class RolesComponent implements OnInit {
  private readonly rolesSvc = inject(RolesService);
  private readonly router = inject(Router);

  protected readonly pageTitle = 'Roles';
  protected readonly pageIcon = 'bi-shield-lock-fill';
  protected readonly pageDesc = 'Administración de roles y perfiles';

  protected readonly roles = signal<RolItem[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  protected verDetalle(rol: RolItem): void {
    this.router.navigate(['/roles', rol.id], { state: { rol } });
  }

  protected reintentar(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.rolesSvc.getAll().subscribe({
      next: (data) => {
        this.roles.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los roles.');
        this.loading.set(false);
      },
    });
  }
}
