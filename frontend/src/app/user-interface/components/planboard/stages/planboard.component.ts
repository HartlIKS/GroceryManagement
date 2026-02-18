import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { CommonModule } from '@angular/common';
import { PlanboardService, ShoppingListItem } from '../../../services';
import { ShoppingListItemComponent } from '../shopping-list-item';
import { PlannedTripComponent } from '../planned-trip';
import { PriceEntrySelectionEvent } from '../shopping-list-item/shopping-list-item.component';
import { MatCheckbox } from '@angular/material/checkbox';
import { RouterLink } from '@angular/router';

export type { PlannedTrip, ShoppingListItem } from '../../../services/planboard.service';

@Component({
  selector: 'app-planboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatCardModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule,
    MatListModule,
    ShoppingListItemComponent,
    PlannedTripComponent,
    MatCheckbox,
    RouterLink
  ],
  templateUrl: './planboard.component.html',
  styleUrls: ['./planboard.component.css']
})
export class PlanboardComponent {
  // Services
  private readonly planboardService = inject(PlanboardService);

  // Expose service computed properties as readonly
  readonly shoppingLists = this.planboardService.shoppingLists;
  readonly selectedShoppingListUuids = this.planboardService.selectedShoppingListUuids;
  readonly loading = this.planboardService.loading;
  readonly shoppingListItems = this.planboardService.shoppingListItems;
  readonly plannedTrips = this.planboardService.plannedTrips;
  readonly hasUnassignedItems = this.planboardService.hasUnassignedItems;

  toggleShoppingListSelection(listUuid: string): void {
    this.planboardService.toggleShoppingListSelection(listUuid);
  }

  assignItemToPriceEntry(listUuid: string, item: ShoppingListItem, event: PriceEntrySelectionEvent): void {
    this.planboardService.assignItemToPriceEntry(listUuid, item, event.price, event.tripId);
  }

  objectKeys(o: any): string[] {
    return Object.keys(o);
  }
}
