import { computed, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreateShoppingTripDTO, ListShoppingTripDTO } from '../models';
import { Page } from '../../models';

interface ShoppingTripState {
  shoppingTrips: ListShoppingTripDTO[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  fromDate: string;
  toDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class ShoppingTripService {
  private readonly endpoint = '/shoppingTrips';

  // Private state signal
  private readonly state = signal<ShoppingTripState>({
    shoppingTrips: [],
    totalElements: 0,
    currentPage: 0,
    pageSize: 20,
    fromDate: '',
    toDate: ''
  });

  // Public computed signals
  public readonly shoppingTrips = computed(() => this.state().shoppingTrips);
  public readonly totalElements = computed(() => this.state().totalElements);
  public readonly currentPage = computed(() => this.state().currentPage);
  public readonly pageSize = computed(() => this.state().pageSize);
  public readonly fromDate = computed(() => this.state().fromDate);
  public readonly toDate = computed(() => this.state().toDate);
  public readonly loading = computed(() => this.apiService.loading());
  public readonly error = computed(() => this.apiService.error());

  constructor(private apiService: ApiService) {}

  // Get shopping trips with caching
  getShoppingTrips(from: string = '', to: string = '', page: number = 0, size: number = 20): void {
    this.apiService.get<Page<ListShoppingTripDTO>>(this.endpoint, { from, to, page, size }).subscribe({
      next: (response: Page<ListShoppingTripDTO>) => {
        this.state.set({
          shoppingTrips: response.content || [],
          totalElements: response.page.totalElements || 0,
          currentPage: response.page.number || 0,
          pageSize: response.page.size || size,
          fromDate: from,
          toDate: to
        });
      },
      error: (error: any) => {
        console.error('Error loading shopping trips:', error);
      }
    });
  }

  // Get single shopping trip
  getShoppingTrip(uuid: string): Observable<ListShoppingTripDTO> {
    return this.apiService.getById<ListShoppingTripDTO>(this.endpoint, uuid);
  }

  // Create shopping trip
  createShoppingTrip(shoppingTrip: CreateShoppingTripDTO): Observable<ListShoppingTripDTO> {
    return this.apiService.post<ListShoppingTripDTO>(this.endpoint, shoppingTrip);
  }

  // Update shopping trip
  updateShoppingTrip(uuid: string, shoppingTrip: CreateShoppingTripDTO): Observable<ListShoppingTripDTO> {
    return this.apiService.put<ListShoppingTripDTO>(this.endpoint, uuid, shoppingTrip);
  }

  // Delete shopping trip
  deleteShoppingTrip(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

  // Refresh current shopping trip list
  refreshShoppingTrips(): void {
    const currentState = this.state();
    this.getShoppingTrips(currentState.fromDate, currentState.toDate, currentState.currentPage, currentState.pageSize);
  }

  // Clear cache
  clearCache(): void {
    this.state.set({
      shoppingTrips: [],
      totalElements: 0,
      currentPage: 0,
      pageSize: 20,
      fromDate: '',
      toDate: ''
    });
  }

  // Get shopping trip by UUID from cache
  getShoppingTripByUuid(uuid: string): ListShoppingTripDTO | undefined {
    return this.state().shoppingTrips.find(trip => trip.uuid === uuid);
  }

  // Update shopping trip in cache (optimistic update)
  updateShoppingTripInCache(updatedShoppingTrip: ListShoppingTripDTO): void {
    const currentState = this.state();
    const updatedShoppingTrips = currentState.shoppingTrips.map(trip =>
      trip.uuid === updatedShoppingTrip.uuid ? updatedShoppingTrip : trip
    );

    this.state.set({
      ...currentState,
      shoppingTrips: updatedShoppingTrips
    });
  }

  // Remove shopping trip from cache (optimistic update)
  removeShoppingTripFromCache(uuid: string): void {
    const currentState = this.state();
    const updatedShoppingTrips = currentState.shoppingTrips.filter(trip => trip.uuid !== uuid);

    this.state.set({
      ...currentState,
      shoppingTrips: updatedShoppingTrips,
      totalElements: Math.max(0, currentState.totalElements - 1)
    });
  }

  // Add shopping trip to cache (optimistic update)
  addShoppingTripToCache(newShoppingTrip: ListShoppingTripDTO): void {
    const currentState = this.state();
    const updatedShoppingTrips = [newShoppingTrip, ...currentState.shoppingTrips];

    this.state.set({
      ...currentState,
      shoppingTrips: updatedShoppingTrips,
      totalElements: currentState.totalElements + 1
    });
  }
}
