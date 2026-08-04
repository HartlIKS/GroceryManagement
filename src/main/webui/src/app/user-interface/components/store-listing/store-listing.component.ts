import { Component, computed, inject, input } from '@angular/core';
import { StoreService } from '../../../master-data/services';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatListItem, MatListItemIcon, MatListItemMeta } from '@angular/material/list';

@Component({
  selector: 'app-store-listing',
  standalone: true,
  imports: [
    MatProgressSpinner,
    MatListItem,
    MatListItemIcon,
    MatListItemMeta,
  ],
  templateUrl: './store-listing.component.html',
  styleUrls: ['./store-listing.component.css'],
})
export class StoreListingComponent {
  readonly uuid = input.required<string>();

  private readonly storeResource = inject(StoreService).getStore(this.uuid);
  readonly store = computed(() => this.storeResource.value());

  readonly loading = computed(() => this.storeResource.status() === 'loading');
}
