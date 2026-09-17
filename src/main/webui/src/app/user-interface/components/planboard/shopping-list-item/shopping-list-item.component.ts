import { Component, computed, inject, input, output, resource } from '@angular/core';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Price, Product } from '../../../../master-data/models';
import { PriceService, ProductService } from '../../../../master-data/services';
import { PlannedTrip, ProductGroupService, ShoppingListItem } from '../../../services';
import { MatListItem, MatListItemMeta } from '@angular/material/list';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { PriceOptionComponent } from './price-option/price-option.component';
import { httpResource } from '@angular/common/http';
import { ProductGroup } from '../../../models';
import { LIST } from '../../../../models/base.model';

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
    MatListItem,
    MatProgressSpinner,
    PriceOptionComponent,
    MatListItemMeta,
    NgOptimizedImage,
  ],
  templateUrl: './shopping-list-item.component.html',
  styleUrls: ['./shopping-list-item.component.css']
})
export class ShoppingListItemComponent {
  // Signal inputs
  readonly item = input.required<ShoppingListItem>();
  readonly plannedTrips = input<Record<number, PlannedTrip>>({});

  // Signal outputs
  readonly priceEntrySelected = output<PriceEntrySelectionEvent>();

  // Services
  private readonly productService = inject(ProductService);
  private readonly productGroupService = inject(ProductGroupService);
  private readonly priceService = inject(PriceService);

  protected readonly productOrGroupResource = httpResource<Product<LIST> | ProductGroup<LIST>>(_ => {
    const it = this.item();
    switch (it.type) {
      case "product":
        return this.productService.rawGet(it.productUuid);
      case "productGroup":
        return this.productGroupService.rawGet(it.productGroupUuid);
      default:
        throw new Error("Unknown product type");
    }
  });
  private readonly consideredProducts = resource({
    params: ({chain}) => chain(this.productOrGroupResource),
    async loader({params}) {
      if("products" in params) return Object.keys(params.products);
      return [params.uuid];
    },
    defaultValue: [],
  })

  private readonly priceResource = this.priceService.search(({chain}) => {
    const products = chain(this.consideredProducts);
    return {
      product: products,
      page: 0,
      size: Number.MAX_SAFE_INTEGER,
    };
  });

  readonly availablePriceEntries = resource({
    params: ({ chain }) => {
      const prices = chain(this.priceResource)?.content;
      if (!prices) return undefined;
      const trips = Object.entries(this.plannedTrips());
      return {
        prices,
        trips
      };
    },
    async loader({ params }) {
      const {prices, trips} = params;
      return prices
        .flatMap((price) => {
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
            tripId: Math.max(-1, ...trips.map(([v]) => Number(v))) + 1,
            newTrip: true,
          }];
        })
        .sort((a, b) => a.price.price - b.price.price);
    }
  });

  protected readonly selectedPriceEntry = computed(() => {
    const selected = this.item().assignment;
    if (!selected) return undefined;
    const prices = this.availablePriceEntries.hasValue() ? this.availablePriceEntries.value() : [];
    const index = prices.findIndex(({price, tripId}) =>
      price.uuid === selected.price.uuid && (tripId === selected.tripId)
    );
    console.log(index);
    if (index === -1) return undefined;
    return index;
  });

  protected onPriceSelection($event: number | undefined) {
    return this.priceEntrySelected.emit(this.availablePriceEntries.value()?.[$event ?? -1] ?? {})
  }
}
