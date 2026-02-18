import { Injectable, isSignal, Signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService, CacheService, GetApiEndpoint } from '../../services';
import { CreateShoppingTripDTO, ListShoppingTripDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingTripService extends CacheService<ListShoppingTripDTO, CreateShoppingTripDTO> {
  private readonly endpoint = '/shoppingTrips';

  constructor(private apiService: ApiService) {
    super();
  }

  protected rawGet(uuid: string): GetApiEndpoint<ListShoppingTripDTO> {
    return this.apiService.getById<ListShoppingTripDTO>(this.endpoint, uuid);
  }

  protected rawUpdate(uuid: string, shoppingTrip: CreateShoppingTripDTO): Observable<ListShoppingTripDTO> {
    return this.apiService.put<ListShoppingTripDTO>(this.endpoint, uuid, shoppingTrip);
  }

  protected rawDelete(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

  // Get shopping trips with pagination and date filtering
  getShoppingTrips(
    from: Signal<Date | undefined> | Date | undefined = undefined,
    to: Signal<Date | undefined> | Date | undefined = undefined,
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20
  ) {
    return this.apiService.get<Page<ListShoppingTripDTO>>(this.endpoint, {
      from,
      to,
      page,
      size
    });
  }

  // Get single shopping trip by UUID
  getShoppingTrip(uuid: Signal<string> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListShoppingTripDTO>(this.endpoint, uuid);
    return this.get(uuid);
  }

  // Create shopping trip
  createShoppingTrip(shoppingTrip: CreateShoppingTripDTO): Observable<ListShoppingTripDTO> {
    return this.apiService.post<ListShoppingTripDTO>(this.endpoint, shoppingTrip);
  }

  addProducts(uuid: string, products: Record<string, number>): Observable<ListShoppingTripDTO> {
    return this.apiService.post<ListShoppingTripDTO>(`${this.endpoint}/${uuid}/add`, products).pipe(
      tap(v => this.postUpdate(uuid, v))
    );
  }
}
