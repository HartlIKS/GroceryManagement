import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProductGroupService } from '../../services';
import { ListProductGroupDTO } from '../../models';

@Component({
  selector: 'app-product-group-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIcon,
    MatInput,
    MatFormFieldModule,
    MatProgressSpinner,
    FormsModule,
    RouterLink
  ],
  templateUrl: './product-group-list.component.html',
  styleUrls: ['./product-group-list.component.css']
})
export class ProductGroupListComponent {
  displayedColumns: string[] = ['name', 'productCount', 'actions'];

  // Search signal
  protected readonly searchTerm = signal('');

  // Create HTTP resource
  private readonly productGroupService = inject(ProductGroupService);
  protected readonly productGroupsResource = this.productGroupService.getProductGroups(this.searchTerm);

  // Computed properties from resource
  public readonly productGroups = computed(() => this.productGroupsResource.value()?.content ?? []);
  public readonly loading = computed(() => this.productGroupsResource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.productGroupsResource.status();
    return status === 'error' ? 'Failed to load product groups' : null;
  });

  // Create MatTableDataSource from product groups signal
  public readonly dataSource = computed(() => {
    const productGroups = this.productGroups();
    return new MatTableDataSource<ListProductGroupDTO>(productGroups);
  });


  onDeleteProductGroup(uuid: string): void {
    if (confirm('Are you sure you want to delete this product group?')) {
      this.productGroupService.delete(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting product group:', error);
        },
        complete: () => this.productGroupsResource.reload(),
      });
    }
  }

  getProductCount(products: Record<string, number>): number {
    return products ? Object.keys(products).length : 0;
  }
}
