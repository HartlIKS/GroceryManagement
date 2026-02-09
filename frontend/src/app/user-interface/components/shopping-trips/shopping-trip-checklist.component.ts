import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CommonModule } from '@angular/common';
import { ShoppingTripService } from '../../services';
import { PriceService, ProductService, StoreService } from '../../../master-data/services';

interface ChecklistItem {
  productUuid: string;
  productName: string;
  productImage?: string;
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
    MatIconModule,
    MatCardModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
  ],
  templateUrl: './shopping-trip-checklist.component.html',
  styleUrls: ['./shopping-trip-checklist.component.css']
})
export class ShoppingTripChecklistComponent {
  shoppingTripUuid = signal<string>('');

  // Create HTTP resources
  private readonly shoppingTripResource = inject(ShoppingTripService).getShoppingTrip(this.shoppingTripUuid);
  private readonly storesResource = inject(StoreService).getStores('', 0, 1000);
  private readonly productsResource = inject(ProductService).getProducts('', 0, 1000);
  private readonly priceService = inject(PriceService);

  // Computed properties from resources
  public readonly loading = computed(() => this.shoppingTripResource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.shoppingTripResource.status();
    return status === 'error' ? 'Failed to load shopping trip' : null;
  });
  public readonly availableStores = computed(() => this.storesResource.value()?.content ?? []);
  public readonly availableProducts = computed(() => this.productsResource.value()?.content ?? []);

  // Current shopping trip
  public readonly currentShoppingTrip = computed(() => {
    return this.shoppingTripResource.value();
  });

  // Current store for the trip
  public readonly currentStore = computed(() => {
    const trip = this.currentShoppingTrip();
    if (!trip) return null;
    return this.availableStores().find(s => s.uuid === trip.store);
  });

  // Prices for the current shopping trip (using the efficient nested structure)
  public readonly currentTripPrices = this.priceService.searchPricesForProductsAndStores(
    computed(() => Object.keys(this.currentShoppingTrip()?.products ?? {})),
    computed(() => {
      const store = this.currentShoppingTrip()?.store;
      return store ? [store] : [];
    }),
    computed(() => new Date(this.currentShoppingTrip()?.time!)),
  );

  // Checklist items
  public readonly checklistItems = computed(() => {
    const trip = this.currentShoppingTrip();
    const products = this.availableProducts();
    const priceMap = this.currentTripPrices.value();

    if (!trip || !products.length) return [];

    return Object.entries(trip.products).map(([productUuid, quantity]) => {
      const product = products.find(p => p.uuid === productUuid);

      // Efficient lookup using the nested Record structure with optional chaining
      const price = priceMap?.[productUuid]?.[trip.store]?.[0]?.price;

      return {
        productUuid,
        productName: product?.name ?? 'Unknown Product',
        productImage: product?.image,
        quantity,
        checked: false,
        price
      } as ChecklistItem;
    });
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

  getCheckedCount(): number {
    return this.checklistItems().filter(item => item.checked).length;
  }

  getTotalCount(): number {
    return this.checklistItems().length;
  }

  getTotalPrice(): number {
    return this.checklistItems().reduce((total, item) => {
      return item.price ? total + (item.price * item.quantity) : total;
    }, 0);
  }

  getHasUnknownPrices(): boolean {
    return this.checklistItems().some(item => !item.price);
  }

  getCompletionPercentage(): number {
    const total = this.getTotalCount();
    if (total === 0) return 0;
    return Math.round((this.getCheckedCount() / total) * 100);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
