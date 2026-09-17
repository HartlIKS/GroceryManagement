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
  protected readonly productsResource = inject(ProductService).search(_ => ({
    name: '',
    page: 0,
    size: 1,
  }));
  protected readonly storesResource = inject(StoreService).search(_ => ({
    name: '',
    page: 0,
    size: 1,
  }));
  protected readonly pricesResource = inject(PriceService).search(_ => ({
    page: 0,
    size: 1,
  }));
}
