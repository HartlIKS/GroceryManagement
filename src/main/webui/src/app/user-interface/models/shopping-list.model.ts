import { Always, BaseDTOTypes, CREATE, LIST, Mode } from '../../models/base.model';

export type ShoppingListTypes = Always<{
  name: string,
  repeating: boolean,
  products: Record<string, number>,
  productGroups: Record<string, number>,
}> & BaseDTOTypes;

export type ShoppingList<mode extends Mode = LIST> = ShoppingListTypes[mode];

export type CreateShoppingListDTO = ShoppingList<CREATE>;
