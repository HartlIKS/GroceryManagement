import { Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, GetApiEndpoint, NamedCacheService } from '../../services';
import { CreateProductDTO, ListProductDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ProductService extends NamedCacheService<ListProductDTO, CreateProductDTO> {
  private readonly endpoint = '/masterdata/product';

  constructor(private apiService: ApiService) {
    super();
  }

  protected override rawGet(uuid: string): GetApiEndpoint<ListProductDTO> {
    return this.apiService.getById<ListProductDTO>(this.endpoint, uuid, false);
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
    size: Signal<number> | number = 20
  ) {
    return this.apiService.get<Page<ListProductDTO>>(this.endpoint, {
        name,
        page,
        size,
    }, false);
  }

  // Get single product by UUID
  getProduct(uuid: Signal<string | undefined> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListProductDTO>(this.endpoint, uuid, false);
    return this.get(uuid);
  }

  // Create product
  createProduct(product: CreateProductDTO): Observable<ListProductDTO> {
    return this.apiService.post<ListProductDTO>(this.endpoint, product);
  }
}
