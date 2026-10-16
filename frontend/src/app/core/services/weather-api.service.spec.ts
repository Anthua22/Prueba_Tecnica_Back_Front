import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { WeatherApiService } from './weather-api.service';

describe('WeatherApiService', () => {
  let service: WeatherApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(WeatherApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('searches municipios by prefix', () => {
    service.searchMunicipios('Ag').subscribe((r) => expect(r.length).toBe(1));

    const req = http.expectOne((r) => r.url.endsWith('/api/municipios'));
    expect(req.request.params.get('nombre')).toBe('Ag');
    req.flush([{ codigo: '03002', nombre: 'Agost' }]);
  });

  it('sends the unit only when one is selected', () => {
    service.getTomorrowForecast('03002', 'G_FAH').subscribe();
    const withUnit = http.expectOne((r) => r.url.endsWith('/api/municipios/03002/prediccion'));
    expect(withUnit.request.params.get('unidadTemperatura')).toBe('G_FAH');
    withUnit.flush({});

    service.getTomorrowForecast('03002', null).subscribe();
    const withoutUnit = http.expectOne((r) => r.url.endsWith('/api/municipios/03002/prediccion'));
    expect(withoutUnit.request.params.has('unidadTemperatura')).toBe(false);
    withoutUnit.flush({});
  });
});
