import { Injectable } from '@angular/core';
import { NamedCrudService } from '../../services';
import { ProductTypes } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ProductService extends NamedCrudService<ProductTypes> {
  protected override readonly endpoint = '/masterdata/product';
}
