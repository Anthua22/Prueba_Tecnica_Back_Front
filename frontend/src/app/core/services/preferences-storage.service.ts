import { Injectable } from '@angular/core';
import { Municipio, TemperatureUnit } from '../models/weather.models';

export interface Preferences {
  municipio: Municipio | null;
  unit: TemperatureUnit | null;
}

const STORAGE_KEY = 'weather.preferences.v1';
const EMPTY: Preferences = { municipio: null, unit: null };

/** Recuerda el último municipio y unidad elegidos en este navegador. */
@Injectable({ providedIn: 'root' })
export class PreferencesStorage {
  load(): Preferences {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? { ...EMPTY, ...JSON.parse(raw) } : EMPTY;
    } catch {
      return EMPTY; // almacenamiento no disponible o contenido corrupto
    }
  }

  save(preferences: Preferences): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences));
    } catch {
      // modo privado / cuota: no es crítico
    }
  }
}
