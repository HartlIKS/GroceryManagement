import { Always, BaseDTOTypes, CREATE, LIST, Mode } from '../../models/base.model';

export type ProductTypes = Always<{
  name: string,
  image: string,
  EAN: string,
}> & BaseDTOTypes;

export type Product<mode extends Mode> = ProductTypes[mode];

export type CreateProductDTO = Product<CREATE>;

export type ListProductDTO = Product<LIST>;
