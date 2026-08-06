import { computed, Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, GetApiEndpoint, NamedCacheService } from '../../services';
import { CreateStoreDTO, ListStoreDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class StoreService extends NamedCacheService<ListStoreDTO, CreateStoreDTO> {
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
    }, false);
  }

  // Get single store by UUID
  getStore(uuid: Signal<string | undefined> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListStoreDTO>(this.endpoint, uuid, false);
    return this.get(uuid);
  }

  getManyStores(uuids: Signal<string[] | undefined> | string[]) {
    return this.apiService.query<Record<string, ListStoreDTO>>(this.endpoint, uuids, false);
  }

  // Create store
  createStore(store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.post<ListStoreDTO>(this.endpoint, store);
  }
}
