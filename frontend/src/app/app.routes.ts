import { Routes } from '@angular/router';
import {
  UserInterfaceComponent
} from './components';
import { NavigationComponent } from './components/navigation/navigation.component';
import { MainNavItemsComponent } from './components/navigation/main-nav-items/main-nav-items.component';
import { MasterDataNavItemsComponent } from './components/navigation/master-data-nav-items/master-data-nav-items.component';
import {
  DashboardComponent, PriceFormComponent, PriceListComponent,
  ProductFormComponent,
  ProductListComponent, StoreFormComponent,
  StoreListComponent
} from './master-data/components';

export const routes: Routes = [
  {
    path: '',
    component: NavigationComponent,
    children: [
      {
        path: '',
        outlet: 'navbar',
        component: MainNavItemsComponent,
      },
      {
        path: '',
        component: UserInterfaceComponent,
      }
    ],
  },
  {
    path: 'master-data',
    component: NavigationComponent,
    children: [
      {
        path: '',
        outlet: 'navbar',
        component: MasterDataNavItemsComponent,
      },
      {
        path: '',
        redirectTo: '/master-data/dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        component: DashboardComponent
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
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
