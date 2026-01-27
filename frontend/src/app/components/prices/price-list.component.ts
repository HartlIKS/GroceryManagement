import { Component, computed, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { PriceService, ProductService, StoreService } from '../../services';
import { ListPriceDTO } from '../../models';

@Component({
  selector: 'app-price-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './price-list.component.html',
  styleUrls: ['./price-list.component.css']
})
export class PriceListComponent implements OnInit {
  displayedColumns: string[] = ['product', 'store', 'price', 'validFrom', 'validTo', 'actions'];

  // Use computed signals from services
  public readonly products = computed(() => this.productService.products());
  public readonly stores = computed(() => this.storeService.stores());
  public readonly prices = computed(() => this.priceService.prices());
  public readonly totalElements = computed(() => this.priceService.totalElements());
  public readonly currentPage = computed(() => this.priceService.currentPage());
  public readonly pageSize = computed(() => this.priceService.pageSize());
  public readonly loading = computed(() => this.priceService.loading());
  public readonly error = computed(() => this.priceService.error());

  // Create MatTableDataSource from prices signal
  public readonly dataSource = computed(() => {
    const prices = this.prices();
    return new MatTableDataSource<ListPriceDTO>(prices);
  });

  constructor(
    private priceService: PriceService,
    private productService: ProductService,
    private storeService: StoreService,
    private router: Router
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

  onAddPrice(): void {
    this.router.navigate(['/prices/new']);
  }

  onEditPrice(uuid: string): void {
    this.router.navigate(['/prices', uuid]);
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
    const product = this.products().find(p => p.uuid === productUuid);
    return product ? product.name : 'Unknown Product';
  }

  getStoreName(storeUuid: string): string {
    const store = this.stores().find(s => s.uuid === storeUuid);
    return store ? store.name : 'Unknown Store';
  }
}
