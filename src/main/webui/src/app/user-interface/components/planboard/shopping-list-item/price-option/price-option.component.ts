import { Component, computed, inject, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Price } from '../../../../../master-data/models';
import { StoreService } from '../../../../../master-data/services';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-price-option',
  standalone: true,
  imports: [
    CurrencyPipe,
    MatProgressSpinner
  ],
  templateUrl: './price-option.component.html',
  styleUrl: './price-option.component.css',
})
export class PriceOptionComponent {
  readonly priceEntry = input.required<{
    price: Price,
    tripId: number,
    newTrip: boolean,
  }>();

  protected readonly storeResource = inject(StoreService).get(_ => this.priceEntry().price.store);

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
