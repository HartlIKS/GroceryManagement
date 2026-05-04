import { Component, computed, inject, input, linkedSignal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ListStoreDTO } from '../../../models';
import { StoreMappingTableService, StoreService } from '../../../services';
import { MatInput } from '@angular/material/input';
import { form, FormField, FormRoot, schema } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { StoreListingComponent } from '../../../../user-interface/components/store-listing/store-listing.component';
import { DiffComponent } from './endpoint-test.component';

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
    StoreListingComponent
  ],
  templateUrl: './store-diff.component.html',
  styleUrls: ['./store-diff.component.css']
})
export class StoreDiffComponent implements DiffComponent {
  readonly api = input.required<string>();
  readonly item = input.required<Partial<ListStoreDTO> & {uuid: string}>();
  private readonly storeService = inject(StoreService);
  private readonly mappingService = inject(StoreMappingTableService);
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
  protected readonly mappedStoreResource = this.storeService.getStore(this.mappedId);
  protected readonly hasDiff = linkedSignal(() => {
    const it = this.mappedItem();
    const mp = this.mappedStoreResource.value();
    if(it === undefined) return false;
    if(mp === undefined) return true;
    if(it.name !== undefined && it.name !== mp.name) return true;
    if(it.logo !== undefined && it.logo !== mp.logo) return true;
    if(it.address !== undefined) {
      const iadr = it.address;
      const madr = mp.address;
      if(iadr.street !== undefined && iadr.street !== madr.street) return true;
      if(iadr.number !== undefined && iadr.number !== madr.number) return true;
      if(iadr.zip !== undefined && iadr.zip !== madr.zip) return true;
      if(iadr.city !== undefined && iadr.city !== madr.city) return true;
      if(iadr.country !== undefined && iadr.country !== madr.country) return true;
    }
    return it.currency !== undefined && it.currency !== mp.currency;

  });
  protected readonly combinedItem = linkedSignal(() => {
    const it = this.mappedItem();
    return {
      ...(this.mappedStoreResource.value() ?? {
        uuid: undefined,
        name: '',
        logo: '',
        address: {
          street: '',
          number: '',
          zip: '',
          city: '',
          country: ''
        },
        currency: ''
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

  protected readonly searchText = linkedSignal(() => this.item().name ?? '');
  private readonly searchedStoresResource = this.storeService.search(this.searchText);
  protected readonly searchedStores = computed(() => {
    const products = this.searchedStoresResource.value()?.content ?? [];
    const uuids = products.map(({uuid}) => uuid);
    const mappedId = this.mappedId();
    if(mappedId !== undefined && !uuids.includes(mappedId)) {
      return [mappedId, ...uuids];
    }
    return uuids;
  });

  protected readonly loading = computed(() => this.mappedIdResource.isLoading() || this.mappedStoreResource.isLoading() || this.searchedStoresResource.isLoading());

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
      this.mappedStoreResource.set(await firstValueFrom(this.storeService.update(uuid, createDTO)));
    } else {
      const {uuid} = await firstValueFrom(this.storeService.createStore(createDTO));
      await firstValueFrom(this.mappingService.setInboundTranslation(this.api(), this.item().uuid, uuid));
      this.mappedId.set(uuid);
    }
  }
}
