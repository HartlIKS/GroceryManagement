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
import { ShoppingListService } from '../../services';
import { MatPaginator } from '@angular/material/paginator';

@Component({
  selector: 'app-shopping-list-list',
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
  templateUrl: './shopping-list-list.component.html',
  styleUrls: ['./shopping-list-list.component.css']
})
export class ShoppingListListComponent {
  displayedColumns: string[] = ['name', 'repeating', 'productCount', 'groupCount', 'actions'];

  // Search signal
  protected readonly searchTerm = signal('');
  protected readonly page = signal(0);
  protected readonly pageSize = signal(20);

  // Create HTTP resource
  private readonly shoppingListService = inject(ShoppingListService);
  protected readonly shoppingListsResource = this.shoppingListService.search(_ => ({
    name: this.searchTerm(),
    page: this.page(),
    pageSize: this.pageSize(),
  }));

  public readonly dataSource = resource({
    params: ({chain}) => chain(this.shoppingListsResource)?.content,
    async loader({params}) {
      return new MatTableDataSource(params);
    }
  });

  onDeleteShoppingList(uuid: string): void {
    if (confirm('Are you sure you want to delete this shopping list?')) {
      this.shoppingListService.delete(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting shopping list:', error);
        },
        complete: () => this.shoppingListsResource.reload(),
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
