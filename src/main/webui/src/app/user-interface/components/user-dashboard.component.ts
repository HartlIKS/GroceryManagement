import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ProductGroupService, ShoppingListService, ShoppingTripService } from '../services';
import { RouterLink } from '@angular/router';
import { ProductGroup } from '../models';
import { LIST } from '../../models/base.model';

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
  protected readonly productGroupsResource = inject(ProductGroupService).search(_ => ({
    name: '',
    page: 0,
    size: Number.MAX_SAFE_INTEGER,
  }));
  protected readonly shoppingListsResource = inject(ShoppingListService).search(_ => ({
    name: '',
    page: 0,
    size: 1,
  }));
  protected readonly shoppingTripsResource = inject(ShoppingTripService).search(_ => ({
    page: 0,
    size: 1,
  }));

  protected totalProductsInGroups(groups: ProductGroup<LIST>[]) {
    return groups.reduce((total, group) => total + (group.products ? Object.keys(group.products).length : 0), 0);
  };
}
