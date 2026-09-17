import { Injectable, ResourceParamsContext } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../services';
import { ListShoppingTripDTO, ShoppingTripTypes } from '../models';
import { Page } from '../../models';
import { httpResource } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ShoppingTripService extends CrudService<ShoppingTripTypes> {
  protected override readonly endpoint = '/shoppingTrips';

  search(q: (ctx: ResourceParamsContext) => {
    from?: Date,
    to?: Date,
    page?: number,
    size?: number,
  } | undefined) {
    return httpResource<Page<ListShoppingTripDTO>>(ctx => {
      const query = q(ctx);
      if(query) return this.apiService.get(this.endpoint, query);
      return undefined;
    });
  }

  addProducts(uuid: string, products: Record<string, number>): Observable<ListShoppingTripDTO> {
    return this.apiService.post<ListShoppingTripDTO>(`${this.endpoint}/${uuid}/add`, products);
  }
}
