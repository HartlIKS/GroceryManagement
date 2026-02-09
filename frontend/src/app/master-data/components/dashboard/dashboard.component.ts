import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { PriceService, ProductService, StoreService } from '../../services';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  // Create HTTP resources for dashboard data
  private readonly productsResource = inject(ProductService).getProducts('', 0, 1);
  private readonly storesResource = inject(StoreService).getStores('', 0, 1);
  private readonly pricesResource = inject(PriceService).getPrices(0, 1);

  // Computed properties from resources
  public readonly products = computed(() => this.productsResource.value()?.content ?? []);
  public readonly stores = computed(() => this.storesResource.value()?.content ?? []);
  public readonly prices = computed(() => this.pricesResource.value()?.content ?? []);
  public readonly loading = computed(() =>
    this.productsResource.status() === 'loading' ||
    this.storesResource.status() === 'loading' ||
    this.pricesResource.status() === 'loading'
  );

  // Computed values for dashboard
  public readonly totalProducts = computed(() => this.productsResource.value()?.page?.totalElements ?? 0);
  public readonly totalStores = computed(() => this.storesResource.value()?.page?.totalElements ?? 0);
  public readonly totalPrices = computed(() => this.pricesResource.value()?.page?.totalElements ?? 0);

}
