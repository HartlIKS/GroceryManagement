import { Routes } from '@angular/router';
import { authGuard, masterDataGuard } from './guards/auth.guard';
import { NavigationComponent, NavItem } from './components/navigation';

const normalNavItems: NavItem[] = [
  {
    path: '/',
    linkOptions: {
      exact: true,
    },
    icon: 'home',
    title: 'Home',
  },
  {
    path: '/product-groups',
    icon: 'category',
    title: 'Product Groups',
  },
  {
    path: '/shopping-lists',
    icon: 'shopping_cart',
    title: 'Shopping Lists',
  },
  {
    path: '/shopping-trips',
    icon: 'receipt_long',
    title: 'Shopping Trips',
  },
  {
    path: '/planboard',
    icon: 'dashboard',
    title: 'Plan Board',
  },
];
const masterDataNavItems: NavItem[] = [
  {
    path: '/master-data/dashboard',
    icon: 'dashboard',
    title: 'Dashboard',
  },
  {
    path: '/master-data/products',
    icon: 'inventory_2',
    title: 'Products',
  },
  {
    path: '/master-data/stores',
    icon: 'store',
    title: 'Stores',
  },
  {
    path: '/master-data/prices',
    icon: 'price_check',
    title: 'Prices',
  },
];

export const routes: Routes = [
  {
    path: '',
    component: NavigationComponent,
    data: {
      navItems: normalNavItems,
      isMasterData: false,
    },
    canActivateChild: [authGuard],
    children: [
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
      },
      {
        path: 'join/:uuid',
        loadComponent: () => import('./user-interface/components/join-share/join-share.component').then(m => m.JoinShareComponent)
      }
    ],
  },
  {
    path: 'master-data',
    component: NavigationComponent,
    data: {
      navItems: masterDataNavItems,
      isMasterData: true,
    },
    canActivate: [masterDataGuard],
    children: [
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
