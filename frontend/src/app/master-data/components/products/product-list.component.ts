import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../services';
import { ListProductDTO } from '../../models';

@Component({
  selector: 'app-product-list',
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

  onSearch(searchTerm: string): void {
    this.searchTerm.set(searchTerm);
  }

  onDeleteProduct(uuid: string): void {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting product:', error);
        }
      });
    }
  }
}
