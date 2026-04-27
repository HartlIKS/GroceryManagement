import { ParameterDTO } from './handling.model';

export type EndpointDTO = {
  uuid: string;
  name: string;
  baseUrl: string;
  pageSize: ParameterDTO;
  page: ParameterDTO;
  itemCount: ParameterDTO;
  basePath: string;
}

export type CreateEndpointDTO = {
  name: string;
  baseUrl: string;
  pageSize: ParameterDTO;
  page: ParameterDTO;
  itemCount: ParameterDTO;
  basePath: string;
}
