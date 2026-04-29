import { Component, computed, inject, input, linkedSignal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ListProductDTO } from '../../../models';
import { ProductMappingTableService, ProductService } from '../../../services';
import { MatInput } from '@angular/material/input';
import { form, FormField, FormRoot, schema } from '@angular/forms/signals';
import {
  ProductListingComponent
} from '../../../../user-interface/components/product-listing/product-listing.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-product-diff',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatInput,
    FormRoot,
    FormField,
    ProductListingComponent
  ],
  templateUrl: './product-diff.component.html',
  styleUrls: ['./product-diff.component.css']
})
export class ProductDiffComponent {
  readonly api = input.required<string>();
  readonly item = input.required<Partial<ListProductDTO> & {uuid: string}>();
  private readonly productService = inject(ProductService);
  private readonly mappingService = inject(ProductMappingTableService);
  private readonly mappedIdResource = this.mappingService.translateInbound(this.api, computed(() => this.item().uuid));
  protected readonly mappedId = linkedSignal(() => this.mappedIdResource.value() ?? undefined);

  protected readonly mappedItem = computed(() => {
    const it = this.item();
    const mid = this.mappedId();
    if(mid === undefined) {
      const {uuid, ...ret} = it;
      return ret;
    }
    return {
      ...it,
      uuid: mid,
    };
  });
  protected readonly mappedProductResource = this.productService.getProduct(this.mappedId);
  protected readonly hasDiff = linkedSignal(() => {
    const it = this.mappedItem();
    const mp = this.mappedProductResource.value();
    if(it === undefined) return false;
    if(mp === undefined) return true;
    if(it.name !== undefined && it.name !== mp.name) return true;
    if(it.image !== undefined && it.image !== mp.image) return true;
    return it.EAN !== undefined && it.EAN !== mp.EAN;
  });
  protected readonly combinedItem = linkedSignal(() => {
    const it = this.mappedItem();
    return {
      ...(this.mappedProductResource.value() ?? {
        uuid: undefined,
        name: '',
        image: '',
        EAN: ''
      }),
      ...it
    };
  });
  protected readonly form = form(
    this.combinedItem,
    schema(() => {}),
    {
      submission: {
        action: async () => {
          const {uuid, ...createDTO} = this.combinedItem();
          if(uuid) {
            await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid))
            await firstValueFrom(this.productService.update(uuid, createDTO));
            this.hasDiff.set(false);
          } else {
            const {uuid} = await firstValueFrom(this.productService.createProduct(createDTO));
            await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid));
            this.hasDiff.set(false);
          }
        }
      }
    }
  );
  protected readonly isIgnored = linkedSignal(() => {
    this.combinedItem();
    return false;
  });

  protected readonly searchText = linkedSignal(() => this.item().name ?? '');
  private readonly searchedProductsResource = this.productService.search(this.searchText);
  protected readonly searchedProducts = computed(() => {
    const products = this.searchedProductsResource.value()?.content ?? [];
    const uuids = products.map(({uuid}) => uuid);
    const mappedId = this.mappedId();
    if(mappedId !== undefined && !uuids.includes(mappedId)) {
      return [mappedId, ...uuids];
    }
    return uuids;
  });
}
