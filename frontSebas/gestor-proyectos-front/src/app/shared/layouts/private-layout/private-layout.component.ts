import { Component, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { SpinnerComponent } from '../../components/spinner/spinner.component';
import { ToastComponent } from '../../components/toast/toast.component';

@Component({
  selector: 'app-private-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent, SpinnerComponent, ToastComponent],
  templateUrl: './private-layout.component.html',
  styleUrl: './private-layout.component.scss',
})
export class PrivateLayoutComponent {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly router = inject(Router);

  protected readonly sidebarCollapsed = signal(
    isPlatformBrowser(this.platformId) ? window.innerWidth < 992 : false,
  );
  protected readonly mobileOpen = signal(false);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.router.events
        .pipe(filter((e) => e instanceof NavigationEnd))
        .subscribe(() => this.mobileOpen.set(false));
    }
  }

  onToggleSidebar(): void {
    if (isPlatformBrowser(this.platformId) && window.innerWidth < 768) {
      this.mobileOpen.update((v) => !v);
    } else {
      this.sidebarCollapsed.update((v) => !v);
    }
  }

  closeMobileSidebar(): void {
    this.mobileOpen.set(false);
  }
}
