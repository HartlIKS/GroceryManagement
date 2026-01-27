import { Component, OnInit, computed } from '@angular/core';
import { Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
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
    FormsModule
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  displayedColumns: string[] = ['name', 'image', 'EAN', 'actions'];
  searchTerm = '';

  // Use computed signals from service
  public readonly products = computed(() => this.productService.products());
  public readonly loading = computed(() => this.productService.loading());
  public readonly error = computed(() => this.productService.error());

  // Create MatTableDataSource from products signal
  public readonly dataSource = computed(() => {
    const products = this.products();
    return new MatTableDataSource<ListProductDTO>(products);
  });

  constructor(
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getProducts(this.searchTerm);
  }

  onSearch(): void {
    this.loadProducts();
  }

  onAddProduct(): void {
    this.router.navigate(['/products/new']);
  }

  onEditProduct(uuid: string): void {
    this.router.navigate(['/products', uuid]);
  }

  onDeleteProduct(uuid: string): void {
    if (confirm('Are you sure you want to delete this product?')) {
      // Optimistic update
      this.productService.removeProductFromCache(uuid);
      
      this.productService.deleteProduct(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting product:', error);
          // Refresh on error to restore state
          this.productService.refreshProducts();
        }
      });
    }
  }
}
