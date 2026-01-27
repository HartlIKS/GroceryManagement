import { AddressDTO } from './address.model';

export type Store = {
  uuid: string;
  name: string;
  logo: string;
  address: AddressDTO;
  currency: string;
}

export type CreateStoreDTO = {
  name: string;
  logo: string;
  address: AddressDTO;
  currency: string;
}

export type ListStoreDTO = Store;
