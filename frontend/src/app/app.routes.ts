import { Routes } from '@angular/router';
import { authGuard, masterDataGuard } from './guards/auth.guard';
import { NavigationComponent } from './components/navigation';
import { MainNavItemsComponent } from './components/navigation/main-nav-items';
import { MasterDataNavItemsComponent } from './components/navigation/master-data-nav-items';

export const routes: Routes = [
  {
    path: '',
    component: NavigationComponent,
    canActivateChild: [authGuard],
    children: [
      {
        path: '',
        outlet: 'navbar',
        component: MainNavItemsComponent,
      },
      {
        path: '',
        loadComponent: () => import('./user-interface/components/user-dashboard.component').then(m => m.UserDashboardComponent),
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
        loadChildren: () => import('./user-interface/planboard.routes').then(m => m.planboardRoutes),
      },
      {
        path: 'share-admin',
        loadChildren: () => import('./share-admin.routes').then(m => m.shareAdminRoutes)
      }
    ],
  },
  {
    path: 'master-data',
    component: NavigationComponent,
    canActivate: [masterDataGuard],
    children: [
      {
        path: '',
        outlet: 'navbar',
        component: MasterDataNavItemsComponent
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
