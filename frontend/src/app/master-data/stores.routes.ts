import { Routes } from '@angular/router';
import { StoreFormComponent, StoreListComponent } from './components/stores';

export const storesRoutes: Routes = [
  {
    path: '',
    component: StoreListComponent
  },
  {
    path: 'new',
    component: StoreFormComponent
  },
  {
    path: ':id',
    component: StoreFormComponent
  }
];
