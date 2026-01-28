import { computed, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreatePriceListingDTO, ListPriceDTO, UpdatePriceDTO } from '../models';
import { Page } from '../../models';

interface PriceState {
  prices: ListPriceDTO[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  searchTerm: string;
  selectedStore: string | null;
  selectedProduct: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class PriceService {
  private readonly endpoint = '/masterdata/price';

  // Private state signal
  private readonly state = signal<PriceState>({
    prices: [],
    totalElements: 0,
    currentPage: 0,
    pageSize: 20,
    searchTerm: '',
    selectedStore: null,
    selectedProduct: null
  });

  // Public computed signals
  public readonly prices = computed(() => this.state().prices);
  public readonly totalElements = computed(() => this.state().totalElements);
  public readonly currentPage = computed(() => this.state().currentPage);
  public readonly pageSize = computed(() => this.state().pageSize);
  public readonly searchTerm = computed(() => this.state().searchTerm);
  public readonly selectedStore = computed(() => this.state().selectedStore);
  public readonly selectedProduct = computed(() => this.state().selectedProduct);
  public readonly loading = computed(() => this.apiService.loading());
  public readonly error = computed(() => this.apiService.error());

  constructor(private apiService: ApiService) {}

  // Get prices with pagination and filtering
  getPrices(page: number = 0, size: number = 20): void {
    const currentState = this.state();

    // Build query parameters based on filters
    const params: any = { page, size };

    if (currentState.selectedStore) {
      params.store = currentState.selectedStore;
    }

    if (currentState.selectedProduct) {
      params.product = currentState.selectedProduct;
    }

    this.apiService.get<Page<ListPriceDTO>>(this.endpoint, params).subscribe({
      next: (response: Page<ListPriceDTO>) => {
        this.state.set({
          prices: response.content || [],
          totalElements: response.page.totalElements || 0,
          currentPage: response.page.number || 0,
          pageSize: response.page.size || size,
          searchTerm: currentState.searchTerm,
          selectedStore: currentState.selectedStore,
          selectedProduct: currentState.selectedProduct
        });
      },
      error: (error: any) => {
        console.error('Error loading prices:', error);
      }
    });
  }

  // Get single price
  getPrice(uuid: string): Observable<ListPriceDTO> {
    return this.apiService.getById<ListPriceDTO>(this.endpoint, uuid);
  }

  // Create price
  createPrice(price: CreatePriceListingDTO): Observable<ListPriceDTO> {
    return this.apiService.post<ListPriceDTO>(this.endpoint, price);
  }

  // Update price
  updatePrice(uuid: string, price: UpdatePriceDTO): Observable<ListPriceDTO> {
    return this.apiService.put<ListPriceDTO>(this.endpoint, uuid, price);
  }

  // Delete price
  deletePrice(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

  // Refresh current price list
  refreshPrices(): void {
    const currentState = this.state();
    this.getPrices(currentState.currentPage, currentState.pageSize);
  }

  // Clear cache
  clearCache(): void {
    this.state.set({
      prices: [],
      totalElements: 0,
      currentPage: 0,
      pageSize: 20,
      searchTerm: '',
      selectedStore: null,
      selectedProduct: null
    });
  }

  // Set store filter
  setStoreFilter(storeUuid: string | null): void {
    const currentState = this.state();
    this.state.set({
      ...currentState,
      selectedStore: storeUuid,
      currentPage: 0 // Reset to first page when filter changes
    });
  }

  // Set product filter
  setProductFilter(productUuid: string | null): void {
    const currentState = this.state();
    this.state.set({
      ...currentState,
      selectedProduct: productUuid,
      currentPage: 0 // Reset to first page when filter changes
    });
  }

  // Clear all filters
  clearFilters(): void {
    const currentState = this.state();
    this.state.set({
      ...currentState,
      selectedStore: null,
      selectedProduct: null,
      currentPage: 0 // Reset to first page when filters are cleared
    });
  }

  // Get price by UUID from cache
  getPriceByUuid(uuid: string): ListPriceDTO | undefined {
    return this.state().prices.find(price => price.uuid === uuid);
  }

  // Update price in cache (optimistic update)
  updatePriceInCache(updatedPrice: ListPriceDTO): void {
    const currentState = this.state();
    const updatedPrices = currentState.prices.map(price =>
      price.uuid === updatedPrice.uuid ? updatedPrice : price
    );

    this.state.set({
      ...currentState,
      prices: updatedPrices
    });
  }

  // Remove price from cache (optimistic update)
  removePriceFromCache(uuid: string): void {
    const currentState = this.state();
    const updatedPrices = currentState.prices.filter(price => price.uuid !== uuid);

    this.state.set({
      ...currentState,
      prices: updatedPrices,
      totalElements: Math.max(0, currentState.totalElements - 1)
    });
  }

  // Add price to cache (optimistic update)
  addPriceToCache(newPrice: ListPriceDTO): void {
    const currentState = this.state();
    const updatedPrices = [newPrice, ...currentState.prices];

    this.state.set({
      ...currentState,
      prices: updatedPrices,
      totalElements: currentState.totalElements + 1
    });
  }
}
