import { Injectable, ResourceParamsContext } from '@angular/core';
import { MappingTableService } from './mapping-table.service';
import { httpResource } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ProductMappingTableService extends MappingTableService {
  protected readonly endpoint1 = '/masterdata/interface';
  protected readonly endpoint2 = 'product';

  search(q: (ctx: ResourceParamsContext) => {
    api: string,
    name: string,
  } | undefined) {
    return httpResource<Record<string, string>>(ctx => {
      const query = q(ctx);
      if(!query) return undefined;
      const {api, ...rest} = query;
      return this.apiService.get(`${this.endpoint1}/${api}/mapping/${this.endpoint2}/search`, rest);
    })
  }
}
