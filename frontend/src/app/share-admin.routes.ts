import { Routes } from '@angular/router';
import { ShareAdminComponent } from './components/share-admin';

export const shareAdminRoutes: Routes = [
  {
    path: '',
    component: ShareAdminComponent,
  }, {
    path: '**',
    redirectTo: '',
  }
];
