import { Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, CacheService, GetApiEndpoint } from '../../services';
import { CreateProductGroupDTO, ListProductGroupDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ProductGroupService extends CacheService<ListProductGroupDTO, CreateProductGroupDTO> {
  private readonly endpoint = '/productGroups';

  constructor(private apiService: ApiService) {
    super();
  }

  protected override rawGet(uuid: string): GetApiEndpoint<ListProductGroupDTO> {
    return this.apiService.getById<ListProductGroupDTO>(this.endpoint, uuid);
  }

  protected override rawUpdate(uuid: string, productGroup: CreateProductGroupDTO): Observable<ListProductGroupDTO> {
    return this.apiService.put<ListProductGroupDTO>(this.endpoint, uuid, productGroup);
  }

  protected override rawDelete(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

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
  getProductGroup(uuid: Signal<string | undefined> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListProductGroupDTO>(this.endpoint, uuid);
    return this.get(uuid);
  }

  // Create product group
  createProductGroup(productGroup: CreateProductGroupDTO): Observable<ListProductGroupDTO> {
    return this.apiService.post<ListProductGroupDTO>(this.endpoint, productGroup);
  }
}
