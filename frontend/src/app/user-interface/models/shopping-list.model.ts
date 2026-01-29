export type ShoppingList = {
  uuid: string;
  name: string;
  products: Record<string, number>;
  productGroups: Record<string, number>;
}

export type CreateShoppingListDTO = {
  name?: string;
  products?: Record<string, number>;
  productGroups?: Record<string, number>;
}

export type ListShoppingListDTO = ShoppingList;
