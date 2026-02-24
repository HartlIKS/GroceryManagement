import { Routes } from '@angular/router';
import { ShoppingListFormComponent, ShoppingListListComponent } from './components/shopping-lists';

export const shoppingListsRoutes: Routes = [
  {
    path: '',
    component: ShoppingListListComponent
  },
  {
    path: 'new',
    component: ShoppingListFormComponent
  },
  {
    path: ':id',
    component: ShoppingListFormComponent
  }
];
