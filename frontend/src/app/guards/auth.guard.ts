import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = async () => {
  const oauthService = inject(OAuthService);
  await inject(AuthService).settle;
  if(oauthService.hasValidAccessToken()) return true;
  oauthService.initLoginFlow();
  return false;
};

export const masterDataGuard: CanActivateFn = async () => {
  const oauthService = inject(OAuthService);
  const router = inject(Router);
  await inject(AuthService).settle;
  if(!oauthService.hasValidAccessToken()) {
    oauthService.initLoginFlow();
    return false;
  }
  const scopes = oauthService.getGrantedScopes() as string[];
  if(scopes.includes('MASTERDATA')) return true;
  return router.createUrlTree(['/']);
};
