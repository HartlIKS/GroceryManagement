import { Injectable } from '@angular/core';
import { EndpointService } from './endpoint.service';
import { CreateProductEndpointDTO, ProductEndpointDTO } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class ProductEndpointService extends EndpointService<ProductEndpointDTO, CreateProductEndpointDTO> {
  protected readonly endpointType = 'product';

  constructor() {
    super();
  }
}
