import { computed, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreateShoppingListDTO, ListShoppingListDTO } from '../models';
import { Page } from '../../models';

interface ShoppingListState {
  shoppingLists: ListShoppingListDTO[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  searchTerm: string;
}

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService {
  private readonly endpoint = '/shoppingLists';

  // Private state signal
  private readonly state = signal<ShoppingListState>({
    shoppingLists: [],
    totalElements: 0,
    currentPage: 0,
    pageSize: 20,
    searchTerm: ''
  });

  // Public computed signals
  public readonly shoppingLists = computed(() => this.state().shoppingLists);
  public readonly totalElements = computed(() => this.state().totalElements);
  public readonly currentPage = computed(() => this.state().currentPage);
  public readonly pageSize = computed(() => this.state().pageSize);
  public readonly searchTerm = computed(() => this.state().searchTerm);
  public readonly loading = computed(() => this.apiService.loading());
  public readonly error = computed(() => this.apiService.error());

  constructor(private apiService: ApiService) {}

  // Get shopping lists with caching
  getShoppingLists(name: string = '', page: number = 0, size: number = 20): void {
    this.apiService.get<Page<ListShoppingListDTO>>(this.endpoint, { name, page, size }).subscribe({
      next: (response: Page<ListShoppingListDTO>) => {
        this.state.set({
          shoppingLists: response.content || [],
          totalElements: response.page.totalElements || 0,
          currentPage: response.page.number || 0,
          pageSize: response.page.size || size,
          searchTerm: name
        });
      },
      error: (error: any) => {
        console.error('Error loading shopping lists:', error);
      }
    });
  }

  // Get single shopping list
  getShoppingList(uuid: string): Observable<ListShoppingListDTO> {
    return this.apiService.getById<ListShoppingListDTO>(this.endpoint, uuid);
  }

  // Create shopping list
  createShoppingList(shoppingList: CreateShoppingListDTO): Observable<ListShoppingListDTO> {
    return this.apiService.post<ListShoppingListDTO>(this.endpoint, shoppingList);
  }

  // Update shopping list
  updateShoppingList(uuid: string, shoppingList: CreateShoppingListDTO): Observable<ListShoppingListDTO> {
    return this.apiService.put<ListShoppingListDTO>(this.endpoint, uuid, shoppingList);
  }

  // Delete shopping list
  deleteShoppingList(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

  // Refresh current shopping list list
  refreshShoppingLists(): void {
    const currentState = this.state();
    this.getShoppingLists(currentState.searchTerm, currentState.currentPage, currentState.pageSize);
  }

  // Clear cache
  clearCache(): void {
    this.state.set({
      shoppingLists: [],
      totalElements: 0,
      currentPage: 0,
      pageSize: 20,
      searchTerm: ''
    });
  }

  // Get shopping list by UUID from cache
  getShoppingListByUuid(uuid: string): ListShoppingListDTO | undefined {
    return this.state().shoppingLists.find(list => list.uuid === uuid);
  }

  // Update shopping list in cache (optimistic update)
  updateShoppingListInCache(updatedShoppingList: ListShoppingListDTO): void {
    const currentState = this.state();
    const updatedShoppingLists = currentState.shoppingLists.map(list =>
      list.uuid === updatedShoppingList.uuid ? updatedShoppingList : list
    );

    this.state.set({
      ...currentState,
      shoppingLists: updatedShoppingLists
    });
  }

  // Remove shopping list from cache (optimistic update)
  removeShoppingListFromCache(uuid: string): void {
    const currentState = this.state();
    const updatedShoppingLists = currentState.shoppingLists.filter(list => list.uuid !== uuid);

    this.state.set({
      ...currentState,
      shoppingLists: updatedShoppingLists,
      totalElements: Math.max(0, currentState.totalElements - 1)
    });
  }

  // Add shopping list to cache (optimistic update)
  addShoppingListToCache(newShoppingList: ListShoppingListDTO): void {
    const currentState = this.state();
    const updatedShoppingLists = [newShoppingList, ...currentState.shoppingLists];

    this.state.set({
      ...currentState,
      shoppingLists: updatedShoppingLists,
      totalElements: currentState.totalElements + 1
    });
  }
}
