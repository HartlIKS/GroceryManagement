import { Component, computed, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { ProductGroupService, ShoppingListService, ShoppingTripService } from '../services';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatCardModule, MatProgressSpinnerModule, RouterLink],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.css']
})
export class UserDashboardComponent implements OnInit {
  constructor(
    private productGroupService: ProductGroupService,
    private shoppingListService: ShoppingListService,
    private shoppingTripService: ShoppingTripService
  ) {}

  // Computed properties for product groups
  readonly totalGroups = computed(() => this.productGroupService.productGroups().length);

  readonly totalProductsInGroups = computed(() => {
    return this.productGroupService.productGroups()
      .reduce((total, group) => total + (group.products ? Object.keys(group.products).length : 0), 0);
  });

  // Computed properties for shopping lists
  readonly totalShoppingLists = computed(() => this.shoppingListService.shoppingLists().length);

  // Computed properties for shopping trips
  readonly totalShoppingTrips = computed(() => this.shoppingTripService.shoppingTrips().length);

  readonly loading = computed(() => 
    this.productGroupService.loading() || this.shoppingListService.loading() || this.shoppingTripService.loading()
  );

  ngOnInit() {
    // Load data when dashboard initializes
    this.productGroupService.getProductGroups();
    this.shoppingListService.getShoppingLists();
    this.shoppingTripService.getShoppingTrips();
  }
}
