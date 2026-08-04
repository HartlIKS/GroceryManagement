import { Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, CacheService, GetApiEndpoint } from '../../services';
import { CreatePriceListingDTO, ListPriceDTO, PriceListingDTO, UpdatePriceDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class PriceService extends CacheService<ListPriceDTO, UpdatePriceDTO> {
  private readonly endpoint = '/masterdata/price';

  constructor(private apiService: ApiService) {
    super();
  }

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
    }, false);
  }

  protected override rawGet(uuid: string): GetApiEndpoint<ListPriceDTO> {
    return this.apiService.getById<ListPriceDTO>(this.endpoint, uuid, false);
  }

  protected override rawUpdate(uuid: string, price: UpdatePriceDTO): Observable<ListPriceDTO> {
    return this.apiService.put<ListPriceDTO>(this.endpoint, uuid, price);
  }

  protected override rawDelete(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

// Get single price by UUID
  getPrice(uuid: Signal<string | undefined> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListPriceDTO>(this.endpoint, uuid, false);
    return this.get(uuid);
  }

  // Create price
  createPrice(price: CreatePriceListingDTO): Observable<ListPriceDTO> {
    return this.apiService.post<ListPriceDTO>(this.endpoint, price);
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
    }, false);
  }
}
