package com.example.weather.domain;

/** Unidades de temperatura soportadas. AEMET siempre devuelve grados Celsius. */
public enum TemperatureUnit {
    G_CEL {
        @Override
        public double fromCelsius(double celsius) {
            return celsius;
        }
    },
    G_FAH {
        @Override
        public double fromCelsius(double celsius) {
            return celsius * 9.0 / 5.0 + 32.0;
        }
    };

    public abstract double fromCelsius(double celsius);
}
