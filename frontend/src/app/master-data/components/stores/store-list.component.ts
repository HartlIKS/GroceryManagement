import { Component, computed, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { StoreService } from '../../services';
import { ListStoreDTO } from '../../models';

@Component({
  selector: 'app-store-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './store-list.component.html',
  styleUrls: ['./store-list.component.css']
})
export class StoreListComponent implements OnInit {
  displayedColumns: string[] = ['name', 'logo', 'address', 'currency', 'actions'];
  searchTerm = '';

  // Use computed signals from service
  public readonly stores = computed(() => this.storeService.stores());
  public readonly loading = computed(() => this.storeService.loading());
  public readonly error = computed(() => this.storeService.error());

  // Create MatTableDataSource from stores signal
  public readonly dataSource = computed(() => {
    const stores = this.stores();
    return new MatTableDataSource<ListStoreDTO>(stores);
  });

  constructor(
    private storeService: StoreService,
  ) {}

  ngOnInit(): void {
    this.loadStores();
  }

  loadStores(): void {
    this.storeService.getStores(this.searchTerm);
  }

  onSearch(): void {
    this.loadStores();
  }

  onDeleteStore(uuid: string): void {
    if (confirm('Are you sure you want to delete this store?')) {
      // Optimistic update
      this.storeService.removeStoreFromCache(uuid);

      this.storeService.deleteStore(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting store:', error);
          // Refresh on error to restore state
          this.storeService.refreshStores();
        }
      });
    }
  }

  formatAddress(address: any): string {
    if (!address) {
      return '-';
    }

    const parts: string[] = [];

    if (address.street && address.number) {
      parts.push(`${address.street} ${address.number}`);
    } else if (address.street) {
      parts.push(address.street);
    } else if (address.number) {
      parts.push(address.number);
    }

    if (address.zip && address.city) {
      parts.push(`${address.zip} ${address.city}`);
    } else if (address.zip) {
      parts.push(address.zip);
    } else if (address.city) {
      parts.push(address.city);
    }

    if (address.country) {
      parts.push(address.country);
    }

    return parts.join(', ');
  }
}
