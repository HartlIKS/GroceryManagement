import { Routes } from '@angular/router';
import { NavigationComponent } from './components/navigation/navigation.component';
import { MainNavItemsComponent } from './components/navigation/main-nav-items/main-nav-items.component';
import { MasterDataNavItemsComponent } from './components/navigation/master-data-nav-items/master-data-nav-items.component';
import {
  DashboardComponent, PriceFormComponent, PriceListComponent,
  ProductFormComponent,
  ProductListComponent, StoreFormComponent,
  StoreListComponent
} from './master-data/components';
import { UserDashboardComponent, ProductGroupListComponent, ProductGroupFormComponent, ShoppingListListComponent, ShoppingListFormComponent, ShoppingTripListComponent, ShoppingTripFormComponent, ShoppingTripChecklistComponent } from './user-interface/components';

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
        component: ProductGroupListComponent
      },
      {
        path: 'product-groups/new',
        component: ProductGroupFormComponent
      },
      {
        path: 'product-groups/:id',
        component: ProductGroupFormComponent
      },
      {
        path: 'shopping-lists',
        component: ShoppingListListComponent
      },
      {
        path: 'shopping-lists/new',
        component: ShoppingListFormComponent
      },
      {
        path: 'shopping-lists/:id',
        component: ShoppingListFormComponent
      },
      {
        path: 'shopping-trips',
        component: ShoppingTripListComponent
      },
      {
        path: 'shopping-trips/new',
        component: ShoppingTripFormComponent
      },
      {
        path: 'shopping-trips/:id',
        component: ShoppingTripFormComponent
      },
      {
        path: 'shopping-trips/:id/checklist',
        component: ShoppingTripChecklistComponent
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
