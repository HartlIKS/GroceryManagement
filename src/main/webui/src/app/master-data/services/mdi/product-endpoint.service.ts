import { Injectable } from '@angular/core';
import { EndpointService } from './endpoint.service';
import { ProductEndpointDTOTypes } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ProductEndpointService extends EndpointService<ProductEndpointDTOTypes> {
  protected readonly endpointType = 'product';

  constructor() {
    super();
  }
}
