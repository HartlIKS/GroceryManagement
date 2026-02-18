import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { PlanboardService, ShoppingTripService } from '../../../services';
import { selectionEvent, TripPlanningCardComponent } from '../trip-planning/trip-planning-card.component';
import { combineLatestAll, from, map, switchMap } from 'rxjs';
import { PriceService } from '../../../../master-data/services';

@Component({
  selector: 'app-trip-planning',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TripPlanningCardComponent,
  ],
  templateUrl: './trip-planning.component.html',
  styleUrls: ['./trip-planning.component.css']
})
export class TripPlanningComponent {
  private readonly planboardService = inject(PlanboardService);
  private readonly shoppingTripService = inject(ShoppingTripService);
  private readonly priceService = inject(PriceService);

  readonly plannedTrips = this.planboardService.plannedTrips;

  // Trip planning state for each planned trip
  private readonly tripPlanningStates = signal<Record<number, selectionEvent>>({});

  readonly allTripsHaveDateTime = computed(() => {
    const plans = this.tripPlanningStates();
    return Object.keys(this.plannedTrips())
      .map(p => plans[Number(p)])
      .every(p => p?.tripUuid || p?.date)
  });

  onTripChange(tripId: string, info: selectionEvent): void {
    this.tripPlanningStates.update(states => ({
      ...states,
      [tripId]: info,
    }));
  }

  createOrMergeTrips() {
    const states = this.tripPlanningStates();
    from(
      Object.entries(this.plannedTrips())
        .map(([tripId, tripInfo]) => [tripInfo, states[Number(tripId)]] as const)
        .map(([trip, sel]) => {
          if (sel.tripUuid !== undefined) return this.shoppingTripService.addProducts(sel.tripUuid, trip.products);
          else return from(Object.entries(trip.products)).pipe(
            map(([uuid, quantity]) => this.priceService.getObservable(uuid).pipe(
              map(v => [v.product, quantity] as const)
            )),
            combineLatestAll(),
            switchMap(products => this.shoppingTripService.createShoppingTrip({
              store: trip.storeUuid,
              time: sel.date!.toISOString(),
              products: Object.fromEntries(products),
            }))
          );
        })
    ).pipe(
      combineLatestAll(),
    ).subscribe(v => {
      console.log(v);
      // Reset assignments after successful creation/merge
      this.planboardService.resetAssignments();
      this.tripPlanningStates.set({});
    });

  }
}
