import { Injectable } from '@angular/core';
import { NamedCrudService } from '../../services';
import { ShoppingListTypes } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService extends NamedCrudService<ShoppingListTypes> {
  protected override readonly endpoint = '/shoppingLists';

  deleteNonRepeating(uuid: string) {
    return this.delete(uuid, {
      ifNonRepeating: true,
    })
  }
}
