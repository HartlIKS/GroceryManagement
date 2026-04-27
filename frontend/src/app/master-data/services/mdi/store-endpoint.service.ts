import { Injectable } from '@angular/core';
import { EndpointService } from './endpoint.service';
import { CreateStoreEndpointDTO, StoreEndpointDTO } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class StoreEndpointService extends EndpointService<StoreEndpointDTO, CreateStoreEndpointDTO> {
  protected readonly endpointType = 'store';

  constructor() {
    super();
  }
}
