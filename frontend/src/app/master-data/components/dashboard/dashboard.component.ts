import { Component, computed, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { PriceService, ProductService, StoreService } from '../../services';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  // Use computed signals from services
  public readonly products = computed(() => this.productService.products());
  public readonly stores = computed(() => this.storeService.stores());
  public readonly prices = computed(() => this.priceService.prices());
  public readonly loading = computed(() =>
    this.productService.loading() || this.storeService.loading() || this.priceService.loading()
  );

  // Computed values for dashboard
  public readonly totalProducts = computed(() => this.productService.totalElements());
  public readonly totalStores = computed(() => this.storeService.totalElements());
  public readonly totalPrices = computed(() => this.priceService.totalElements());

  constructor(
    private productService: ProductService,
    private storeService: StoreService,
    private priceService: PriceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    // Clear caches to get fresh data
    this.productService.clearCache();
    this.storeService.clearCache();
    this.priceService.clearCache();

    // Load products
    this.productService.getProducts('', 0, 1);

    // Load stores
    this.storeService.getStores('', 0, 1);

    // Load prices
    this.priceService.getPrices(0, 1);
  }

  navigateToProducts(): void {
    this.router.navigate(['/products']);
  }

  navigateToStores(): void {
    this.router.navigate(['/stores']);
  }

  navigateToPrices(): void {
    this.router.navigate(['/prices']);
  }

  addProduct(): void {
    this.router.navigate(['/products/new']);
  }

  addStore(): void {
    this.router.navigate(['/stores/new']);
  }

  addPrice(): void {
    this.router.navigate(['/prices/new']);
  }
}
