import { Component, computed, inject, input, linkedSignal, output, resource, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateProductDTO, ListProductDTO, ProductTypes } from '../../../../models';
import { ProductMappingTableService, ProductService } from '../../../../services';
import { MatInput } from '@angular/material/input';
import { form, FormField, FormRoot, schema } from '@angular/forms/signals';
import {
  ProductListingComponent
} from '../../../../../user-interface/components/product-listing/product-listing.component';
import { firstValueFrom } from 'rxjs';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { DiffComponent } from '../diff-adapter';
import { MatExpansionPanel, MatExpansionPanelContent, MatExpansionPanelHeader } from '@angular/material/expansion';
import { CREATE, UPDATE } from '../../../../../models/base.model';

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
    ProductListingComponent,
    MatProgressSpinner,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelContent
  ],
  templateUrl: './product-diff.component.html',
  styleUrls: ['./product-diff.component.css']
})
export class ProductDiffComponent implements DiffComponent<CreateProductDTO> {
  readonly api = input.required<string>();
  readonly item = input.required<CreateProductDTO & {uuid: string}>();
  private readonly productService = inject(ProductService);
  private readonly mappingService = inject(ProductMappingTableService);
  protected readonly mappedId = this.mappingService.translateInbound(_ => [this.api(), this.item().uuid]);
  private readonly fetchedItem = this.productService.get(({chain}) => chain(this.mappedId));

  protected readonly mappedItem = resource({
    params: ({ chain }) => ({
      item: this.item(),
      id: chain(this.mappedId)
    }),
    async loader({ params }) {
      const {item, id} = params;
      if(id === undefined) {
        const {uuid, ...rest} = item;
        return rest;
      }
      return {
        ...item,
        uuid: id,
      }
    },
  });
  protected readonly hasDiff = resource({
    params: ({ chain }) => [
      chain(this.mappedItem),
      chain(this.fetchedItem),
    ],
    async loader({ params }) {
      const [it, mp] = params;
      if (it === undefined) return false;
      if (mp === undefined) return true;
      if (it.name !== undefined && it.name !== mp.name) return true;
      if (it.image !== undefined && it.image !== mp.image) return true;
      return it.EAN !== undefined && it.EAN !== mp.EAN;
    }
  });
  protected readonly combinedItems = resource({
    params: ({ chain }) => {
      const mapped = chain(this.mappedItem);
      if(!mapped) return undefined;
      return {
        mapped,
        fetched: chain(this.fetchedItem)
      };
    },
    async loader({ params }) {
      return params;
    }
  });
  protected readonly mergedItem = linkedSignal<ProductTypes[CREATE] | ProductTypes[UPDATE] & {uuid: string}>(() => {
    const {mapped, fetched} = (() => {
      if(this.combinedItems.hasValue()) return this.combinedItems.value();
      const {uuid, ...mapped} = this.item();
      return {
        mapped,
        fetched: undefined,
      }
    })();
    return {
      ...(fetched ?? {
        uuid: undefined,
        name: '',
        image: '',
        EAN: '',
      }),
      ...mapped
    };
  });
  protected readonly form = form(
    this.mergedItem,
    schema(() => {}),
    {
      submission: {
        action: () => this.accept()
      }
    }
  );
  protected readonly isIgnored = linkedSignal(() => {
    this.mergedItem();
    return false;
  });

  protected readonly doReassignment = signal(false);
  protected readonly searchText = linkedSignal(() => this.item().name ?? '');
  private readonly searchedProductsResource = this.productService.search(_ => {
    if(!this.doReassignment()) return undefined;
    return {
      name: this.searchText(),
      page: 0,
      size: Number.MAX_SAFE_INTEGER,
    };
  });
  protected readonly searchedProducts = resource({
    params: ({ chain }) => ({
      products: chain(this.searchedProductsResource)?.content ?? [],
      mappedId: chain(this.mappedId),
    }),
    async loader({ params }) {
      const {products, mappedId} = params;
      const uuids = products.map(({ uuid }) => uuid);
      if (mappedId !== undefined && !uuids.includes(mappedId)) {
        return [mappedId, ...uuids];
      }
      return uuids;
    },
    defaultValue: [],
  });

  readonly status = computed(() => {
    if(this.combinedItems.isLoading()) return 'loading';
    if(this.isIgnored()) return 'ignored';
    if(!this.mappedId.hasValue()) return 'create';
    if(this.hasDiff.value()) return 'different';
    return 'same';
  })

  async accept(status: {
    create?: boolean,
    different?: boolean,
  } = {
    create: true,
    different: true,
  }) {
    const st = this.status();
    if(st === 'loading' || st === 'ignored' || st === 'same') return;
    if(!status[st]) return;
    const comb = this.mergedItem();
    if("uuid" in comb) {
      const {uuid, ...updateDTO} = comb;
      if(uuid !== this.mappedId.value()) await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid))
      const newFetch = await firstValueFrom(this.productService.update(uuid, updateDTO));
      if(uuid === this.mappedId.value()) this.fetchedItem.set(newFetch);
      else this.mappedId.reload();
    } else {
      const {uuid} = await firstValueFrom(this.productService.create(comb));
      this.mappedId.set(await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid)));
    }
  }
}
