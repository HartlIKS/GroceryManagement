import { Injectable, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreateProductGroupDTO, ListProductGroupDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ProductGroupService {
  private readonly endpoint = '/productGroups';

  constructor(private apiService: ApiService) {}

  // Get product groups with pagination and search
  getProductGroups(
    name: Signal<string> | string = '',
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20
  ) {
    return this.apiService.get<Page<ListProductGroupDTO>>(this.endpoint, {
      name,
      page,
      size
    });
  }

  // Get single product group by UUID
  getProductGroup(uuid: Signal<string> | string) {
    return this.apiService.getById<ListProductGroupDTO>(this.endpoint, uuid);
  }

  // Create product group
  createProductGroup(productGroup: CreateProductGroupDTO): Observable<ListProductGroupDTO> {
    return this.apiService.post<ListProductGroupDTO>(this.endpoint, productGroup);
  }

  // Update product group
  updateProductGroup(uuid: string, productGroup: CreateProductGroupDTO): Observable<ListProductGroupDTO> {
    return this.apiService.put<ListProductGroupDTO>(this.endpoint, uuid, productGroup);
  }

  // Delete product group
  deleteProductGroup(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

}
