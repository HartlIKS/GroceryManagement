import { Injectable, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreateShoppingTripDTO, ListShoppingTripDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingTripService {
  private readonly endpoint = '/shoppingTrips';

  constructor(private apiService: ApiService) {}

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

}
