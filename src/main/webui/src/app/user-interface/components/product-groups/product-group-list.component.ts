import { Component, inject, resource, signal } from '@angular/core';
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
import { MatPaginator } from '@angular/material/paginator';

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
    RouterLink,
    MatPaginator
  ],
  templateUrl: './product-group-list.component.html',
  styleUrls: ['./product-group-list.component.css']
})
export class ProductGroupListComponent {
  protected readonly displayedColumns: readonly string[] = ['name', 'productCount', 'actions'];

  // Search signal
  protected readonly searchTerm = signal('');
  protected readonly page = signal(0);
  protected readonly pageSize = signal(20);

  // Create HTTP resource
  private readonly productGroupService = inject(ProductGroupService);
  protected readonly productGroupsResource = this.productGroupService.search(_ => ({
    name: this.searchTerm(),
    page: this.page(),
    pageSize: this.pageSize(),
  }));

  // Create MatTableDataSource from product groups signal
  protected readonly dataSource = resource({
    params: ({chain}) => {
      return chain(this.productGroupsResource)?.content;
    },
    async loader({params}) {
      return new MatTableDataSource<ListProductGroupDTO>(params);
    }
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
