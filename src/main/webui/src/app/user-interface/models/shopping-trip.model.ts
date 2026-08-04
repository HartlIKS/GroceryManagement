export type ShoppingTrip = {
  uuid: string;
  store: string;
  time: string;
  products: Record<string, number>;
}

export type CreateShoppingTripDTO = {
  store: string;
  time: string;
  products: Record<string, number>;
}

export type ListShoppingTripDTO = ShoppingTrip;
