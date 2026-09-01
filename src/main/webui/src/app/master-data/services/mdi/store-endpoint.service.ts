import { Injectable } from '@angular/core';
import { EndpointService } from './endpoint.service';
import { StoreEndpointDTOTypes } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class StoreEndpointService extends EndpointService<StoreEndpointDTOTypes> {
  protected readonly endpointType = 'store';

  constructor() {
    super();
  }
}
