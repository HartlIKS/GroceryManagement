import { Injectable, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreateShoppingListDTO, ListShoppingListDTO } from '../models';
import { Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService {
  private readonly endpoint = '/shoppingLists';

  constructor(private apiService: ApiService) {}

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
    return this.apiService.getById<ListShoppingListDTO>(this.endpoint, uuid);
  }

  // Create shopping list
  createShoppingList(shoppingList: CreateShoppingListDTO): Observable<ListShoppingListDTO> {
    return this.apiService.post<ListShoppingListDTO>(this.endpoint, shoppingList);
  }

  // Update shopping list
  updateShoppingList(uuid: string, shoppingList: CreateShoppingListDTO): Observable<ListShoppingListDTO> {
    return this.apiService.put<ListShoppingListDTO>(this.endpoint, uuid, shoppingList);
  }

  // Delete shopping list
  deleteShoppingList(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

}
