import { EndpointDTO, CreateEndpointDTO } from './endpoint.model';

export type ProductEndpointDTO = EndpointDTO & {
  productIdPath: string;
  productNamePath: string;
  productImagePath: string;
  productEANPath: string;
}

export type CreateProductEndpointDTO = CreateEndpointDTO & {
  productIdPath: string;
  productNamePath: string;
  productImagePath: string;
  productEANPath: string;
}
