import { computed, Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, NamedCacheService } from '../../services';
import { CreateProductDTO, ListProductDTO, ProductTypes } from '../models';
import { Page } from '../../models';
import { HttpResourceRef } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ProductService extends NamedCacheService<ProductTypes> {
  private readonly endpoint = '/masterdata/product';

  constructor(private apiService: ApiService) {
    super();
  }

  protected override rawGet(uuid: string): HttpResourceRef<ListProductDTO | undefined> {
    return this.apiService.getById<ListProductDTO>(this.endpoint, uuid);
  }

  protected override rawUpdate(uuid: string, product: CreateProductDTO): Observable<ListProductDTO> {
    return this.apiService.put<ListProductDTO>(this.endpoint, uuid, product);
  }

  protected override rawDelete(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

// Get products with pagination and search
  public override search(
    name: Signal<string> | string = '',
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20,
    suppress?: Signal<boolean>
  ) {
    const ep = suppress ? computed(() => suppress() ? undefined : this.endpoint) : this.endpoint;
    return this.apiService.get<Page<ListProductDTO>>(ep, {
        name,
        page,
        size,
    });
  }

  // Get single product by UUID
  getProduct(uuid: Signal<string | undefined> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListProductDTO>(this.endpoint, uuid);
    return this.get(uuid);
  }

  getManyProducts(uuids: Signal<string[] | undefined> | string[]) {
    return this.apiService.query<Record<string, ListProductDTO>>(this.endpoint, uuids);
  }

  // Create product
  createProduct(product: CreateProductDTO): Observable<ListProductDTO> {
    return this.apiService.post<ListProductDTO>(this.endpoint, product);
  }
}
