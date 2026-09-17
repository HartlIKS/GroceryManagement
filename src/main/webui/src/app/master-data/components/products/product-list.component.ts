import { Component, computed, inject, resource, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services';
import { ListProductDTO } from '../../models';
import { MatPaginator } from '@angular/material/paginator';

@Component({
  selector: 'app-product-list',
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
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent {
  displayedColumns: string[] = ['image', 'name', 'EAN', 'actions'];

  // Search signal
  protected readonly searchTerm = signal('');
  protected readonly page = signal(0);
  protected readonly pageSize = signal(20);

  private readonly productService = inject(ProductService);

  // Create HTTP resource
  protected readonly productsResource = this.productService.search(_ => ({
      name: this.searchTerm(),
      page: this.page(),
      size: this.pageSize(),
    }));

  public readonly dataSource = resource({
    params: ({chain}) => chain(this.productsResource),
    async loader({params}) {
      return new MatTableDataSource<ListProductDTO>(params.content);
    }
  })

  public readonly loading = computed(() => this.dataSource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.dataSource.status();
    return status === 'error' ? 'Failed to load products' : null;
  });

  onDeleteProduct(uuid: string): void {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.delete(uuid).subscribe({
        complete: () => this.productsResource.reload(),
        error: (error) => {
          console.error('Error deleting product:', error);
        }
      });
    }
  }
}
