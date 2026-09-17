import { Injectable } from '@angular/core';
import { NamedCrudService } from '../../../services';
import { ExternalAPIDTOTypes } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ExternalAPIService extends NamedCrudService<ExternalAPIDTOTypes> {
  protected override readonly endpoint = '/masterdata/interface';
}
