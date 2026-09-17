import { Component, inject, resource, signal } from '@angular/core';
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
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { ProductListingComponent } from '../../../user-interface/components/product-listing/product-listing.component';
import { StoreListingComponent } from '../../../user-interface/components/store-listing/store-listing.component';

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
    FormsModule,
    NgOptimizedImage,
    ProductListingComponent,
    StoreListingComponent
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
  protected readonly productsResource = this.productService.search(_ => ({
    name: '',
    page: 0,
    size: Number.MAX_SAFE_INTEGER,
  }));
  protected readonly storesResource = this.storeService.search(_ => ({
    name: '',
    page: 0,
    size: Number.MAX_SAFE_INTEGER,
  }));
  protected readonly pricesResource = this.priceService.search(_ => ({
    page: this.currentPage(),
    size: this.pageSize(),
    store: this.selectedStore(),
    product: this.selectedProduct(),
  }));

  public readonly dataSource = resource({
    params: ({ chain }) => {
      const prices = chain(this.pricesResource)?.content;
      if(!prices) return undefined;
      return {
        prices,
        referenceTimestamp: new Date(this.referenceTimestamp()),
      }
    },
    loader: async ({ params }) => {
      const {prices, referenceTimestamp} = params;
      const pricesWithValidity = prices.map(price => {
        const status = PriceListComponent.getPriceValidityStatus(price, referenceTimestamp);
        return {
          ...price,
          validityStatus: status,
          validityClass: `price-${status}`
        } as PriceWithValidity;
      });
      return new MatTableDataSource<PriceWithValidity>(pricesWithValidity);
    }
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

  // Price validity status methods
  private static getPriceValidityStatus(price: ListPriceDTO, referenceTimestamp: Date): 'past' | 'current' | 'future' {
    const referenceTime = referenceTimestamp.getTime();
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
