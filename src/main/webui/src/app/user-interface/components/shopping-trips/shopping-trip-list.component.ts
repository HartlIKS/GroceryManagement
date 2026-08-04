import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';
import { ShoppingTripService } from '../../services';
import { ListShoppingTripDTO } from '../../models';
import { StoreService } from '../../../master-data/services';
import { toDate } from '../../../utils/signalutils';

@Component({
  selector: 'app-shopping-trip-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIcon,
    MatInput,
    MatFormFieldModule,
    MatProgressSpinner,
    MatSelectModule,
    RouterLink,
    MatCardModule,
    FormsModule
  ],
  templateUrl: './shopping-trip-list.component.html',
  styleUrls: ['./shopping-trip-list.component.css']
})
export class ShoppingTripListComponent {
  displayedColumns: string[] = ['time', 'store', 'productCount', 'actions'];
  // Filter signals
  protected readonly fromDate = signal<string | undefined>(this.toDatetimeLocal());
  protected readonly toDate = signal<string | undefined>(undefined);

  private readonly shoppingTripService = inject(ShoppingTripService);
  private readonly storeService = inject(StoreService);

  // Create HTTP resources
  protected readonly shoppingTripsResource = this.shoppingTripService.getShoppingTrips(
    toDate(this.fromDate),
    toDate(this.toDate)
  );
  private readonly storesResource = this.storeService.search('', 0, 1000);

  // Computed properties from resources
  public readonly shoppingTrips = computed(() => this.shoppingTripsResource.value()?.content ?? []);
  public readonly loading = computed(() => this.shoppingTripsResource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.shoppingTripsResource.status();
    return status === 'error' ? 'Failed to load shopping trips' : null;
  });
  public readonly availableStores = computed(() => this.storesResource.value()?.content ?? []);

  // Create MatTableDataSource from shopping trips signal
  public readonly dataSource = computed(() => {
    const shoppingTrips = this.shoppingTrips();
    return new MatTableDataSource<ListShoppingTripDTO>(shoppingTrips);
  });

  clearFilters(): void {
    this.fromDate.set(undefined)
    this.toDate.set(undefined);
  }

  deleteShoppingTrip(uuid: string): void {
    if (confirm('Are you sure you want to delete this shopping trip?')) {
      this.shoppingTripService.delete(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting shopping trip:', error);
        },
        complete: () => this.shoppingTripsResource.reload(),
      });
    }
  }

  toDatetimeLocal(d: Date = new Date()): string {
    return `${d.getFullYear()}-${('0'+(d.getMonth()+1)).slice(-2)}-${('0'+d.getDate()).slice(-2)}T${d.toTimeString().slice(0, 5)}`;
  }

  getStoreName(storeUuid: string): string {
    return this.availableStores().find(s => s.uuid === storeUuid)?.name ?? 'Unknown Store';
  }

  getStoreLogo(storeUuid: string): string | undefined {
    return this.availableStores().find(s => s.uuid === storeUuid)?.logo;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  getProductCount(products: Record<string, number>): number {
    return Object.keys(products).length;
  }
}
