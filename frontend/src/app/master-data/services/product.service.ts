import { Injectable, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreateProductDTO, ListProductDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly endpoint = '/masterdata/product';

  constructor(private apiService: ApiService) {}

  // Get products with pagination and search
  getProducts(
    name: Signal<string> | string = '',
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20
  ) {
    return this.apiService.get<Page<ListProductDTO>>(this.endpoint, {
        name,
        page,
        size,
    });
  }

  // Get single product by UUID
  getProduct(uuid: Signal<string | undefined> | string) {
    return this.apiService.getById<ListProductDTO>(this.endpoint, uuid);
  }

  // Create product
  createProduct(product: CreateProductDTO): Observable<ListProductDTO> {
    return this.apiService.post<ListProductDTO>(this.endpoint, product);
  }

  // Update product
  updateProduct(uuid: string, product: CreateProductDTO): Observable<ListProductDTO> {
    return this.apiService.put<ListProductDTO>(this.endpoint, uuid, product);
  }

  // Delete product
  deleteProduct(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

}
