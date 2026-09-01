import { AddressDTO } from './address.model';
import { Always, BaseDTOTypes, CREATE, LIST, Mode } from '../../models/base.model';

export type StoreTypes = Always<{
  name: string,
  logo: string,
  address: AddressDTO,
  currency: string,
}> & BaseDTOTypes;

export type Store<mode extends Mode> = StoreTypes[mode];

export type CreateStoreDTO = Store<CREATE>;

export type ListStoreDTO = Store<LIST>;
