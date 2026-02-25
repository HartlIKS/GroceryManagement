import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ProductGroupService, ShoppingListService, ShoppingTripService } from '../services';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatProgressSpinner,
    RouterLink
  ],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.css']
})
export class UserDashboardComponent {
  // Create HTTP resources for dashboard data
  private readonly productGroupsResource = inject(ProductGroupService).getProductGroups('', 0, 1);
  private readonly shoppingListsResource = inject(ShoppingListService).getShoppingLists('', 0, 1);
  private readonly shoppingTripsResource = inject(ShoppingTripService).getShoppingTrips(undefined, undefined, 0, 1);

  // Computed properties from resources
  readonly productGroups = computed(() => this.productGroupsResource.value()?.content ?? []);
  readonly shoppingLists = computed(() => this.shoppingListsResource.value()?.content ?? []);
  readonly shoppingTrips = computed(() => this.shoppingTripsResource.value()?.content ?? []);

  // Computed properties for product groups
  readonly totalGroups = computed(() => this.productGroups().length);

  readonly totalProductsInGroups = computed(() => {
    return this.productGroups()
      .reduce((total, group) => total + (group.products ? Object.keys(group.products).length : 0), 0);
  });

  // Computed properties for shopping lists
  readonly totalShoppingLists = computed(() => this.shoppingLists().length);

  // Computed properties for shopping trips
  readonly totalShoppingTrips = computed(() => this.shoppingTrips().length);

  readonly loading = computed(() =>
    this.productGroupsResource.status() === 'loading' ||
    this.shoppingListsResource.status() === 'loading' ||
    this.shoppingTripsResource.status() === 'loading'
  );
}
