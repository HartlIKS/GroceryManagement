import { Routes } from '@angular/router';
import { ShoppingTripChecklistComponent, ShoppingTripFormComponent, ShoppingTripListComponent } from './components';

export const shoppingTripsRoutes: Routes = [
  {
    path: '',
    component: ShoppingTripListComponent
  },
  {
    path: 'new',
    component: ShoppingTripFormComponent
  },
  {
    path: ':id',
    component: ShoppingTripFormComponent
  },
  {
    path: ':id/checklist',
    component: ShoppingTripChecklistComponent
  }
];
