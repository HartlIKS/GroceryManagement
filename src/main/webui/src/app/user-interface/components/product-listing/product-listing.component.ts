import { Component, computed, inject, input } from '@angular/core';
import { PriceService, ProductService, StoreService } from '../../../master-data/services';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatListItem, MatListItemIcon, MatListItemMeta } from '@angular/material/list';
import { CurrencyPipe } from '@angular/common';

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
    MatListItem,
    MatListItemIcon,
    CurrencyPipe,
    MatListItemMeta,
  ],
  templateUrl: './product-listing.component.html',
  styleUrls: ['./product-listing.component.css'],
})
export class ProductListingComponent {
  readonly uuid = input.required<string | undefined>();
  readonly isPrice = input<boolean | `${boolean}`>(false);
  readonly isPriceBool = computed(() => isTrue(this.isPrice()));
  readonly quantity = input<number>();

  private readonly priceUuid = computed(() => this.isPriceBool() ? this.uuid() : undefined);
  private readonly priceResource = inject(PriceService).getPrice(this.priceUuid);
  readonly price = computed(() => this.priceResource.value());

  private readonly productUuid = computed(() => this.isPriceBool() ? this.price()?.product : this.uuid());
  private readonly productResource = inject(ProductService).getProduct(this.productUuid);
  readonly product = computed(() => this.productResource.value());

  private readonly storeUuid = computed(() => this.price()?.store);
  private readonly storeResource = inject(StoreService).getStore(this.storeUuid);
  readonly currency = computed(() => this.storeResource.value()?.currency);

  readonly loading = computed(() =>
    this.productResource.status() === 'loading' ||
    this.priceResource.status() === 'loading' ||
    this.storeResource.status() === 'loading'
  );
}
