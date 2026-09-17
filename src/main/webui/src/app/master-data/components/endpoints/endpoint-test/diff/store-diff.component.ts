import { Component, computed, inject, input, linkedSignal, resource, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateStoreDTO, StoreTypes } from '../../../../models';
import { StoreMappingTableService, StoreService } from '../../../../services';
import { MatInput } from '@angular/material/input';
import { form, FormField, FormRoot, schema } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { StoreListingComponent } from '../../../../../user-interface/components/store-listing/store-listing.component';
import { DiffComponent } from '../diff-adapter';
import { MatExpansionPanel, MatExpansionPanelContent, MatExpansionPanelHeader } from '@angular/material/expansion';
import { CREATE, UPDATE } from '../../../../../models/base.model';

@Component({
  selector: 'app-store-diff',
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
    MatProgressSpinner,
    StoreListingComponent,
    MatExpansionPanel,
    MatExpansionPanelContent,
    MatExpansionPanelHeader
  ],
  templateUrl: './store-diff.component.html',
  styleUrls: ['./store-diff.component.css']
})
export class StoreDiffComponent implements DiffComponent<StoreTypes[CREATE]> {
  readonly api = input.required<string>();
  readonly item = input.required<StoreTypes[CREATE] & {uuid: string}>();
  private readonly storeService = inject(StoreService);
  private readonly mappingService = inject(StoreMappingTableService);
  protected readonly mappedId = this.mappingService.translateInbound(_ => [this.api(), this.item().uuid]);
  protected readonly fetchedItem = this.storeService.get(({chain}) => chain(this.mappedId));

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
      if (it.logo !== undefined && it.logo !== mp.logo) return true;
      if (it.address !== undefined) {
        const iadr = it.address;
        const madr = mp.address;
        if(madr === undefined) return true;
        if (iadr.streetAndNumber !== undefined && iadr.streetAndNumber !== madr.streetAndNumber) return true;
        if (iadr.zip !== undefined && iadr.zip !== madr.zip) return true;
        if (iadr.city !== undefined && iadr.city !== madr.city) return true;
        if (iadr.country !== undefined && iadr.country !== madr.country) return true;
      }
      return it.currency !== undefined && it.currency !== mp.currency;
    },
    defaultValue: false,
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
  protected readonly mergedItem = linkedSignal<StoreTypes[CREATE] | StoreTypes[UPDATE] & {uuid: string}>(() => {
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
          name: '',
          logo: '',
          address: {
            streetAndNumber: '',
            zip: '',
            city: '',
            country: ''
          },
          currency: ''
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
  private readonly searchedStoresResource = this.storeService.search(_ => {
    if(!this.doReassignment()) return undefined;
    return {
      name: this.searchText(),
      page: 0,
      size: Number.MAX_SAFE_INTEGER,
    };
  });
  protected readonly searchedStores = resource({
    params: ({ chain }) => ({
      products: chain(this.searchedStoresResource)?.content ?? [],
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
      if(uuid !== this.mappedId.value()) await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid));
      this.fetchedItem.set(await firstValueFrom(this.storeService.update(uuid, updateDTO)));
    } else {
      const {uuid} = await firstValueFrom(this.storeService.create(comb));
      await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid));
      this.mappedId.set(uuid);
    }
  }
}
