import { AsyncPipe } from '@angular/common';
import { Component, OnInit, inject, input, output } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { catchError, debounceTime, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import { Municipio } from '../../core/models/weather.models';
import { WeatherApiService } from '../../core/services/weather-api.service';

const MIN_CHARS = 2;

@Component({
  selector: 'app-municipio-autocomplete',
  imports: [
    AsyncPipe,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  template: `
    <mat-form-field appearance="outline" class="field">
      <mat-label>Municipio</mat-label>
      <input matInput [formControl]="control" [matAutocomplete]="auto" />
      <mat-icon matSuffix>search</mat-icon>
      <mat-autocomplete
        #auto="matAutocomplete"
        [displayWith]="display"
        (optionSelected)="selected.emit($event.option.value)"
      >
        @for (m of options$ | async; track m.codigo) {
          <mat-option [value]="m">{{ m.nombre }}</mat-option>
        }
      </mat-autocomplete>
    </mat-form-field>
  `,
  styles: '.field { width: 100%; }',
})
export class MunicipioAutocompleteComponent implements OnInit {
  private readonly api = inject(WeatherApiService);

  readonly initialValue = input<Municipio | null>(null);
  readonly selected = output<Municipio>();

  protected readonly control = new FormControl<string | Municipio>('', { nonNullable: true });

  protected readonly options$ = this.control.valueChanges.pipe(
    map((value) => (typeof value === 'string' ? value.trim() : '')),
    debounceTime(300),
    distinctUntilChanged(),
    switchMap((term) =>
      term.length >= MIN_CHARS
        ? this.api.searchMunicipios(term).pipe(catchError(() => of([] as Municipio[])))
        : of([] as Municipio[]),
    ),
  );

  protected readonly display = (m: Municipio | string | null): string =>
    typeof m === 'string' ? m : (m?.nombre ?? '');

  ngOnInit(): void {
    const initial = this.initialValue();
    if (initial) {
      this.control.setValue(initial, { emitEvent: false });
    }
  }
}
