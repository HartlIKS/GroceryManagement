import { computed, Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, NamedCacheService } from '../../services';
import { CreateStoreDTO, ListStoreDTO, StoreTypes } from '../models';
import { Page } from '../../models';
import { HttpResourceRef } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class StoreService extends NamedCacheService<StoreTypes> {
  private readonly endpoint = '/masterdata/store';

  constructor(private apiService: ApiService) {
    super();
  }

  protected override rawGet(uuid: string): HttpResourceRef<ListStoreDTO | undefined> {
    return this.apiService.getById<ListStoreDTO>(this.endpoint, uuid);
  }

  protected override rawUpdate(uuid: string, store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.put<ListStoreDTO>(this.endpoint, uuid, store);
  }

  protected override rawDelete(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

// Get stores with pagination and search
  public override search(
    name: Signal<string> | string = '',
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20,
    suppress?: Signal<boolean>
  ) {
    const ep = suppress ? computed(() => suppress() ? undefined : this.endpoint) : this.endpoint;
    return this.apiService.get<Page<ListStoreDTO>>(ep, {
        name,
        page,
        size,
    });
  }

  // Get single store by UUID
  getStore(uuid: Signal<string | undefined> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListStoreDTO>(this.endpoint, uuid);
    return this.get(uuid);
  }

  getManyStores(uuids: Signal<string[] | undefined> | string[]) {
    return this.apiService.query<Record<string, ListStoreDTO>>(this.endpoint, uuids);
  }

  // Create store
  createStore(store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.post<ListStoreDTO>(this.endpoint, store);
  }
}
