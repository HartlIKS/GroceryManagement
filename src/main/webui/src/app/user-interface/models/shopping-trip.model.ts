import { Always, BaseDTOTypes, CREATE, LIST, Mode } from '../../models/base.model';

export type ShoppingTripTypes = Always<{
  store: string,
  time: string,
  products: Record<string, number>,
}> & BaseDTOTypes;

export type ShoppingTrip<mode extends Mode> = ShoppingTripTypes[mode];

export type CreateShoppingTripDTO = ShoppingTrip<CREATE>;

export type ListShoppingTripDTO = ShoppingTrip<LIST>;
