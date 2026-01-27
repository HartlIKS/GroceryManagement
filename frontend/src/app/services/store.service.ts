import { computed, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CreateStoreDTO, ListStoreDTO, Page } from '../models';

interface StoreState {
  stores: ListStoreDTO[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  searchTerm: string;
}

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private readonly endpoint = '/masterdata/store';

  // Private state signal
  private readonly state = signal<StoreState>({
    stores: [],
    totalElements: 0,
    currentPage: 0,
    pageSize: 20,
    searchTerm: ''
  });

  // Public computed signals
  public readonly stores = computed(() => this.state().stores);
  public readonly totalElements = computed(() => this.state().totalElements);
  public readonly currentPage = computed(() => this.state().currentPage);
  public readonly pageSize = computed(() => this.state().pageSize);
  public readonly searchTerm = computed(() => this.state().searchTerm);
  public readonly loading = computed(() => this.apiService.loading());
  public readonly error = computed(() => this.apiService.error());

  constructor(private apiService: ApiService) {}

  // Get stores with caching
  getStores(name: string = '', page: number = 0, size: number = 20): void {
    this.apiService.get<Page<ListStoreDTO>>(this.endpoint, { name, page, size }).subscribe({
      next: (response: Page<ListStoreDTO>) => {
        this.state.set({
          stores: response.content || [],
          totalElements: response.page.totalElements || 0,
          currentPage: response.page.number || 0,
          pageSize: response.page.size || size,
          searchTerm: name
        });
      },
      error: (error: any) => {
        console.error('Error loading stores:', error);
      }
    });
  }

  // Get single store
  getStore(uuid: string): Observable<ListStoreDTO> {
    return this.apiService.getById<ListStoreDTO>(this.endpoint, uuid);
  }

  // Create store
  createStore(store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.post<ListStoreDTO>(this.endpoint, store);
  }

  // Update store
  updateStore(uuid: string, store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.put<ListStoreDTO>(this.endpoint, uuid, store);
  }

  // Delete store
  deleteStore(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

  // Refresh current store list
  refreshStores(): void {
    const currentState = this.state();
    this.getStores(currentState.searchTerm, currentState.currentPage, currentState.pageSize);
  }

  // Clear cache
  clearCache(): void {
    this.state.set({
      stores: [],
      totalElements: 0,
      currentPage: 0,
      pageSize: 20,
      searchTerm: ''
    });
  }

  // Get store by UUID from cache
  getStoreByUuid(uuid: string): ListStoreDTO | undefined {
    return this.state().stores.find(store => store.uuid === uuid);
  }

  // Update store in cache (optimistic update)
  updateStoreInCache(updatedStore: ListStoreDTO): void {
    const currentState = this.state();
    const updatedStores = currentState.stores.map(store =>
      store.uuid === updatedStore.uuid ? updatedStore : store
    );

    this.state.set({
      ...currentState,
      stores: updatedStores
    });
  }

  // Remove store from cache (optimistic update)
  removeStoreFromCache(uuid: string): void {
    const currentState = this.state();
    const updatedStores = currentState.stores.filter(store => store.uuid !== uuid);

    this.state.set({
      ...currentState,
      stores: updatedStores,
      totalElements: Math.max(0, currentState.totalElements - 1)
    });
  }

  // Add store to cache (optimistic update)
  addStoreToCache(newStore: ListStoreDTO): void {
    const currentState = this.state();
    const updatedStores = [newStore, ...currentState.stores];

    this.state.set({
      ...currentState,
      stores: updatedStores,
      totalElements: currentState.totalElements + 1
    });
  }
}
