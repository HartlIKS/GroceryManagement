import { Routes } from '@angular/router';

export const planboardRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/planboard/stages/planboard.component').then(m => m.PlanboardComponent)
  },
  {
    path: 'trip-planning',
    loadComponent: () => import('./components/planboard/stages/trip-planning.component').then(m => m.TripPlanningComponent)
  }
];
