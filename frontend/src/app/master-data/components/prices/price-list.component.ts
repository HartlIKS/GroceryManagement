import { Component, computed, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { PriceService, ProductService, StoreService } from '../../services';
import { ListPriceDTO } from '../../models';

// Enhanced price item with validity status
type PriceWithValidity = ListPriceDTO & {
  validityStatus: 'past' | 'current' | 'future',
  validityClass: string,
};

@Component({
  selector: 'app-price-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink
  ],
  templateUrl: './price-list.component.html',
  styleUrls: ['./price-list.component.css']
})
export class PriceListComponent implements OnInit {
  displayedColumns: string[] = ['product', 'store', 'price', 'validFrom', 'validTo', 'actions'];

  // Reference timestamp signal - defaults to current time
  private readonly referenceTimestamp = signal<string>(new Date().toISOString().slice(0, 16));
  public readonly referenceTimestampValue = this.referenceTimestamp.asReadonly();

  // Use computed signals from services
  public readonly products = computed(() => this.productService.products());
  public readonly stores = computed(() => this.storeService.stores());
  public readonly prices = computed(() => this.priceService.prices());
  public readonly totalElements = computed(() => this.priceService.totalElements());
  public readonly currentPage = computed(() => this.priceService.currentPage());
  public readonly pageSize = computed(() => this.priceService.pageSize());
  public readonly loading = computed(() => this.priceService.loading());
  public readonly error = computed(() => this.priceService.error());
  public readonly selectedStore = computed(() => this.priceService.selectedStore());
  public readonly selectedProduct = computed(() => this.priceService.selectedProduct());

  // Create MatTableDataSource from prices signal with validity status included
  public readonly dataSource = computed(() => {
    const prices = this.prices();
    const pricesWithValidity = prices.map(price => {
      const status = this.getPriceValidityStatus(price);
      return {
        ...price,
        validityStatus: status,
        validityClass: `price-${status}`
      } as PriceWithValidity;
    });
    return new MatTableDataSource<PriceWithValidity>(pricesWithValidity);
  });

  constructor(
    private priceService: PriceService,
    private productService: ProductService,
    private storeService: StoreService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    this.loadStores();
    this.loadPrices();
  }

  loadProducts(): void {
    this.productService.getProducts('', 0, 1000);
  }

  loadStores(): void {
    this.storeService.getStores('', 0, 1000);
  }

  loadPrices(): void {
    this.priceService.getPrices();
  }

  onPageChange(event: any): void {
    this.priceService.getPrices(event.pageIndex, event.pageSize);
  }

  onStoreFilterChange(storeUuid: string): void {
    if (storeUuid === 'all') {
      this.priceService.setStoreFilter(null);
    } else {
      this.priceService.setStoreFilter(storeUuid);
    }
    this.priceService.getPrices(0, this.pageSize());
  }

  onProductFilterChange(productUuid: string): void {
    if (productUuid === 'all') {
      this.priceService.setProductFilter(null);
    } else {
      this.priceService.setProductFilter(productUuid);
    }
    this.priceService.getPrices(0, this.pageSize());
  }

  onClearFilters(): void {
    this.priceService.clearFilters();
    this.priceService.getPrices(0, this.pageSize());
  }

  onDeletePrice(uuid: string): void {
    if (confirm('Are you sure you want to delete this price?')) {
      // Optimistic update
      this.priceService.removePriceFromCache(uuid);

      this.priceService.deletePrice(uuid).subscribe({
        error: (error: any) => {
          console.error('Error deleting price:', error);
          // Refresh on error to restore state
          this.priceService.refreshPrices();
        }
      });
    }
  }

  getProductName(productUuid: string): string {
    return this.products().find(p => p.uuid === productUuid)?.name ?? 'Unknown Product';
  }

  getStoreName(storeUuid: string): string {
    return this.stores().find(s => s.uuid === storeUuid)?.name ?? 'Unknown Store';
  }

  getProductImage(productUuid: string): string | null {
    return this.products().find(p => p.uuid === productUuid)?.image ?? null;
  }

  getStoreLogo(storeUuid: string): string | null {
    return this.stores().find(s => s.uuid === storeUuid)?.logo ?? null;
  }

  // Price validity status methods
  private getPriceValidityStatus(price: ListPriceDTO): 'past' | 'current' | 'future' {
    const referenceTime = new Date(this.referenceTimestamp()).getTime();
    const validFrom = new Date(price.validFrom).getTime();
    const validTo = price.validTo ? new Date(price.validTo).getTime() : null;

    if (validTo && referenceTime > validTo) {
      return 'past';
    } else if (referenceTime >= validFrom && (!validTo || referenceTime <= validTo)) {
      return 'current';
    } else {
      return 'future';
    }
  }

  onReferenceTimestampChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.referenceTimestamp.set(input.value);
  }
}
