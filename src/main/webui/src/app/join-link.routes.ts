import { Routes } from '@angular/router';
import { JoinLinkFormComponent } from './user-interface/components/join-link-form/join-link-form.component';

export const joinLinkRoutes: Routes = [
  {
    path: 'create',
    component: JoinLinkFormComponent,
  },
  {
    path: ':id/edit',
    component: JoinLinkFormComponent,
  },
  {
    path: '**',
    redirectTo: '/share-admin',
  }
];
