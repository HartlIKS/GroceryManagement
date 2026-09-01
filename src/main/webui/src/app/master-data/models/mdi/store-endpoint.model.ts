import { AddressPathsDTO } from './address-paths.model';
import { EndpointDTOTypes } from './endpoint.model';
import { Always, CREATE, LIST, Mode } from '../../../models/base.model';

export type StoreEndpointDTOTypes = Always<{
  storeIdPath: string,
  storeNamePath: string,
  storeLogoPath: string,
  addressPath: string,
  addressPaths: AddressPathsDTO,
  storeCurrencyPath: string,
}> & EndpointDTOTypes;

export type StoreEndpointDTO<mode extends Mode = LIST> = StoreEndpointDTOTypes[mode];

export type CreateStoreEndpointDTO = StoreEndpointDTO<CREATE>;
