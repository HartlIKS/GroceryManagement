export type Product = {
  uuid: string;
  name: string;
  image: string;
  EAN: string;
}

export type CreateProductDTO = {
  name: string;
  image: string;
  EAN: string;
}

export type ListProductDTO = Product;
