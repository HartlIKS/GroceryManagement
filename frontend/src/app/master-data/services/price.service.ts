import { Injectable, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreatePriceListingDTO, ListPriceDTO, PriceListingDTO, UpdatePriceDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class PriceService {
  private readonly endpoint = '/masterdata/price';

  constructor(private apiService: ApiService) {}

  // Get prices with pagination and filtering
  getPrices(
    page: Signal<number> | number,
    size: Signal<number> | number,
    store?: Signal<string | undefined> | string,
    product?: Signal<string | undefined> | string,
  ) {
    return this.apiService.get<Page<ListPriceDTO>>(this.endpoint, {
        page,
        size,
        store,
        product,
    });
  }

  // Get single price by UUID
  getPrice(uuid: Signal<string | undefined> | string) {
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


  // Search prices for specific products and stores at a specific time
  searchPricesForProductsAndStores(
    products: Signal<string[]> | string[],
    stores: Signal<string[]> | string[],
    at: Signal<Date> | Date
  ) {
    return this.apiService.get<Record<string, Record<string, PriceListingDTO[]>>>(this.endpoint, {
      products,
      stores,
      at
    });
  }
}
