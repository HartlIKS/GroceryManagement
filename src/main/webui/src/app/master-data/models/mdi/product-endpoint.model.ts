import { EndpointDTOTypes } from './endpoint.model';
import { Always, CREATE, LIST, Mode } from '../../../models/base.model';

export type ProductEndpointDTOTypes = Always<{
  productIdPath: string,
  productNamePath: string,
  productImagePath: string,
  productEANPath: string,
}> & EndpointDTOTypes;

export type ProductEndpointDTO<mode extends Mode = LIST> = ProductEndpointDTOTypes[mode];

export type CreateProductEndpointDTO = ProductEndpointDTO<CREATE>;
