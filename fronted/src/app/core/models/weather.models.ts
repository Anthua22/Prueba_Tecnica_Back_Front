export interface Municipio {
  codigo: string;
  nombre: string;
}

export type TemperatureUnit = 'G_CEL' | 'G_FAH';

export interface PrecipitationProbability {
  probabilidad: number;
  periodo: string;
}

export interface Forecast {
  mediaTemperatura: number;
  unidadTemperatura: TemperatureUnit;
  probPrecipitacion: PrecipitationProbability[];
}
