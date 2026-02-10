import { Routes } from '@angular/router';
import { ProductFormComponent, ProductListComponent } from './components';

export const productsRoutes: Routes = [
  {
    path: '',
    component: ProductListComponent
  },
  {
    path: 'new',
    component: ProductFormComponent
  },
  {
    path: ':id',
    component: ProductFormComponent
  }
];
