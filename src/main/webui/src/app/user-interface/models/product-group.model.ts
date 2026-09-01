import { Always, BaseDTOTypes, CREATE, LIST, Mode } from '../../models/base.model';

export type ProductGroupTypes = Always<{
  name: string,
  products: Record<string, number>,
}> & BaseDTOTypes;

export type ProductGroup<mode extends Mode> = ProductGroupTypes[mode];

export type CreateProductGroupDTO = ProductGroup<CREATE>;

export type ListProductGroupDTO = ProductGroup<LIST>;
