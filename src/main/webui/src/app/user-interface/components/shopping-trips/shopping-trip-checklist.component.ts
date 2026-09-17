import { Component, inject, resource, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatProgressBar } from '@angular/material/progress-bar';
import { CommonModule } from '@angular/common';
import { ShoppingTripService } from '../../services';
import { PriceService, StoreService } from '../../../master-data/services';
import { ProductListingComponent } from '../product-listing/product-listing.component';

interface ChecklistItem {
  productUuid: string;
  quantity: number;
  checked: boolean;
  price?: number;
}

@Component({
  selector: 'app-shopping-trip-checklist',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatCheckbox,
    MatProgressSpinner,
    MatProgressBar,
    ProductListingComponent,
  ],
  templateUrl: './shopping-trip-checklist.component.html',
  styleUrls: ['./shopping-trip-checklist.component.css']
})
export class ShoppingTripChecklistComponent {
  shoppingTripUuid = signal<string>('');

  // Create HTTP resources
  private readonly shoppingTripResource = inject(ShoppingTripService).get(this.shoppingTripUuid);
  protected readonly currentStore = inject(StoreService)
    .get(({chain}) => chain(this.shoppingTripResource)?.store);
  private readonly priceService = inject(PriceService);

  // Prices for the current shopping trip (using the efficient nested structure)
  private readonly currentTripPrices = this.priceService.search(({chain}) => {
    const trip = chain(this.shoppingTripResource);
    if(!trip) return undefined;
    return {
      products: Object.keys(trip.products),
      stores: [trip.store],
      at: new Date(trip.time),
    };
  });

  public readonly combinedResource = resource({
    params: ({ chain }) => {
      const trip = chain(this.shoppingTripResource);
      if (!trip) return undefined;
      const store = chain(this.currentStore);
      if(!store) return undefined;
      const priceMap = chain(this.currentTripPrices);
      if (!priceMap) return undefined;
      return { trip, store, priceMap };
    },
    async loader({ params }) {
      const {trip, store, priceMap} = params;
      return {
        trip,
        store,
        items: Object.entries(trip.products).map(([productUuid, quantity]) => {
          const price = priceMap?.[productUuid]?.[trip.store]?.[0]?.price;
          return {
            productUuid,
            quantity,
            checked: false,
            price
          } as ChecklistItem;
        }),
      };
    }
  });

  constructor(
    private route: ActivatedRoute
  ) {
    // Set up route parameter subscription
    this.route.params.subscribe(({id}) => {
      this.shoppingTripUuid.set(id);
    });

  }

  toggleItem(item: ChecklistItem): void {
    item.checked = !item.checked;
  }

  getCheckedItemCount(items: ChecklistItem[]): number {
    return items.reduce((total, item) => total + (item.checked ? 1 : 0), 0);
  }

  getTotalPrice(items: ChecklistItem[]): number {
    return items.reduce((total, item) => {
      return item.price ? total + (item.price * item.quantity) : total;
    }, 0);
  }

  getHasUnknownPrices(items: ChecklistItem[]): boolean {
    return items.some(item => !item.price);
  }

  getCompletionPercentage(items: ChecklistItem[]): number {
    const total = items.length;
    if (total === 0) return 0;
    return Math.round((this.getCheckedItemCount(items) / total) * 100);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
