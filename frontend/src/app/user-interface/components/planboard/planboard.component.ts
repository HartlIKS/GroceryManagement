import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
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
import { ShoppingListService } from '../../services';
import { Price } from '../../../master-data/models';
import { ShoppingListItemComponent } from './shopping-list-item';
import { PlannedTripComponent } from './planned-trip';
import { PriceEntrySelectionEvent } from './shopping-list-item/shopping-list-item.component';
import { ShoppingList } from '../../models';
import { MatCheckbox } from '@angular/material/checkbox';

export type ShoppingListItem = {
  quantity: number,
  assignment: {
    tripId: number,
    price: Price,
  } | undefined,
} & ({
  type: 'product',
  productUuid: string,
  productGroupUuid?: never,
} | {
  type: 'productGroup',
  productUuid?: never,
  productGroupUuid: string,
});

export type PlannedTrip = {
  storeUuid: string,
  products: Record<string, number>
  validFrom: Date,
  validTo: Date,
};

function max<T>(a: T, b?: T): T {
  if (b === undefined || b === null || b < a) return a;
  return b;
}

function min<T>(a: T, b?: T): T {
  if (b === undefined || b === null || b > a) return a;
  return b;
}

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
    MatCheckbox
  ],
  templateUrl: './planboard.component.html',
  styleUrls: ['./planboard.component.css']
})
export class PlanboardComponent {
  // Item assignments - maps shopping list items to trips and prices
  private readonly itemAssignments = signal<Record<string, Record<string, {
    tripId: number,
    price: Price,
  }>>>({});

  // Services
  private readonly shoppingListService = inject(ShoppingListService);

  // HTTP Resources
  private readonly shoppingListsResource = this.shoppingListService.getShoppingLists('', 0, 1000);

  // Computed properties
  readonly shoppingLists = computed(() => this.shoppingListsResource.value()?.content ?? []);
  // Selected shopping lists
  readonly selectedShoppingListUuids = linkedSignal({
    source: this.shoppingLists,
    computation: (c, p): string[] => {
      if(!p) return [c[0]?.uuid].filter(u => u);
      return p.value.filter(s => c.some(l => l.uuid === s));
    },
  });
  readonly loading = computed(() =>
    this.shoppingListsResource.status() === 'loading'
  );

  // Shopping list items from selected lists
  readonly shoppingListItems = computed<{list: ShoppingList, items: ShoppingListItem[]}[]>(() => {
    const selectedUuids = this.selectedShoppingListUuids();
    const allLists = this.shoppingLists();
    const assignments = this.itemAssignments();

    const allItems: {list: ShoppingList, items: ShoppingListItem[]}[] = [];

    for (const listUuid of selectedUuids) {
      const items: ShoppingListItem[] = [];
      const list = allLists.find(l => l.uuid === listUuid);
      if (!list) continue;

      // Add individual products
      for (const [productUuid, quantity] of Object.entries(list.products)) {
        items.push({
          type: 'product',
          productUuid,
          assignment: assignments[listUuid]?.[productUuid],
          quantity,
        });
      }

      // Add product groups
      for (const [productGroupUuid, quantity] of Object.entries(list.productGroups)) {
        items.push({
          type: 'productGroup',
          productGroupUuid,
          quantity,
          assignment: assignments[listUuid]?.[productGroupUuid],
        });
      }

      allItems.push({list, items});
    }

    return allItems;
  });

  // Planned trips
  readonly plannedTrips = computed(() => this.shoppingListItems()
    .flatMap(({items}) => items)
    .reduce<Record<number, PlannedTrip>>((a, v) => {
      const assignment = v.assignment;
      if (!assignment) return a;
      const trip = a[assignment.tripId] ?? {
        storeUuid: assignment.price.store,
        products: {},
      };
      a[assignment.tripId] = {
        storeUuid: trip.storeUuid,
        products: {
          ...trip.products,
          [assignment.price.uuid]: (trip.products[assignment.price.uuid] ?? 0) + v.quantity
        },
        validFrom: max(new Date(assignment.price.validFrom), trip.validFrom),
        validTo: min(new Date(assignment.price.validTo), trip.validTo),
      }
      return a;
    }, {})
  );

  readonly hasUnassignedItems = computed(() => this.shoppingListItems()
    .some(({items}) => items.some(({assignment}) => !assignment))
  );

  toggleShoppingListSelection(listUuid: string): void {
    const current = this.selectedShoppingListUuids();
    const index = current.indexOf(listUuid);

    if (index >= 0) {
      this.selectedShoppingListUuids.set(current.filter((_, i) => i !== index));
    } else {
      this.selectedShoppingListUuids.set([...current, listUuid]);
    }
  }

  assignItemToPriceEntry(listUuid: string, item: ShoppingListItem, event: PriceEntrySelectionEvent): void {
    if (event.price) {
      // Assign to price entry
      this.itemAssignments.update(c => ({
        ...c,
        [listUuid]: {
          ...c[listUuid] ?? {},
          [item.productUuid ?? item.productGroupUuid]: {
            tripId: event.tripId ?? Math.max(0, ...Object.values(c).flatMap(Object.values).map(v => v?.tripId ?? -1)) + 1,
            price: event.price,
          },
        }
      }));
    } else {
      // Unassign
      this.itemAssignments.update(c => Object.fromEntries(
        Object.entries(c)
          .map(([lid, l]) => [
            lid,
            lid === listUuid ?
              Object.fromEntries(
                Object.entries(l)
                  .filter(([pid]) => pid !== (item.productUuid ?? item.productGroupUuid))
              ) : l
          ])
      ));
    }
  }

  finalizePlanning(): void {
    const trips = this.plannedTrips();

    // Here you would normally send the data to the backend
    console.log('Finalizing trips:', trips);
    alert('Planning finalized! Trips would be sent to backend.');

    // Reset after successful finalization
    this.itemAssignments.set({});
  }

  objectKeys(o: any): string[] {
    return Object.keys(o);
  }
}
