import { Routes } from '@angular/router';
import {
  ExternalAPIFormComponent,
  ExternalAPIListComponent,
  PriceEndpointFormComponent,
  ProductEndpointFormComponent,
  StoreEndpointFormComponent
} from './components/endpoints';

export const externalAPIRoutes: Routes = [
  {
    path: '',
    component: ExternalAPIListComponent
  },
  {
    path: 'new',
    component: ExternalAPIFormComponent
  },
  {
    path: ':id',
    component: ExternalAPIFormComponent
  },
  {
    path: ':id/price-endpoint/new',
    component: PriceEndpointFormComponent
  },
  {
    path: ':id/price-endpoint/:endpointId',
    component: PriceEndpointFormComponent
  },
  {
    path: ':id/product-endpoint/new',
    component: ProductEndpointFormComponent
  },
  {
    path: ':id/product-endpoint/:endpointId',
    component: ProductEndpointFormComponent
  },
  {
    path: ':id/store-endpoint/new',
    component: StoreEndpointFormComponent
  },
  {
    path: ':id/store-endpoint/:endpointId',
    component: StoreEndpointFormComponent
  }
];
