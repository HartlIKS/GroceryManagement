import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { CommonModule } from '@angular/common';
import { PlannedTrip, ShoppingTripService } from '../../../services';
import { ProductListComponent } from '../product-list';

export interface TripPlanningState {
  tripId: number;
  plannedTrip: PlannedTrip;
}

export type selectionEvent =
  {tripUuid: string, date?: never} |
  {tripUuid?: never, date: Date | undefined};

@Component({
  selector: 'app-trip-planning-card',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatInput,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatDatepickerModule,
    ProductListComponent,
  ],
  templateUrl: './trip-planning-card.component.html',
  styleUrls: ['./trip-planning-card.component.css']
})
export class TripPlanningCardComponent {
  readonly trip = input.required<PlannedTrip>();
  readonly tripUuid = signal<string | undefined>(undefined);
  readonly date = signal<string | undefined>(undefined);
  private readonly sel = computed((): selectionEvent => {
    const tripUuid = this.tripUuid();
    if(tripUuid !== undefined) return {tripUuid};
    const dateStr = this.date();
    const date = dateStr === undefined ? undefined : new Date(dateStr);
    return {date};
  });
  readonly selection = output<selectionEvent>();

  constructor() {
    effect(() => this.selection.emit(this.sel()));
  }

  // Services
  private readonly shoppingTripService = inject(ShoppingTripService);

  // Get existing trips for this specific trip's store and timespan
  readonly existingTripsResource = this.shoppingTripService.getShoppingTrips(
    computed(() => this.trip().validFrom),
    computed(() => this.trip().validTo),
    0,
    1000
  );

  readonly existingTrips = computed(() => {
    const trips = this.existingTripsResource.value()?.content ?? [];
    return trips.filter(trip =>
      trip.store === this.trip().storeUuid
    );
  });

  objectKeys(o: any): string[] {
    return Object.keys(o);
  }
}
