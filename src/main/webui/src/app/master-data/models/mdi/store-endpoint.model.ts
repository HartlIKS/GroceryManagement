import { AddressPathsDTO } from './address-paths.model';
import { EndpointDTO, CreateEndpointDTO } from './endpoint.model';

export type StoreEndpointDTO = EndpointDTO & {
  storeIdPath: string;
  storeNamePath: string;
  storeLogoPath: string;
  addressPath: string;
  addressPaths: AddressPathsDTO;
  storeCurrencyPath: string;
}

export type CreateStoreEndpointDTO = CreateEndpointDTO & {
  storeIdPath: string;
  storeNamePath: string;
  storeLogoPath: string;
  addressPath: string;
  addressPaths: AddressPathsDTO;
  storeCurrencyPath: string;
}
