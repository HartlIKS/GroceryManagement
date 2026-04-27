import { Injectable } from '@angular/core';
import { MappingTableService } from './mapping-table.service';

@Injectable({
  providedIn: 'root'
})
export class ProductMappingTableService extends MappingTableService {
  protected readonly endpoint1 = '/masterdata/interface';
  protected readonly endpoint2 = 'product';

  constructor() {
    super();
  }
}
