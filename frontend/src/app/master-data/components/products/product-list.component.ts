import { Component, computed, inject, signal } from '@angular/core';
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
    RouterLink
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent {
  displayedColumns: string[] = ['image', 'name', 'EAN', 'actions'];

  // Search signal
  protected readonly searchTerm = signal('');

  private readonly productService = inject(ProductService);

  // Create HTTP resource
  protected readonly productsResource = this.productService.getProducts(this.searchTerm());

  // Computed properties from resource
  public readonly products = computed(() => this.productsResource.value()?.content ?? []);
  public readonly loading = computed(() => this.productsResource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.productsResource.status();
    return status === 'error' ? 'Failed to load products' : null;
  });

  // Create MatTableDataSource from products signal
  public readonly dataSource = computed(() => {
    const products = this.products();
    return new MatTableDataSource<ListProductDTO>(products);
  });

  onDeleteProduct(uuid: string): void {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.delete(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting product:', error);
        }
      });
    }
  }
}
