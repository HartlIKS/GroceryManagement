export type ShoppingList = {
  uuid: string;
  name: string;
  repeating: boolean;
  products: Record<string, number>;
  productGroups: Record<string, number>;
}

export type CreateShoppingListDTO = {
  name?: string;
  repeating?: boolean;
  products?: Record<string, number>;
  productGroups?: Record<string, number>;
}

export type ListShoppingListDTO = ShoppingList;
