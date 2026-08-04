import { Routes } from '@angular/router';
import { ShareAdminComponent } from './user-interface/components/share-admin';

export const shareAdminRoutes: Routes = [
  {
    path: '',
    component: ShareAdminComponent,
  },
  {
    path: 'join-links',
    loadChildren: () => import('./join-link.routes').then(m => m.joinLinkRoutes)
  }
];
