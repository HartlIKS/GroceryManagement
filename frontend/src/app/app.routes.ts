import { Routes } from '@angular/router';
import { MainNavItemsComponent, MasterDataNavItemsComponent, NavigationComponent } from './components/navigation';
import { UserDashboardComponent } from './user-interface/components';

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
        component: UserDashboardComponent,
      },
      {
        path: 'product-groups',
        loadChildren: () => import('./user-interface/product-groups.routes').then(m => m.productGroupsRoutes)
      },
      {
        path: 'shopping-lists',
        loadChildren: () => import('./user-interface/shopping-lists.routes').then(m => m.shoppingListsRoutes)
      },
      {
        path: 'shopping-trips',
        loadChildren: () => import('./user-interface/shopping-trips.routes').then(m => m.shoppingTripsRoutes)
      },
      {
        path: 'planboard',
        loadComponent: () => import('./user-interface/components').then(m => m.PlanboardComponent)
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
        loadComponent: () => import('./master-data/components').then(m => m.DashboardComponent)
      },
      {
        path: 'products',
        loadChildren: () => import('./master-data/products.routes').then(m => m.productsRoutes)
      },
      {
        path: 'stores',
        loadChildren: () => import('./master-data/stores.routes').then(m => m.storesRoutes)
      },
      {
        path: 'prices',
        loadChildren: () => import('./master-data/prices.routes').then(m => m.pricesRoutes)
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
