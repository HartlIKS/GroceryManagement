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
import { ShoppingListService } from '../../services';
import { ListShoppingListDTO } from '../../models';

@Component({
  selector: 'app-shopping-list-list',
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
  templateUrl: './shopping-list-list.component.html',
  styleUrls: ['./shopping-list-list.component.css']
})
export class ShoppingListListComponent {
  displayedColumns: string[] = ['name', 'productCount', 'groupCount', 'actions'];

  // Search signal
  protected readonly searchTerm = signal('');

  // Create HTTP resource
  private readonly shoppingListService = inject(ShoppingListService);
  protected readonly shoppingListsResource = this.shoppingListService.getShoppingLists(this.searchTerm);

  // Computed properties from resource
  public readonly shoppingLists = computed(() => this.shoppingListsResource.value()?.content ?? []);
  public readonly loading = computed(() => this.shoppingListsResource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.shoppingListsResource.status();
    return status === 'error' ? 'Failed to load shopping lists' : null;
  });

  // Create MatTableDataSource from shopping lists signal
  public readonly dataSource = computed(() => {
    const shoppingLists = this.shoppingLists();
    return new MatTableDataSource<ListShoppingListDTO>(shoppingLists);
  });

  onDeleteShoppingList(uuid: string): void {
    if (confirm('Are you sure you want to delete this shopping list?')) {
      this.shoppingListService.deleteShoppingList(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting shopping list:', error);
        }
      });
    }
  }

  getProductCount(products: Record<string, number>): number {
    return products ? Object.keys(products).length : 0;
  }

  getGroupCount(productGroups: Record<string, number>): number {
    return productGroups ? Object.keys(productGroups).length : 0;
  }
}
