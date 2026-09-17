import { Component, inject, resource, signal } from '@angular/core';
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
import { toDate } from '../../../utils/dateUtil';
import { MatPaginator } from '@angular/material/paginator';

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
    FormsModule,
    MatPaginator
  ],
  templateUrl: './shopping-trip-list.component.html',
  styleUrls: ['./shopping-trip-list.component.css']
})
export class ShoppingTripListComponent {
  displayedColumns: string[] = ['time', 'store', 'productCount', 'actions'];
  protected readonly fromDate = signal<string | undefined>(this.toDatetimeLocal());
  protected readonly toDate = signal<string | undefined>(undefined);
  protected readonly page = signal(0);
  protected readonly pageSize = signal(20);

  private readonly shoppingTripService = inject(ShoppingTripService);
  private readonly storeService = inject(StoreService);

  protected readonly shoppingTripsResource = this.shoppingTripService.search(_ => ({
    from: toDate(this.fromDate()),
    to: toDate(this.toDate()),
    page: this.page(),
    pageSize: this.pageSize(),
  }));
  private readonly storesResource = this.storeService.search(_ => ({
    name: '',
    page: 0,
    size: Number.MAX_SAFE_INTEGER,
  }));

  protected readonly dataSource = resource({
    params: ({chain}) => chain(this.shoppingTripsResource),
    async loader({params}) {
      return new MatTableDataSource<ListShoppingTripDTO>(params.content);
    }
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
    return (this.storesResource.value()?.content ?? []).find(s => s.uuid === storeUuid)?.name ?? 'Unknown Store';
  }

  getStoreLogo(storeUuid: string): string | undefined {
    return (this.storesResource.value()?.content ?? []).find(s => s.uuid === storeUuid)?.logo;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  getProductCount(products: Record<string, number>): number {
    return Object.keys(products).length;
  }
}
