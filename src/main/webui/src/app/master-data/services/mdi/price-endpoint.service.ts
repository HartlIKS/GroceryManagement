import { Injectable } from '@angular/core';
import { EndpointService } from './endpoint.service';
import { PriceEndpointDTOTypes } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class PriceEndpointService extends EndpointService<PriceEndpointDTOTypes> {
  protected readonly endpointType = 'price';
}
