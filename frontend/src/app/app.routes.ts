import { Routes } from '@angular/router';
import {
  PriceFormComponent,
  PriceListComponent,
  ProductFormComponent,
  ProductListComponent,
  StoreFormComponent,
  StoreListComponent
} from './components';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'products',
    component: ProductListComponent
  },
  {
    path: 'products/new',
    component: ProductFormComponent
  },
  {
    path: 'products/:id',
    component: ProductFormComponent
  },
  {
    path: 'stores',
    component: StoreListComponent
  },
  {
    path: 'stores/new',
    component: StoreFormComponent
  },
  {
    path: 'stores/:id',
    component: StoreFormComponent
  },
  {
    path: 'prices',
    component: PriceListComponent
  },
  {
    path: 'prices/new',
    component: PriceFormComponent
  },
  {
    path: 'prices/:id',
    component: PriceFormComponent
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
