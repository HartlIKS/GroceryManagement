import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatPaginator } from '@angular/material/paginator';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { PriceService, ProductService, StoreService } from '../../services';
import { ListPriceDTO } from '../../models';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

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
    MatIcon,
    MatPaginator,
    MatProgressSpinner,
    MatSelectModule,
    MatFormFieldModule,
    MatInput,
    RouterLink,
    FormsModule
  ],
  templateUrl: './price-list.component.html',
  styleUrls: ['./price-list.component.css']
})
export class PriceListComponent {
  displayedColumns: string[] = ['product', 'store', 'price', 'validFrom', 'validTo', 'actions'];

  // Reference timestamp signal - defaults to current time
  protected readonly referenceTimestamp = signal<string>(new Date().toISOString().slice(0, 16));

  // Pagination and filter signals
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(20);
  protected readonly selectedStore = signal<string>('');
  protected readonly selectedProduct = signal<string>('');

  private readonly productService = inject(ProductService);
  private readonly storeService = inject(StoreService);
  private readonly priceService = inject(PriceService);

  // Create HTTP resources
  private readonly productsResource = this.productService.getProducts('', 0, 1000);
  private readonly storesResource = this.storeService.getStores('', 0, 1000);
  protected readonly pricesResource = this.priceService.getPrices(
    this.currentPage,
    this.pageSize,
    this.selectedStore,
    this.selectedProduct,
  );

  // Computed properties from resources
  public readonly products = computed(() => this.productsResource.value()?.content ?? []);
  public readonly stores = computed(() => this.storesResource.value()?.content ?? []);
  public readonly prices = computed(() => this.pricesResource.value()?.content ?? []);
  public readonly totalElements = computed(() => this.pricesResource.value()?.page?.totalElements ?? 0);
  public readonly loading = computed(() => this.pricesResource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.pricesResource.status();
    return status === 'error' ? 'Failed to load prices' : null;
  });

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

  onPageChange(event: any): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  onStoreFilterChange(storeUuid: string): void {
    this.selectedStore.set(storeUuid === 'all' ? '' : storeUuid);
    this.currentPage.set(0);
  }

  onProductFilterChange(productUuid: string): void {
    this.selectedProduct.set(productUuid === 'all' ? '' : productUuid);
    this.currentPage.set(0);
  }

  onClearFilters(): void {
    this.selectedStore.set('');
    this.selectedProduct.set('');
    this.currentPage.set(0);
  }

  onDeletePrice(uuid: string): void {
    if (confirm('Are you sure you want to delete this price?')) {
      this.priceService.delete(uuid).subscribe({
        error: (error: any) => {
          console.error('Error deleting price:', error);
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
}
