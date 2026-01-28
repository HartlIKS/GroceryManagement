export type ProductGroup = {
  uuid: string;
  name: string;
  products: string[];
}

export type CreateProductGroupDTO = {
  name: string;
}

export type ListProductGroupDTO = ProductGroup;
