export type Price = {
  uuid: string;
  store: string;
  product: string;
  validFrom: string;
  validTo: string;
  price: number;
}

export type CreatePriceListingDTO = {
  store: string;
  product: string;
  validFrom: string;
  validTo: string;
  price: number;
}

export type UpdatePriceDTO = {
  price?: number;
  validFrom?: string;
  validTo?: string;
}

export type ListPriceDTO = Price;
