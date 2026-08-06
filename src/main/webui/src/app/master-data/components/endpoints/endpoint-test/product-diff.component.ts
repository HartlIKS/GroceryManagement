import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';
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
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { DiffComponent } from './endpoint-test.component';
import { MatExpansionPanel, MatExpansionPanelContent, MatExpansionPanelHeader } from '@angular/material/expansion';

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
export class ProductDiffComponent implements DiffComponent<ListProductDTO> {
  readonly api = input.required<string>();
  readonly item = input.required<Partial<ListProductDTO> & {uuid: string}>();
  readonly fetchedItem = input.required<ListProductDTO | undefined>();
  readonly afterChange = input.required<() => void>();
  private readonly productService = inject(ProductService);
  private readonly mappingService = inject(ProductMappingTableService);
  protected readonly mappedId = linkedSignal(() => this.fetchedItem()?.uuid);

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
  protected readonly hasDiff = linkedSignal(() => {
    const it = this.mappedItem();
    const mp = this.fetchedItem();
    if(it === undefined) return false;
    if(mp === undefined) return true;
    if(it.name !== undefined && it.name !== mp.name) return true;
    if(it.image !== undefined && it.image !== mp.image) return true;
    return it.EAN !== undefined && it.EAN !== mp.EAN;
  });
  protected readonly combinedItem = linkedSignal(() => {
    const it = this.mappedItem();
    return {
      ...(this.fetchedItem() ?? {
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
        action: () => this.accept()
      }
    }
  );
  protected readonly isIgnored = linkedSignal(() => {
    this.combinedItem();
    return false;
  });

  protected readonly doReassignment = signal(false);
  protected readonly searchText = linkedSignal(() => this.item().name ?? '');
  private readonly searchedProductsResource = this.productService.search(
    this.searchText,
    undefined,
    undefined,
    computed(() => !this.doReassignment())
  );
  protected readonly searchedProducts = computed(() => {
    const products = this.searchedProductsResource.value()?.content ?? [];
    const uuids = products.map(({uuid}) => uuid);
    const mappedId = this.mappedId();
    if(mappedId !== undefined && !uuids.includes(mappedId)) {
      return [mappedId, ...uuids];
    }
    return uuids;
  });
  protected readonly loading = this.searchedProductsResource.isLoading;

  protected access(v: any, f: string[]): any {
    for(const k of f) {
      v = v?.[k];
    }
    return v;
  }

  readonly status = computed(() => {
    if(this.loading()) return 'loading';
    if(this.isIgnored()) return 'ignored';
    if(this.mappedId() === undefined) return 'create';
    if(this.hasDiff()) return 'different';
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
    const {uuid, ...createDTO} = this.combinedItem();
    if(uuid) {
      await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid))
      await firstValueFrom(this.productService.update(uuid, createDTO));
    } else {
      const {uuid} = await firstValueFrom(this.productService.createProduct(createDTO));
      await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid));
      this.mappedId.set(uuid);
    }
    this.afterChange()();
  }
}
