import { Routes } from '@angular/router';
import { PriceFormComponent, PriceListComponent } from './components/prices';

export const pricesRoutes: Routes = [
  {
    path: '',
    component: PriceListComponent
  },
  {
    path: 'new',
    component: PriceFormComponent
  },
  {
    path: ':id',
    component: PriceFormComponent
  }
];
