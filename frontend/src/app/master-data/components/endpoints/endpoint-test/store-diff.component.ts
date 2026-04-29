import { Component, computed, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ListStoreDTO, Store } from '../../../models';
import { StoreService } from '../../../services';
import { MatInput } from '@angular/material/input';

interface DiffField {
  field: string;
  remote: string;
  local: string;
}

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
    MatInput
  ],
  templateUrl: './store-diff.component.html',
  styleUrls: ['./store-diff.component.css']
})
export class StoreDiffComponent {
  readonly item = input.required<Partial<ListStoreDTO> & {uuid: string}>();
  private readonly storeService = inject(StoreService);

  protected readonly isCollapsed = signal(true);
  protected readonly selectedLocalUuid = signal<string | undefined>(undefined);
  protected readonly searchQuery = signal('');
  protected readonly availableStoresResource = this.storeService.search(this.searchQuery);
  protected readonly availableStores = computed(() => this.availableStoresResource.value()?.content ?? []);

  protected readonly selectedLocalStore = computed(() => {
    const uuid = this.selectedLocalUuid();
    if (!uuid) return null;
    return this.availableStores().find(s => s.uuid === uuid) || null;
  });

  protected readonly differences = computed<DiffField[]>(() => {
    const remote = this.item();
    const local = this.selectedLocalStore();
    if (!local) return [];

    const diffs: DiffField[] = [];

    if (remote.name !== local.name) {
      diffs.push({ field: 'Name', remote: remote.name || '', local: local.name });
    }
    if (remote.logo !== local.logo) {
      diffs.push({ field: 'Logo', remote: remote.logo || '', local: local.logo });
    }
    if (remote.currency !== local.currency) {
      diffs.push({ field: 'Currency', remote: remote.currency || '', local: local.currency });
    }

    // Address comparison
    if (remote.address && local.address) {
      if (remote.address.street !== local.address.street) {
        diffs.push({ field: 'Street', remote: remote.address.street || '', local: local.address.street });
      }
      if (remote.address.number !== local.address.number) {
        diffs.push({ field: 'Number', remote: remote.address.number || '', local: local.address.number });
      }
      if (remote.address.zip !== local.address.zip) {
        diffs.push({ field: 'ZIP', remote: remote.address.zip || '', local: local.address.zip });
      }
      if (remote.address.city !== local.address.city) {
        diffs.push({ field: 'City', remote: remote.address.city || '', local: local.address.city });
      }
      if (remote.address.country !== local.address.country) {
        diffs.push({ field: 'Country', remote: remote.address.country || '', local: local.address.country });
      }
    }

    return diffs;
  });

  protected readonly hasDifferences = computed(() => this.differences().length > 0);

  constructor() {
    // Auto-collapse if no differences when a store is selected
    // This will be handled in the template
  }

  toggleCollapse(): void {
    this.isCollapsed.set(!this.isCollapsed());
  }

  onIgnore(): void {
    this.isCollapsed.set(true);
  }

  onAccept(): void {
    const localUuid = this.selectedLocalUuid();
    const remoteUuid = this.item().uuid;

    if (!localUuid) return;

    // TODO: Send mapping to backend via mapping service
    // This would call the mapping service to set the inbound translation
    console.log(`Accept mapping: remote ${remoteUuid} -> local ${localUuid}`);

    // Also update local store if there are differences
    if (this.hasDifferences()) {
      const updates: Partial<Store> = {};
      const remote = this.item();

      if (remote.name) updates.name = remote.name;
      if (remote.logo) updates.logo = remote.logo;
      if (remote.currency) updates.currency = remote.currency;
      if (remote.address) updates.address = remote.address;

      // TODO: Update store via storeService
      console.log('Update store with:', updates);
    }

    this.isCollapsed.set(true);
  }
}
