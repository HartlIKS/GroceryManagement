import { computed, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CreatePriceListingDTO, ListPriceDTO, Page, UpdatePriceDTO } from '../models';

interface PriceState {
  prices: ListPriceDTO[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  searchTerm: string;
}

@Injectable({
  providedIn: 'root'
})
export class PriceService {
  private readonly endpoint = '/price';

  // Private state signal
  private readonly state = signal<PriceState>({
    prices: [],
    totalElements: 0,
    currentPage: 0,
    pageSize: 20,
    searchTerm: ''
  });

  // Public computed signals
  public readonly prices = computed(() => this.state().prices);
  public readonly totalElements = computed(() => this.state().totalElements);
  public readonly currentPage = computed(() => this.state().currentPage);
  public readonly pageSize = computed(() => this.state().pageSize);
  public readonly searchTerm = computed(() => this.state().searchTerm);
  public readonly loading = computed(() => this.apiService.loading());
  public readonly error = computed(() => this.apiService.error());

  constructor(private apiService: ApiService) {}

  // Get prices with pagination
  getPrices(page: number = 0, size: number = 20): void {
    const currentState = this.state();

    // Only fetch if parameters have changed or cache is empty
    if (currentState.currentPage === page &&
        currentState.pageSize === size &&
        currentState.prices.length > 0) {
      return;
    }

    this.apiService.get<Page<ListPriceDTO>>(this.endpoint, { page, size }).subscribe({
      next: (response: Page<ListPriceDTO>) => {
        this.state.set({
          prices: response.content || [],
          totalElements: response.page.totalElements || 0,
          currentPage: response.page.number || 0,
          pageSize: response.page.size || size,
          searchTerm: currentState.searchTerm
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
      searchTerm: ''
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
