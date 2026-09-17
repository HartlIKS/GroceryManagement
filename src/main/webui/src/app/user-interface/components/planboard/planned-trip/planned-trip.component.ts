import { Component, computed, inject, input } from '@angular/core';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { PlannedTrip } from '../../../services';
import { StoreService } from '../../../../master-data/services';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { ProductListComponent } from '../product-list';

@Component({
  selector: 'app-planned-trip',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatProgressSpinner,
    MatCardModule,
    ProductListComponent,
    NgOptimizedImage,
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
  protected readonly storeResource = this.storeService.get(_ => this.trip().storeUuid);

  protected readonly hasValidity = computed(() =>
    this.trip().validFrom && this.trip().validTo
  );

  protected readonly validityText = computed(() => {
    if (!this.hasValidity()) return 'No validity overlap';
    return `${this.formatDate(this.trip().validFrom.toString())} - ${this.formatDate(this.trip().validTo.toString())}`;
  });

  private formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
