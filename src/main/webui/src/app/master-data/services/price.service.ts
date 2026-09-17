import { Injectable, ResourceParamsContext } from '@angular/core';
import { CrudService } from '../../services';
import { ListPriceDTO, PriceListingDTO, PriceTypes } from '../models';
import { Page } from '../../models';
import { httpResource, HttpResourceRef } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PriceService extends CrudService<PriceTypes> {
  protected override readonly endpoint = '/masterdata/price';

  search(q: (ctx: ResourceParamsContext) => {
    page: number,
    size: number,
    store?: string | string[],
    product?: string | string[],
  } | undefined): HttpResourceRef<Page<ListPriceDTO> | undefined>;
  search(q: (ctx: ResourceParamsContext) => {
    products: string[],
    stores: string[],
    at: Date,
  } | undefined): HttpResourceRef<Record<string, Record<string, PriceListingDTO[]>> | undefined>;
  search(q: (ctx: ResourceParamsContext) => {
    page: number,
    size: number,
    store?: string | string[],
    product?: string | string[],
  } | {
    products: string[],
    stores: string[],
    at: Date,
  } | undefined) {
    return httpResource(ctx => {
      const query = q(ctx);
      if(query) return this.apiService.get(this.endpoint, query);
      return undefined;
    });
  }
}
