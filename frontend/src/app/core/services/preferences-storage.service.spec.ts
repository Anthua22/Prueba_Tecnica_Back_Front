import { TestBed } from '@angular/core/testing';
import { PreferencesStorage } from './preferences-storage.service';

describe('PreferencesStorage', () => {
  let storage: PreferencesStorage;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    storage = TestBed.inject(PreferencesStorage);
  });

  it('returns empty preferences when nothing was saved', () => {
    expect(storage.load()).toEqual({ municipio: null, unit: null });
  });

  it('restores what was saved', () => {
    const prefs = { municipio: { codigo: '03002', nombre: 'Agost' }, unit: 'G_FAH' as const };
    storage.save(prefs);
    expect(storage.load()).toEqual(prefs);
  });

  it('ignores corrupt content', () => {
    localStorage.setItem('weather.preferences.v1', '{no-json');
    expect(storage.load()).toEqual({ municipio: null, unit: null });
  });
});
