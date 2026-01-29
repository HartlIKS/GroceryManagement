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
import { ProductGroupService } from '../../services';
import { ListProductGroupDTO } from '../../models';

@Component({
  selector: 'app-product-group-list',
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
  templateUrl: './product-group-list.component.html',
  styleUrls: ['./product-group-list.component.css']
})
export class ProductGroupListComponent implements OnInit {
  displayedColumns: string[] = ['name', 'productCount', 'actions'];
  searchTerm = '';

  // Use computed signals from service
  public readonly productGroups = computed(() => this.productGroupService.productGroups());
  public readonly loading = computed(() => this.productGroupService.loading());
  public readonly error = computed(() => this.productGroupService.error());

  // Create MatTableDataSource from product groups signal
  public readonly dataSource = computed(() => {
    const productGroups = this.productGroups();
    return new MatTableDataSource<ListProductGroupDTO>(productGroups);
  });

  constructor(
    private productGroupService: ProductGroupService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProductGroups();
  }

  loadProductGroups(): void {
    this.productGroupService.getProductGroups(this.searchTerm);
  }

  onSearch(): void {
    this.loadProductGroups();
  }

  onAddProductGroup(): void {
    this.router.navigate(['/product-groups/new']);
  }

  onEditProductGroup(uuid: string): void {
    this.router.navigate(['/product-groups', uuid]);
  }

  onDeleteProductGroup(uuid: string): void {
    if (confirm('Are you sure you want to delete this product group?')) {
      // Optimistic update
      this.productGroupService.removeProductGroupFromCache(uuid);

      this.productGroupService.deleteProductGroup(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting product group:', error);
          // Refresh on error to restore state
          this.productGroupService.refreshProductGroups();
        }
      });
    }
  }

  getProductCount(products: Record<string, number>): number {
    return products ? Object.keys(products).length : 0;
  }
}
