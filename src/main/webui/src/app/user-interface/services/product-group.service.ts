import { Injectable } from '@angular/core';
import { NamedCrudService } from '../../services';
import { ProductGroupTypes } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ProductGroupService extends NamedCrudService<ProductGroupTypes> {
  protected override readonly endpoint = '/productGroups';
}
