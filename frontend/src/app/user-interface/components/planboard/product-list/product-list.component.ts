import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { ProductListingComponent } from '../../product-listing/product-listing.component';

@Component({
  selector: 'app-trip-product-display',
  standalone: true,
  imports: [
    CommonModule,
    MatListModule,
    ProductListingComponent,
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent {
  readonly products = input.required<Record<string, number>>();

  readonly productEntries = computed(() =>
    Object.entries(this.products()).map(([priceUuid, quantity]) => ({
      priceUuid,
      quantity
    }))
  );
}
