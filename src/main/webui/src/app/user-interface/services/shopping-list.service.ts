import { Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiParam, ApiService, CacheService } from '../../services';
import { ShoppingListTypes } from '../models';
import { Page } from '../../models';
import { CREATE, LIST } from '../../models/base.model';
import { HttpResourceRef } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService extends CacheService<ShoppingListTypes> {
  private readonly endpoint = '/shoppingLists';

  constructor(private apiService: ApiService) {
    super();
  }

  protected rawGet(uuid: string): HttpResourceRef<ShoppingListTypes[LIST] | undefined> {
    return this.apiService.getById<ShoppingListTypes[LIST]>(this.endpoint, uuid);
  }

  protected rawUpdate(uuid: string, shoppingList: ShoppingListTypes[CREATE]): Observable<ShoppingListTypes[LIST]> {
    return this.apiService.put<ShoppingListTypes[LIST]>(this.endpoint, uuid, shoppingList);
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
    return this.apiService.get<Page<ShoppingListTypes[LIST]>>(this.endpoint, {
      name,
      page,
      size
    });
  }

  // Get single shopping list by UUID
  getShoppingList(uuid: Signal<string> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ShoppingListTypes[LIST]>(this.endpoint, uuid);
    return this.get(uuid);
  }

  // Create shopping list
  createShoppingList(shoppingList: ShoppingListTypes[CREATE]): Observable<ShoppingListTypes[LIST]> {
    return this.apiService.post<ShoppingListTypes[LIST]>(this.endpoint, shoppingList);
  }

  deleteNonRepeating(uuid: string) {
    return this.delete(uuid, {
      ifNonRepeating: true,
    })
  }
}
