import { Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, CacheService, GetApiEndpoint } from '../../services';
import { CreateStoreDTO, ListStoreDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class StoreService extends CacheService<ListStoreDTO, CreateStoreDTO> {
  private readonly endpoint = '/masterdata/store';

  constructor(private apiService: ApiService) {
    super();
  }

  protected override rawGet(uuid: string): GetApiEndpoint<ListStoreDTO> {
    return this.apiService.getById<ListStoreDTO>(this.endpoint, uuid, false);
  }

  protected override rawUpdate(uuid: string, store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.put<ListStoreDTO>(this.endpoint, uuid, store);
  }

  protected override rawDelete(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

// Get stores with pagination and search
  getStores(
    name: Signal<string> | string = '',
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20
  ) {
    return this.apiService.get<Page<ListStoreDTO>>(this.endpoint, {
        name,
        page,
        size,
    }, false);
  }

  // Get single store by UUID
  getStore(uuid: Signal<string | undefined> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListStoreDTO>(this.endpoint, uuid, false);
    return this.get(uuid);
  }

  // Create store
  createStore(store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.post<ListStoreDTO>(this.endpoint, store);
  }
}
