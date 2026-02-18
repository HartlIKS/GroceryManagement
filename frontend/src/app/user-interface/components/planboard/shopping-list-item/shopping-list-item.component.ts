import { Component, computed, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { Price } from '../../../../master-data/models';
import { PriceService, ProductService } from '../../../../master-data/services';
import { PlannedTrip, ProductGroupService, ShoppingListItem } from '../../../services';
import { MatListItem, MatListItemMeta } from '@angular/material/list';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { PriceOptionComponent } from './price-option/price-option.component';

export type PriceEntrySelectionEvent = {
  price: Price,
  tripId?: number,
} | {
  price?: never,
  tripId?: never,
}

@Component({
  selector: 'app-shopping-list-item',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatBadgeModule,
    MatListItem,
    MatProgressSpinner,
    PriceOptionComponent,
    MatListItemMeta,
  ],
  templateUrl: './shopping-list-item.component.html',
  styleUrls: ['./shopping-list-item.component.css']
})
export class ShoppingListItemComponent {
  // Signal inputs
  readonly item = input.required<ShoppingListItem>();
  readonly itemType = computed(() => this.item().type);
  readonly plannedTrips = input<Record<number, PlannedTrip>>({});

  // Signal outputs
  readonly priceEntrySelected = output<PriceEntrySelectionEvent>();

  // Services
  private readonly productService = inject(ProductService);
  private readonly productGroupService = inject(ProductGroupService);
  private readonly priceService = inject(PriceService);

  // Resources for data loading
  private readonly productResource = this.productService.getProduct(
    computed(() => this.item().productUuid)
  );

  private readonly productGroupResource = this.productGroupService.getProductGroup(
    computed(() => this.item().productGroupUuid)
  );

  private readonly priceResource = this.priceService.getPrices(0, 1000, undefined, computed(() => this.item().productUuid));

  // Computed properties
  readonly product = computed(() => this.productResource.value());
  readonly productGroup = computed(() => this.productGroupResource.value());
  private readonly consideredProducts = computed(() => {
    switch(this.itemType()) {
      case "product":
        return [this.product()?.uuid].filter((a): a is string => !!a);
      case "productGroup":
        return Object.keys(this.productGroup()?.products ?? {});
    }
  })
  private readonly prices = computed(() => this.priceResource.value()?.content ?? []);
  readonly loading = computed(() =>
    (this.productResource.status() === 'loading') ||
    (this.productGroupResource.status() === 'loading')
  );

  readonly availablePriceEntries = computed(() => {
    const trips = Object.entries(this.plannedTrips());
    const products = this.consideredProducts();
    return this.prices()
      .flatMap((price) => {
        if(!products.includes(price.product)) return [];
        const overlaps = trips
          .flatMap(([tripId, { storeUuid, validFrom, validTo }]) => {
            if (price.store === storeUuid && (
              new Date(price.validFrom) < new Date(validTo) ||
              new Date(validFrom) < new Date(price.validTo)
            )) {
              return [{
                price,
                tripId: Number(tripId),
                newTrip: false,
              }];
            }
            return [];
          });
        if (overlaps.length) return overlaps;
        return [{
          price,
          tripId: Math.max(-1, ...trips.map(([v]) => Number(v)))+1,
          newTrip: true,
        }];
      })
      .sort((a, b) => a.price.price - b.price.price);
  });

  readonly itemName = computed(() => {
    switch (this.itemType()) {
      case "product":
        return this.product()?.name
      case "productGroup":
        return this.productGroup()?.name
    }
  })

  readonly itemImage = computed(() => {
    switch (this.itemType()) {
      case "product":
        return this.product()?.image;
      case "productGroup":
        return undefined;
    }
  })

  readonly selectedPriceEntry = computed(() => {
    const prices = this.availablePriceEntries();
    const selected = this.item().assignment;
    if (!selected) return undefined;
    const index = prices.findIndex(({price, tripId}) =>
      price.uuid === selected.price.uuid && (tripId === selected.tripId)
    );
    console.log(index);
    if (index === -1) return undefined;
    return index;
  });
}
