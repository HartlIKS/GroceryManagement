export type ParameterDTO = {
  header: string;
  queryParameter: string;
}

export type PathDTO = {
  type: 'path';
  path: string;
}

export type WrappedParameterDTO = {
  type: 'parameter',
} & ParameterDTO;

export type OneForAllDTO = {
  type: 'oneForAll';
}

export type ProductHandlingDTO = WrappedParameterDTO | PathDTO;

export type StoreHandlingDTO = WrappedParameterDTO | PathDTO | OneForAllDTO;
