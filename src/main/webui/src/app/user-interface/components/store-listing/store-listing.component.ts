import { Component, inject, input } from '@angular/core';
import { StoreService } from '../../../master-data/services';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatListItemIcon, MatListItemMeta } from '@angular/material/list';
import { NgOptimizedImage } from '@angular/common';

@Component({
  selector: 'app-store-listing',
  standalone: true,
  imports: [
    MatProgressSpinner,
    MatListItemIcon,
    MatListItemMeta,
    NgOptimizedImage
  ],
  templateUrl: './store-listing.component.html',
  styleUrls: ['./store-listing.component.css'],
})
export class StoreListingComponent {
  readonly uuid = input.required<string>();

  protected readonly storeResource = inject(StoreService).get(this.uuid);
}
