import { Routes } from '@angular/router';
import { ProductGroupFormComponent, ProductGroupListComponent } from './components/product-groups';

export const productGroupsRoutes: Routes = [
  {
    path: '',
    component: ProductGroupListComponent
  },
  {
    path: 'new',
    component: ProductGroupFormComponent
  },
  {
    path: ':id',
    component: ProductGroupFormComponent
  }
];
