import { Component, computed, inject, input, resource } from '@angular/core';
import { PriceService, ProductService, StoreService } from '../../../master-data/services';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatListItemIcon, MatListItemMeta } from '@angular/material/list';
import { CurrencyPipe, NgOptimizedImage } from '@angular/common';

function isTrue(b: boolean | `${boolean}`): boolean {
  switch(typeof b) {
    case "boolean":
      return b;
    case "string":
      return b.toLowerCase() == 'true'
  }
}

@Component({
  selector: 'app-product-listing',
  standalone: true,
  imports: [
    MatProgressSpinner,
    MatListItemIcon,
    CurrencyPipe,
    MatListItemMeta,
    NgOptimizedImage,
  ],
  templateUrl: './product-listing.component.html',
  styleUrls: ['./product-listing.component.css'],
})
export class ProductListingComponent {
  readonly uuid = input.required<string | undefined>();
  readonly isPriceUuid = input<boolean | `${boolean}`>(false);
  readonly isPriceBool = computed(() => isTrue(this.isPriceUuid()));
  readonly quantity = input<number>();
  readonly price = input<number>();
  readonly currency = input<string>();

  private readonly priceUuid = computed(() => this.isPriceBool() && this.price() === undefined ? this.uuid() : undefined);
  private readonly fetchedPriceResource = inject(PriceService).get(this.priceUuid);

  protected readonly productResource = inject(ProductService).get(({chain}) => {
    if(this.isPriceBool()) return chain(this.fetchedPriceResource)?.product;
    else return this.uuid();
  });

  private readonly storeResource = inject(StoreService).get(({chain}) => chain(this.fetchedPriceResource)?.store);
  protected readonly priceCurrencyResource = resource({
    params: ({chain}) => {
      const price = this.price() ?? chain(this.fetchedPriceResource)?.price;
      if(price === undefined) return undefined;
      const currency = this.currency() ?? chain(this.storeResource)?.currency;
      if(currency === undefined) return undefined;
      return {price, currency};
    },
    async loader({params}) {
      return params;
    }
  });
}
