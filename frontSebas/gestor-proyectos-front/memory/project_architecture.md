---
name: project-architecture
description: Stack Angular 21 SSR, estructura core/shared/features, flujo JWT completo, rutas lazy-loaded
metadata:
  type: project
---

## Stack
- **Framework**: Angular 21 (standalone components, SSR con `@angular/ssr`)
- **Estilos**: SCSS + Bootstrap (importado vía SCSS), Bootstrap Icons (`bi-*`)
- **Auth**: JWT con interceptor HTTP (`auth.interceptor.ts`) y guards (`auth.guard.ts`, `no-auth.guard.ts`)
- **Estado global**: Signals (`signal`, `computed`, `inject`)
- **Formularios**: Reactive Forms (`FormBuilder`, `FormGroup`, `FormArray`) y template-driven (`FormsModule`) — no mezclados en el mismo componente

## Estructura de directorios

```
src/app/
  core/
    constants/    api.constants.ts, modulos.catalog.ts
    guards/       auth.guard.ts, no-auth.guard.ts
    handlers/     global-error.handler.ts
    interceptors/ auth.interceptor.ts
    models/       auth-response, login-request, proyecto.model, usuario-response, ticket.model, ...
    services/     auth.service.ts, proyecto.service.ts, session.service.ts, ticket.service.ts
  features/
    auth/         login/
    pro/          pro.component (lista), detalle/, backlog/, historias/, sprint/, equipo/, nuevo/, tickets/
    dashboard/
    tickets/      (global ticket detail: /tickets/:folio)
    configuracion/, his/, permisos/, qa/, roles/, sesiones/, sol/, usuarios/
    errors/       forbidden, not-found, server-error, unauthorized
  shared/
    components/   header/, sidebar/, spinner/, toast/, ticket-ribbon/
    layouts/      private-layout/
    services/     spinner.service.ts, toast.service.ts
```

## Convenciones visuales (design tokens)
- **Fondo de página**: `#f0f2f5` (definido en `body`)
- **Cards**: `background: #fff; border-radius: 16px; border: 1.5px solid #f0f2f5; box-shadow: 0 2px 8px rgba(0,0,0,0.05); padding: 1.75rem`
- **Color primario (acento)**: `#4361ee` (índigo)
- **Texto primario**: `#14172b`; Texto secundario: `#6c757d`; Texto muted: `#9ca3af`
- **Input**: `border: 1.5px solid #e5e7eb; border-radius: 9px; background: #fafafa` | focus: `border-color: #4361ee; box-shadow: 0 0 0 3px rgba(67,97,238,0.1)`
- **Botón primario**: `background: #4361ee; color: #fff; border-radius: 9px`
- **Botón secundario**: `border: 1.5px solid #e5e7eb; color: #6b7280; background: #fff`
- **Estado badges** (`.estado-badge`): dot coloreado + texto; activo=verde, progreso=azul, pausa=amarillo, cerrado=gris, pendiente=naranja

## Clases globales disponibles (en `styles.scss`)
- `.feature-page` — wrapper de página (width: 100%)
- `.breadcrumb-bar`, `.breadcrumb-link`, `.breadcrumb-sep`, `.breadcrumb-current`
- `.page-header`, `.page-header-left`, `.page-title`, `.page-subtitle`
- `.btn-regresar`, `.empty-state`, `.empty-icon`

## Modelos clave

### ProyectoResponse
```typescript
{ id, nombre, descripcion, color, iniciales, progreso, usuarios, estado, categoria, urlQa?, urlProduccion? }
```

### ProyectoRequest (POST/PUT)
```typescript
{ nombre, descripcion, progreso, estado, categoria }
```

### ProyectoDetalleResponse
```typescript
{
  id, nombre, iniciales, color, descripcion, estado: { id, nombre, codigo }, categoria,
  cliente: { id, nombre }, progresoPorcentaje,
  metricas: ProyectoMetricasResponse | null,
  estructura: ProyectoEstructuraResponse | null,
  equipo: MiembroProyectoResponse[],
  ticketsRecientes: TicketProyectoResumenResponse[],
  actividadReciente: ActividadProyectoResponse[],
  documentos: DocumentoProyectoResponse[],
  fechaCreacion: string | null
}
```

### MiembroProyectoResponse
```typescript
{ idUsuario, nombre, apellido, nombreCompleto?, iniciales?, nombreRol? }
```

### TicketProyectoResumenResponse
```typescript
{
  id, folio, titulo,
  tipo: { id, codigo, nombre } | null,
  estado: { id, nombre } | null,
  prioridad: { id, nombre } | null,
  asignadoA: { idUsuario, nombre, apellido } | null,
  fechaCreacion, fechaVencimiento
}
```

## Rutas lazy-loaded relevantes
- `/pro` → `ProComponent` (lista de proyectos con modal de edición)
- `/pro/nuevo` → `ProyectoNuevoComponent` (página completa, Reactive Forms, sin llamada HTTP — TODO conectar)
- `/pro/:id` → `ProyectoDetalleComponent`
- `/pro/:id/backlog`, `/sprint`, `/historias`
- `/pro/:id/equipo` → `EquipoComponent` (miembros del proyecto, carga real desde API)
- `/pro/:id/tickets` → `ProyectoTicketsComponent` (grid paginado de tickets con filtros)
- `/tickets/:folio` → `TicketDetalleComponent` (detalle global, accesible también desde `/pro/:id/tickets/:folio`)
- `/mis-actividades` → `MisActividadesComponent` (actividades agrupadas por estado)

## Flujo JWT
`AuthService` → guarda tokens en `SessionService` → `AuthInterceptor` inyecta `Authorization: Bearer` en cada request → `RefreshResponse` para renovar token silenciosamente.

## Vista "Nuevo Proyecto" (`/pro/nuevo`)
- Componente: `src/app/features/pro/nuevo/proyecto-nuevo.component.ts`
- 5 secciones en tarjetas separadas: Información general, Estado y prioridad, Planificación, Integración y equipo, Metadata avanzada (colapsable)
- Chips para `categoria_proyecto` implementados con signal + template ref (sin ngModel)
- `FormArray` para metadata clave-valor dinámica
- `console.log(payload)` en submit — sin POST real (TODO conectar servicio)
- Solo `nombre_proyecto` es required; `github_repo_url` tiene `Validators.pattern`
- `pro.component.ts`: botón "Nuevo Proyecto" navega a `/pro/nuevo` (ya no abre modal)

## Vista "Equipo del Proyecto" (`/pro/:id/equipo`)
- Componente: `src/app/features/pro/equipo/equipo.component.ts`
- Señales: `equipo`, `cargandoEquipo`, `errorEquipo`, `idProyecto`
- Carga real con `ProyectoService.obtenerEquipo(id)` → `GET /api/v1/proyectos/:id/equipo`
- Patrón `takeUntilDestroyed(destroyRef)` + `distinctUntilChanged()` para evitar cargas duplicadas
- Avatares generados con iniciales e índice de color (`AVATAR_COLORS` array de 8 colores)
- Helpers: `obtenerNombreCompleto`, `obtenerIniciales`, `obtenerRol`, `obtenerColorAvatar`

## Vista "Tickets del Proyecto" (`/pro/:id/tickets`)
- Componente: `src/app/features/pro/tickets/proyecto-tickets.component.ts`
- Grid paginado (PAGE_SIZE = 24) con carga inicial via `forkJoin({ proyecto, detalle, pagina })`
- **Filtros**: tipo (chips, 8 tipos), estado (dropdown dinámico), prioridad (dropdown dinámico), búsqueda (debounce 380ms), ordenar
- **Tipos de ticket**: `FASE, HIST, ACT, BUG, MEJ, TEC, INV, DOC` — cada uno con color, ícono y badge configurados en `TIPOS_MAP`
- **Catálogos dinámicos**: `estadosDisponibles` y `prioridadesDisponibles` construidos desde los tickets cargados (no hay endpoint de catálogo separado)
- Paginación con ellipsis (`paginasVisibles` computed, muestra páginas contiguas + primero/último)
- `HostListener('document:click')` para cerrar todos los dropdowns al hacer clic fuera
- Navega a `/tickets/:folio` al hacer clic en una fila
- Helpers de presentación: `estadoCss`, `prioridadCss`, `prioridadIcon`, `estaVencido`, `inferirProgreso`, `formatFecha`
- **Grid**: 4 col desktop, 2 col ≤1024px, 1 col ≤580px; gap 18px
- **Tarjeta `.tc-card`**: padding `1.1rem 1.2rem` / `pt 2.65rem`, gap `0.7rem`, altura natural (sin min-height); hover eleva 2px con sombra índigo 0.14
- **`.tc-card--fase`**: modificador visual aplicado cuando `ticket.tipo?.codigo === 'FASE'`; solo cambia la sombra del hover a morada — sin borde superior (el ribbon es la única decoración de tipo visible)

## Componente compartido `TicketRibbonComponent`
- Path: `src/app/shared/components/ticket-ribbon/`
- Input: `tipo` (string, required) — código del tipo de ticket
- Muestra un ribbon/badge de color con ícono y label del tipo
- Usado en la vista de tickets del proyecto para identificar visualmente el tipo de cada ticket

## Sidebar dinámico (`SidebarComponent`)
- Path: `src/app/shared/components/sidebar/sidebar.component.ts`
- Navegación construida a partir de `session.modulos()` cruzados con `MODULOS_CATALOG` (`core/constants/modulos.catalog.ts`)
- `MODULOS_CATALOG`: 21+ módulos con `ref, nombre, icono, ruta, tipo (PRINCIPAL|SOLO_ADMIN|GENERAL|ROL), requiereProyecto`
- **Secciones del nav**: Dashboard (siempre), PRINCIPAL (siempre), Mis actividades (siempre), SOLO_ADMIN (si tiene permisos), GENERAL y ROL (solo si hay `proyectoId` activo)
- Rutas de módulos con `requiereProyecto: true` se arman como `/pro/:proyectoId<ruta>`
- `isProActive` computed desde URL reactiva via `toSignal + NavigationEnd`
- `onClickProyectos()`: comportamiento contextual — en submódulo limpia proyecto y va a `/pro`; fuera de `/pro` con proyecto activo va a `/pro/:id`; sin proyecto va a `/pro`

## Vista "Mis actividades" (`/mis-actividades`)
- Componente: `src/app/features/mis-actividades/mis-actividades.component.ts`
- Datos: `MOCK_ACTIVIDADES` (mock, pendiente conectar servicio real)
- **Agrupación por estado**: `gruposVisibles` computed agrupa `actividadesFiltradas()` según `ESTADO_CONFIG` (6 estados en orden: POR_HACER → EN_DESARROLLO → PR_EN_REVISION → REVISION_QA → LISTO_PARA_INTEGRAR → INTEGRADO)
- **Chips de estado**: barra de chips horizontal con "Todos" + uno por estado con actividades; usa `--chip-color` CSS variable; clases `.mac-chip--active` / `.mac-chip--todos-active`
- **Doble filtro**: `filtroProyectoId` (select dropdown) + `filtroEstado` (chips) aplicados en cascada
- **Layout grupos**: `.mac-grupos` → `.mac-grupo` → `.mac-grupo-header` (dot + nombre + count + línea) + `.mac-grupo-grid` (grid 4col → 3col ≤1280px → 2col ≤900px → 1col ≤580px; gap 18px)
- **Orden dentro de cada grupo**: vencimiento asc, nulls al final, luego por id
- `chipCounts` computed acumula conteo por estado desde `actividadesFiltradas()`
- **Tarjeta `.act-card`**: visual idéntico a `.tc-card` — `position: relative; overflow: visible; padding-top: 2.65rem` para el ribbon; estructura: `<app-ticket-ribbon tipo="ACT">` + top (código + menú tres puntos) + título + meta (fase · historia + proyecto) + badges + separador + footer (fecha/completado)
- **Menú tres puntos por tarjeta**: `menuAbiertoId: number | null`, `toggleMenu()`, `copiarEnlace()`; `@HostListener('document:click')` + `ElementRef` para cerrar al hacer clic fuera
- `EstadoBadgeComponent` con `size="sm"`; `TicketRibbonComponent` con `tipo="ACT"`
- Tarjeta completada (INTEGRADO): clase `.act-card-done` (opacidad 0.55, fondo $surface, hover deshabilitado)
- Animación de entrada: `mac-fade-in` (opacity + translateY 4px, 200ms)

## Vista "Detalle de Proyecto" (`/pro/:id`) — diseño actualizado
- Componente: `src/app/features/pro/detalle/proyecto-detalle.component`
- Tarjeta principal (`proyecto-card`) con secciones: identidad (avatar + nombre + badges), progreso global, acciones (Ver tickets, Editar)
- Badges: `app-estado-badge` para estado, badge de categoría, badge de rol del usuario actual
- `rolLabel()` y `rolColorClass()` computed según el rol del usuario en el proyecto
- `puedeEditar()` computed — muestra botón editar solo si tiene permisos
- `irATickets(tipo)` navega a `/pro/:id/tickets` con query param `?tipo=<código>` opcional
