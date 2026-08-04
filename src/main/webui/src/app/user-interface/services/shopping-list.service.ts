import { Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiParam, ApiService, CacheService, GetApiEndpoint } from '../../services';
import { CreateShoppingListDTO, ListShoppingListDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService extends CacheService<ListShoppingListDTO, CreateShoppingListDTO> {
  private readonly endpoint = '/shoppingLists';

  constructor(private apiService: ApiService) {
    super();
  }

  protected rawGet(uuid: string): GetApiEndpoint<ListShoppingListDTO> {
    return this.apiService.getById<ListShoppingListDTO>(this.endpoint, uuid);
  }

  protected rawUpdate(uuid: string, shoppingList: CreateShoppingListDTO): Observable<ListShoppingListDTO> {
    return this.apiService.put<ListShoppingListDTO>(this.endpoint, uuid, shoppingList);
  }

  protected rawDelete(uuid: string, params?: Record<string, ApiParam>): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid, params);
  }

  // Get shopping lists with pagination and search
  getShoppingLists(
    name: Signal<string> | string = '',
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20
  ) {
    return this.apiService.get<Page<ListShoppingListDTO>>(this.endpoint, {
      name,
      page,
      size
    });
  }

  // Get single shopping list by UUID
  getShoppingList(uuid: Signal<string> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ListShoppingListDTO>(this.endpoint, uuid);
    return this.get(uuid);
  }

  // Create shopping list
  createShoppingList(shoppingList: CreateShoppingListDTO): Observable<ListShoppingListDTO> {
    return this.apiService.post<ListShoppingListDTO>(this.endpoint, shoppingList);
  }

  deleteNonRepeating(uuid: string) {
    return this.delete(uuid, {
      ifNonRepeating: true,
    })
  }
}
