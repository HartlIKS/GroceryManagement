import { ParameterDTO } from './handling.model';
import { ResponseType } from './response-type.enum';
import { Always, BaseDTOTypes } from '../../../models/base.model';

export type EndpointDTOTypes = Always<{
  name: string,
  baseUrl: string,
  pageSize: ParameterDTO,
  page: ParameterDTO,
  itemCount: ParameterDTO,
  responseType: ResponseType,
  basePath: string,
}> & BaseDTOTypes;
