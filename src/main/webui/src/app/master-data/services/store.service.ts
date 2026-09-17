import { Injectable } from '@angular/core';
import { NamedCrudService } from '../../services';
import { StoreTypes } from '../models';

@Injectable({
  providedIn: 'root'
})
export class StoreService extends NamedCrudService<StoreTypes> {
  protected override readonly endpoint = '/masterdata/store';
}
