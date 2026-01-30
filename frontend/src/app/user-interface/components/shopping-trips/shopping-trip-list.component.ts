import { Component, computed, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';
import { ShoppingTripService } from '../../services';
import { ListShoppingTripDTO } from '../../models';
import { StoreService } from '../../../master-data/services';

@Component({
  selector: 'app-shopping-trip-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    RouterLink,
    MatCardModule,
    ReactiveFormsModule
  ],
  templateUrl: './shopping-trip-list.component.html',
  styleUrls: ['./shopping-trip-list.component.css']
})
export class ShoppingTripListComponent implements OnInit {
  displayedColumns: string[] = ['time', 'store', 'productCount', 'actions'];
  fromDateControl: FormControl;
  toDateControl: FormControl;

  // Use computed signals from service
  public readonly shoppingTrips = computed(() => this.shoppingTripService.shoppingTrips());
  public readonly loading = computed(() => this.shoppingTripService.loading());
  public readonly error = computed(() => this.shoppingTripService.error());
  public readonly availableStores = computed(() => this.storeService.stores());

  // Create MatTableDataSource from shopping trips signal
  public readonly dataSource = computed(() => {
    const shoppingTrips = this.shoppingTrips();
    return new MatTableDataSource<ListShoppingTripDTO>(shoppingTrips);
  });

  constructor(
    private shoppingTripService: ShoppingTripService,
    private storeService: StoreService
  ) {
    this.fromDateControl = new FormControl('');
    this.toDateControl = new FormControl('');
  }

  ngOnInit(): void {
    // Load initial data
    this.loadShoppingTrips();
    this.storeService.getStores();
  }

  loadShoppingTrips(): void {
    const fromDate = this.fromDateControl.value || '';
    const toDate = this.toDateControl.value || '';
    // Convert datetime-local format to ISO format for the API
    const formattedFromDate = fromDate ? new Date(fromDate).toISOString() : '';
    const formattedToDate = toDate ? new Date(toDate).toISOString() : '';
    this.shoppingTripService.getShoppingTrips(formattedFromDate, formattedToDate);
  }

  onDateFilterChange(): void {
    this.loadShoppingTrips();
  }

  clearFilters(): void {
    this.fromDateControl.setValue('');
    this.toDateControl.setValue('');
    this.loadShoppingTrips();
  }

  deleteShoppingTrip(uuid: string): void {
    if (confirm('Are you sure you want to delete this shopping trip?')) {
      this.shoppingTripService.deleteShoppingTrip(uuid).subscribe({
        next: () => {
          // Service will handle optimistic update
        },
        error: (error) => {
          console.error('Error deleting shopping trip:', error);
        }
      });
    }
  }

  getStoreName(storeUuid: string): string {
    const store = this.availableStores().find(s => s.uuid === storeUuid);
    return store ? store.name : 'Unknown Store';
  }

  getStoreLogo(storeUuid: string): string | undefined {
    const store = this.availableStores().find(s => s.uuid === storeUuid);
    return store?.logo;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  getProductCount(products: Record<string, number>): number {
    return Object.keys(products).length;
  }

  getTotalProducts(products: Record<string, number>): number {
    return Object.values(products).reduce((sum, amount) => sum + amount, 0);
  }
}
