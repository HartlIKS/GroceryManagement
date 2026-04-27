import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { PriceService, ProductService, StoreService } from '../../services';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIcon, MatProgressSpinner, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  // Create HTTP resources for dashboard data
  private readonly productsResource = inject(ProductService).search('', 0, 1);
  private readonly storesResource = inject(StoreService).search('', 0, 1);
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
