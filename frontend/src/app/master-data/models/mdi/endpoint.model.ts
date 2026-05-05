import { ParameterDTO } from './handling.model';
import { ResponseType } from './response-type.enum';

export type EndpointDTO = {
  uuid: string;
  name: string;
  baseUrl: string;
  pageSize: ParameterDTO;
  page: ParameterDTO;
  itemCount: ParameterDTO;
  responseType: ResponseType;
  basePath: string;
}

export type CreateEndpointDTO = {
  name: string;
  baseUrl: string;
  pageSize: ParameterDTO;
  page: ParameterDTO;
  itemCount: ParameterDTO;
  responseType: ResponseType;
  basePath: string;
}
