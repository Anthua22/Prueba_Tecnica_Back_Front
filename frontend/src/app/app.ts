import { Component } from '@angular/core';
import { WeatherPageComponent } from './features/weather/weather-page.component';

@Component({
  selector: 'app-root',
  imports: [WeatherPageComponent],
  template: '<app-weather-page />',
})
export class App {}
