import { Component, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

import { PlannedTrip } from '../planboard.component';
import { StoreService } from '../../../../master-data/services';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatList } from '@angular/material/list';
import { ProductListingComponent } from '../../product-listing/product-listing.component';

@Component({
  selector: 'app-planned-trip',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatProgressSpinner,
    MatCardModule,
    MatList,
    ProductListingComponent,

  ],
  templateUrl: './planned-trip.component.html',
  styleUrls: ['./planned-trip.component.css']
})
export class PlannedTripComponent {
  // Signal inputs
  readonly trip = input.required<PlannedTrip>();
  readonly tripId = input.required<string>();

  // Services
  private readonly storeService = inject(StoreService);

  // Resources
  private readonly storeResource = this.storeService.getStore(
    computed(() => this.trip().storeUuid)
  );

  // Computed properties
  protected readonly store = computed(() => this.storeResource.value());
  protected readonly loading = computed(() =>
    this.storeResource.status() === 'loading'
  );

  protected readonly hasValidity = computed(() =>
    this.trip().validFrom && this.trip().validTo
  );

  protected readonly validityText = computed(() => {
    if (!this.hasValidity()) return 'No validity overlap';
    return `${this.formatDate(this.trip().validFrom.toString())} - ${this.formatDate(this.trip().validTo.toString())}`;
  });

  protected readonly productEntries = computed(() =>
    Object.entries(this.trip().products).map(([priceUuid, quantity]) => ({
      priceUuid,
      quantity
    }))
  );

  private formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
