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

  private readonly storeResource = inject(StoreService).getStore(computed(() => this.priceEntry().price.store))

  protected readonly store = computed(() => this.storeResource.value());

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
