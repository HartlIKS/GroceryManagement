import { Always, BaseDTOTypes, CREATE, LIST, Mode } from '../../../models/base.model';

export type ExternalAPIDTOTypes = Always<{
  name: string,
}> & BaseDTOTypes;

export type ExternalAPIDTO<mode extends Mode = LIST> = ExternalAPIDTOTypes[mode];

export type CreateExternalAPIDTO = ExternalAPIDTO<CREATE>;
