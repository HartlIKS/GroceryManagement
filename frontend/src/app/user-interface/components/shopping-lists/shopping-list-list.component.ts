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
    FormsModule
  ],
  templateUrl: './shopping-list-list.component.html',
  styleUrls: ['./shopping-list-list.component.css']
})
export class ShoppingListListComponent implements OnInit {
  displayedColumns: string[] = ['name', 'productCount', 'groupCount', 'actions'];
  searchTerm = '';

  // Use computed signals from service
  public readonly shoppingLists = computed(() => this.shoppingListService.shoppingLists());
  public readonly loading = computed(() => this.shoppingListService.loading());
  public readonly error = computed(() => this.shoppingListService.error());

  // Create MatTableDataSource from shopping lists signal
  public readonly dataSource = computed(() => {
    const shoppingLists = this.shoppingLists();
    return new MatTableDataSource<ListShoppingListDTO>(shoppingLists);
  });

  constructor(
    private shoppingListService: ShoppingListService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadShoppingLists();
  }

  loadShoppingLists(): void {
    this.shoppingListService.getShoppingLists(this.searchTerm);
  }

  onSearch(): void {
    this.loadShoppingLists();
  }

  onAddShoppingList(): void {
    this.router.navigate(['/shopping-lists/new']);
  }

  onEditShoppingList(uuid: string): void {
    this.router.navigate(['/shopping-lists', uuid]);
  }

  onDeleteShoppingList(uuid: string): void {
    if (confirm('Are you sure you want to delete this shopping list?')) {
      // Optimistic update
      this.shoppingListService.removeShoppingListFromCache(uuid);

      this.shoppingListService.deleteShoppingList(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting shopping list:', error);
          // Refresh on error to restore state
          this.shoppingListService.refreshShoppingLists();
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
