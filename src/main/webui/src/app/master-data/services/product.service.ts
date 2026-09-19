import { Injectable } from '@angular/core';
import { NamedCrudService } from '../../services';
import { ProductTypes } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ProductService extends NamedCrudService<ProductTypes, {
  name: string,
  hasEAN?: boolean,
}> {
  protected override readonly endpoint = '/masterdata/product';
}
