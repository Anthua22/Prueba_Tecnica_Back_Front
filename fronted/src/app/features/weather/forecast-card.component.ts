import { DecimalPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { Forecast } from '../../core/models/weather.models';

/** Componente de presentación: solo pinta lo que recibe. */
@Component({
  selector: 'app-forecast-card',
  imports: [DecimalPipe],
  template: `
    <section class="card">
      <h2>{{ municipio() }}</h2>
      <p class="date">{{ dateLabel() }}</p>
      <p class="temp">
        {{ forecast().mediaTemperatura | number: '1.0-0' }}<span class="unit">{{ symbol() }}</span>
      </p>
      <ul class="rain">
        @for (p of forecast().probPrecipitacion; track p.periodo) {
          <li>
            <span class="prob">{{ p.probabilidad }}%</span>
            <span class="period">{{ p.periodo }}</span>
          </li>
        }
      </ul>
    </section>
  `,
  styles: `
    .card { padding: 8px 4px; }
    h2 { margin: 0; font-weight: 400; font-size: 28px; }
    .date { margin: 4px 0 16px; color: #666; font-size: 14px; }
    .temp { margin: 0 0 24px; font-size: 72px; font-weight: 300; color: #555; }
    .unit { font-size: 32px; vertical-align: top; }
    .rain { display: flex; justify-content: space-between; list-style: none; margin: 0; padding: 0;
            border-bottom: 1px solid #ddd; }
    .rain li { display: flex; flex-direction: column; align-items: center; padding-bottom: 8px; }
    .prob { font-size: 20px; color: #666; }
    .period { font-size: 12px; color: #333; }
  `,
})
export class ForecastCardComponent {
  readonly municipio = input.required<string>();
  readonly forecast = input.required<Forecast>();

  protected readonly symbol = computed(() =>
    this.forecast().unidadTemperatura === 'G_FAH' ? '°F' : '°C',
  );

  /** La previsión es siempre la del día siguiente. */
  protected readonly dateLabel = computed(() => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const text = tomorrow.toLocaleDateString('es-ES', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
    return text.charAt(0).toUpperCase() + text.slice(1);
  });
}
