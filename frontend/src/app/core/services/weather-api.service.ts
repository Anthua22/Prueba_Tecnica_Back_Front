import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Forecast, Municipio, TemperatureUnit } from '../models/weather.models';
import { environment } from '../../../environments/environment.development';

const API_URL = environment.apiUrl;

/** Único punto del frontend que conoce las URLs del backend. */
@Injectable({ providedIn: 'root' })
export class WeatherApiService {
  private readonly http = inject(HttpClient);

  searchMunicipios(prefix: string): Observable<Municipio[]> {
    return this.http.get<Municipio[]>(`${API_URL}/municipios`, { params: { nombre: prefix } });
  }

  getTomorrowForecast(codigo: string, unit: TemperatureUnit | null): Observable<Forecast> {
    let params = new HttpParams();
    if (unit) {
      params = params.set('unidadTemperatura', unit);
    }
    return this.http.get<Forecast>(`${API_URL}/municipios/${codigo}/prediccion`, { params });
  }
}
