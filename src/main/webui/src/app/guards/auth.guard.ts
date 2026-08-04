import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  return inject(AuthService).ensureLoggedIn();
};

export const masterDataGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const authService = inject(AuthService);
  if(!await authService.ensureLoggedIn()) return false;
  if(authService.hasMasterData()) return true;
  return router.createUrlTree(['/']);
};
