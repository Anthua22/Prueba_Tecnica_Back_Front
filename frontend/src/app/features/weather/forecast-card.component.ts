import { DecimalPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { Forecast } from '../../core/models/weather.models';
import { MatIconModule } from '@angular/material/icon';

/** Componente de presentación: solo pinta lo que recibe. */
@Component({
  selector: 'app-forecast-card',
  imports: [DecimalPipe, MatIconModule],
  template: `
    <section class="card">
      <h2>{{ municipio() }}</h2>
      <p class="date">{{ dateLabel() }}</p>
      <div class="temp-row">
        <p class="temp">
          {{ forecast().mediaTemperatura | number: '1.0-0'
          }}<span class="unit">{{ symbol() }}</span>
        </p>
        <mat-icon
          class="sky-icon"
          [style.color]="sky().color"
          [attr.aria-label]="sky().label"
          [attr.title]="sky().label"
          >{{ sky().icon }}</mat-icon
        >
      </div>

      <div class="rain-row">
        <mat-icon class="rain-icon" aria-label="Probabilidad de lluvia">water_drop</mat-icon>
        <ul class="rain">
          @for (p of forecast().probPrecipitacion; track p.periodo) {
            <li>
              <span class="prob">{{ p.probabilidad }}%</span>
              <span class="period">{{ p.periodo }}</span>
            </li>
          }
        </ul>
      </div>
    </section>
  `,
  styles: `
    .card {
      padding: 8px 4px;
    }
    h2 {
      margin: 0;
      font-weight: 400;
      font-size: 28px;
    }
    .date {
      margin: 4px 0 16px;
      color: #666;
      font-size: 14px;
    }
    .temp {
      margin: 0 0 24px;
      font-size: 72px;
      font-weight: 300;
      color: #555;
    }
    .unit {
      font-size: 32px;
      vertical-align: top;
    }
    .rain {
      display: flex;
      justify-content: space-between;
      list-style: none;
      margin: 0;
      padding: 0;
      border-bottom: 1px solid #ddd;
    }
    .rain li {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding-bottom: 8px;
    }
    .prob {
      font-size: 20px;
      color: #666;
    }
    .period {
      font-size: 12px;
      color: #333;
    }
    .rain-row {
      display: flex;
      align-items: flex-start;
      gap: 12px;
    }
    .rain-icon {
      color: #1e88e5;
      font-size: 32px;
      width: 32px;
      height: 32px;
    }
    .rain {
      flex: 1; /* mantén el resto de propiedades que ya tenía */
    }
    .temp {
      margin: 0;
      font-size: 72px;
      font-weight: 300;
      color: #555;
    }
    .temp-row {
      display: flex;
      align-items: center;
      gap: 20px;
      margin-bottom: 24px;
    }
    .sky-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
    }
  `,
})
export class ForecastCardComponent {
  readonly municipio = input.required<string>();
  readonly forecast = input.required<Forecast>();

  protected readonly symbol = computed(() =>
    this.forecast().unidadTemperatura === 'G_FAH' ? '°F' : '°C',
  );

  protected readonly sky = computed(() => {
    const maxRain = Math.max(0, ...this.forecast().probPrecipitacion.map((p) => p.probabilidad));
    if (maxRain >= 50) return { icon: 'rainy', label: 'Lluvia', color: '#1e88e5' };
    if (maxRain >= 20) return { icon: 'cloud', label: 'Nublado', color: '#78909c' };
    return { icon: 'wb_sunny', label: 'Soleado', color: '#f9a825' };
  });

  /** La previsión es siempre la del día siguiente. */
  protected readonly dateLabel = computed(() => {
    const hoy = new Date();
    const text = hoy.toLocaleDateString('es-ES', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
    return text.charAt(0).toUpperCase() + text.slice(1);
  });
}
