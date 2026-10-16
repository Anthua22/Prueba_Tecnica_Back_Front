import { Component, computed, effect, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { Observable, catchError, map, of, startWith, switchMap } from 'rxjs';
import { Forecast, Municipio, TemperatureUnit } from '../../core/models/weather.models';
import { PreferencesStorage } from '../../core/services/preferences-storage.service';
import { WeatherApiService } from '../../core/services/weather-api.service';
import { ForecastCardComponent } from './forecast-card.component';
import { MunicipioAutocompleteComponent } from './municipio-autocomplete.component';

type ViewState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'error' }
  | { status: 'ready'; forecast: Forecast };

/**
 * Componente contenedor: mantiene el estado (municipio y unidad) y decide cuándo pedir la previsión.
 * Cualquier cambio en municipio o unidad provoca una nueva petición.
 */
@Component({
  selector: 'app-weather-page',
  imports: [
    MunicipioAutocompleteComponent,
    ForecastCardComponent,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <main class="page">
      <div class="filters">
        <app-municipio-autocomplete
          class="municipio"
          [initialValue]="initialMunicipio"
          (selected)="municipio.set($event)"
        />
        <mat-form-field appearance="outline" class="unit">
          <mat-label>Unidad</mat-label>
          <mat-select [value]="unit()" (selectionChange)="unit.set($event.value)">
            <mat-option [value]="null">Por defecto (°C)</mat-option>
            <mat-option value="G_CEL">°C</mat-option>
            <mat-option value="G_FAH">°F</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      @let s = state();
      @if (s.status === 'idle') {
        <p class="hint">Busca un municipio para ver la previsión de mañana.</p>
      } @else if (s.status === 'loading') {
        <mat-spinner diameter="36" />
      } @else if (s.status === 'error') {
        <p class="error">No se pudo cargar la previsión. Inténtalo de nuevo en unos segundos.</p>
      } @else if (s.status === 'ready' && municipio(); as m) {
        <app-forecast-card [municipio]="m.nombre" [forecast]="s.forecast" />
      }
    </main>
  `,
  styles: `
    .page { max-width: 420px; margin: 24px auto; padding: 0 16px; }
    .filters { display: flex; gap: 12px; }
    .municipio { flex: 1; }
    .unit { width: 150px; }
    .hint { color: #666; }
    .error { color: #b00020; }
  `,
})
export class WeatherPageComponent {
  private readonly api = inject(WeatherApiService);
  private readonly preferences = inject(PreferencesStorage);
  private readonly saved = this.preferences.load();

  protected readonly initialMunicipio = this.saved.municipio;
  protected readonly municipio = signal<Municipio | null>(this.saved.municipio);
  protected readonly unit = signal<TemperatureUnit | null>(this.saved.unit);

  protected readonly state = toSignal(
    toObservable(computed(() => ({ municipio: this.municipio(), unit: this.unit() }))).pipe(
      switchMap(({ municipio, unit }) =>
        municipio ? this.loadForecast(municipio, unit) : of<ViewState>({ status: 'idle' }),
      ),
    ),
    { initialValue: { status: 'idle' } as ViewState },
  );

  constructor() {
    // Recordar la última selección: al volver, se carga la previsión automáticamente.
    effect(() => this.preferences.save({ municipio: this.municipio(), unit: this.unit() }));
  }

  private loadForecast(municipio: Municipio, unit: TemperatureUnit | null): Observable<ViewState> {
    return this.api.getTomorrowForecast(municipio.codigo, unit).pipe(
      map((forecast): ViewState => ({ status: 'ready', forecast })),
      startWith<ViewState>({ status: 'loading' }),
      catchError(() => of<ViewState>({ status: 'error' })),
    );
  }
}
