import { Component, inject, resource, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { StoreService } from '../../services';
import { ListStoreDTO } from '../../models';
import { MatPaginator } from '@angular/material/paginator';

@Component({
  selector: 'app-store-list',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIcon,
    MatInput,
    MatFormFieldModule,
    MatProgressSpinner,
    FormsModule,
    RouterLink,
    MatPaginator
  ],
  templateUrl: './store-list.component.html',
  styleUrls: ['./store-list.component.css']
})
export class StoreListComponent {
  displayedColumns: string[] = ['name', 'logo', 'address', 'currency', 'actions'];

  protected readonly searchTerm = signal('');
  protected readonly page = signal(0);
  protected readonly pageSize = signal(20);

  private readonly storeService = inject(StoreService);

  protected readonly storesResource = this.storeService.search(_ => ({
    name: this.searchTerm(),
    page: this.page(),
    pageSize: this.pageSize(),
  }));

  protected readonly dataSource = resource({
    params: ({ chain }) => chain(this.storesResource)?.content,
    async loader({ params }) {
      return new MatTableDataSource<ListStoreDTO>(params);
    }
  });

  onDeleteStore(uuid: string): void {
    if (confirm('Are you sure you want to delete this store?')) {
      this.storeService.delete(uuid).subscribe({
        complete: this.storesResource.reload,
        error: (error) => {
          console.error('Error deleting store:', error);
        }
      });
    }
  }

  formatAddress(address: any): string {
    if (!address) {
      return '-';
    }

    const parts: string[] = [];

    if (address.streetAndNumber) {
      parts.push(address.streetAndNumber);
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
