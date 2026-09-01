import { Always, BaseDTOTypes, CREATE, LIST, Mode, NotUpdate, UPDATE } from '../../models/base.model';

export type PriceTypes = Always<{
  validFrom: string,
  validTo: string,
  price: number,
}> & NotUpdate<{
  store: string,
  product: string,
}> & BaseDTOTypes;

export type Price<mode extends Mode = LIST> = PriceTypes[mode];

export type CreatePriceListingDTO = Price<CREATE>

export type UpdatePriceDTO = Price<UPDATE>

export type ListPriceDTO = Price;

export type PriceListingDTO = {
  listPriceUUID: string,
  price: number,
}
