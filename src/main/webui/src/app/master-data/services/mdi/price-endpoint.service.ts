import { Injectable } from '@angular/core';
import { EndpointService } from './endpoint.service';
import { CreatePriceEndpointDTO, PriceEndpointDTO } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class PriceEndpointService extends EndpointService<PriceEndpointDTO, CreatePriceEndpointDTO> {
  protected readonly endpointType = 'price';

  constructor() {
    super();
  }
}
