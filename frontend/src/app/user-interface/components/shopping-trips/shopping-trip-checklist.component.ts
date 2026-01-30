import { Component, computed, effect, OnInit, signal } from '@angular/core';
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
import { PriceListingDTO } from '../../../master-data/models';

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
export class ShoppingTripChecklistComponent implements OnInit {
  shoppingTripUuid: string | null = null;

  // Use computed signals from services
  public readonly loading = computed(() => this.shoppingTripService.loading());
  public readonly error = computed(() => this.shoppingTripService.error());
  public readonly availableStores = computed(() => this.storeService.stores());
  public readonly availableProducts = computed(() => this.productService.products());

  // Current shopping trip
  public readonly currentShoppingTrip = computed(() => {
    if (!this.shoppingTripUuid) return null;
    return this.shoppingTripService.getShoppingTripByUuid(this.shoppingTripUuid);
  });

  // Current store for the trip
  public readonly currentStore = computed(() => {
    const trip = this.currentShoppingTrip();
    if (!trip) return null;
    return this.availableStores().find(s => s.uuid === trip.store);
  });

  // Prices for the current shopping trip (using the efficient nested structure)
  public readonly currentTripPrices = signal<Record<string, Record<string, PriceListingDTO[]>> | null>(null);

  // Checklist items
  public readonly checklistItems = computed(() => {
    const trip = this.currentShoppingTrip();
    const products = this.availableProducts();
    const priceMap = this.currentTripPrices();

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
    private route: ActivatedRoute,
    private shoppingTripService: ShoppingTripService,
    private storeService: StoreService,
    private productService: ProductService,
    private priceService: PriceService
  ) {
    // Effect to automatically fetch prices when the current shopping trip changes
    effect(() => {
      const trip = this.currentShoppingTrip();
      if (!trip) {
        this.currentTripPrices.set(null);
        return;
      }

      // Get all product UUIDs from the shopping trip
      const productUuids = Object.keys(trip.products);
      if (productUuids.length === 0) {
        this.currentTripPrices.set({});
        return;
      }

      // Fetch prices for this trip's store and products
      this.priceService.searchPricesForProductsAndStores(productUuids, trip.store, trip.time).subscribe({
        next: (priceMap) => {
          this.currentTripPrices.set(priceMap);
        },
        error: (error) => {
          console.error('Error loading prices for store:', error);
          this.currentTripPrices.set(null);
        }
      });
    });
  }

  ngOnInit(): void {
    this.shoppingTripUuid = this.route.snapshot.paramMap.get('id');

    if (this.shoppingTripUuid) {
      this.loadShoppingTrip();
    }

    // Load stores and products for display
    this.storeService.getStores();
    this.productService.getProducts('', 0, 1000); // Load all products for mapping
  }

  loadShoppingTrip(): void {
    if (!this.shoppingTripUuid) return;

    // First try to get from cache
    const cachedTrip = this.shoppingTripService.getShoppingTripByUuid(this.shoppingTripUuid);
    if (cachedTrip) {
      return; // Already loaded - effect will handle price fetching
    }

    // If not in cache, load individual trip
    this.shoppingTripService.getShoppingTrip(this.shoppingTripUuid).subscribe({
      next: (trip) => {
        // Add the loaded trip to cache so it becomes available
        this.shoppingTripService.addShoppingTripToCache(trip);
        // Effect will automatically fetch prices when trip is available
      },
      error: (error) => {
        console.error('Error loading shopping trip:', error);
      }
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
