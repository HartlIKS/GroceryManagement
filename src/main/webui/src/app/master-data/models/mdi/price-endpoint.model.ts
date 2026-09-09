import { ProductHandlingDTO, StoreHandlingDTO } from './handling.model';
import { EndpointDTOTypes } from './endpoint.model';
import { Always, CREATE, LIST, Mode } from '../../../models/base.model';

export type PriceEndpointDTOTypes = Always<{
  useEAN: boolean,
  productHandling: ProductHandlingDTO,
  storeHandling: StoreHandlingDTO,
  pricePath: string,
  timeFormat: string,
  validFromPath: string,
  validUntilPath: string,
}> & EndpointDTOTypes;

export type PriceEndpointDTO<mode extends Mode = LIST> = PriceEndpointDTOTypes[mode];

export type CreatePriceEndpointDTO = PriceEndpointDTO<CREATE>;
