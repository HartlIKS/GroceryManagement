import { ProductHandlingDTO, StoreHandlingDTO } from './handling.model';
import { EndpointDTO, CreateEndpointDTO } from './endpoint.model';

export type PriceEndpointDTO = EndpointDTO & {
  productHandling: ProductHandlingDTO;
  storeHandling: StoreHandlingDTO;
  pricePath: string;
  timeFormat: string;
  validFromPath: string;
  validUntilPath: string;
}

export type CreatePriceEndpointDTO = CreateEndpointDTO & {
  productHandling: ProductHandlingDTO;
  storeHandling: StoreHandlingDTO;
  pricePath: string;
  timeFormat: string;
  validFromPath: string;
  validUntilPath: string;
}
