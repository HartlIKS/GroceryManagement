export type ProductGroup = {
  uuid: string;
  name: string;
  products: Record<string, number>;
}

export type CreateProductGroupDTO = {
  name?: string;
  products?: Record<string, number>;
}

export type ListProductGroupDTO = ProductGroup;
