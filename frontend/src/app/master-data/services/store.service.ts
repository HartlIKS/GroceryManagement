import { Injectable, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreateStoreDTO, ListStoreDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private readonly endpoint = '/masterdata/store';

  constructor(private apiService: ApiService) {}

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
    });
  }

  // Get single store by UUID
  getStore(uuid: Signal<string> | string) {
    return this.apiService.getById<ListStoreDTO>(this.endpoint, uuid);
  }

  // Create store
  createStore(store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.post<ListStoreDTO>(this.endpoint, store);
  }

  // Update store
  updateStore(uuid: string, store: CreateStoreDTO): Observable<ListStoreDTO> {
    return this.apiService.put<ListStoreDTO>(this.endpoint, uuid, store);
  }

  // Delete store
  deleteStore(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

}
