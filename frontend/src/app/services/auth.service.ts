import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly oauthService = inject(OAuthService);
  private readonly accessToken_ = signal<string | undefined>(undefined);
  readonly accessToken = this.accessToken_.asReadonly();
  readonly settle: Promise<void>;
  readonly scopes = computed(() => {
    this.accessToken();
    return this.oauthService.getGrantedScopes() as string[] ?? [];
  })
  readonly hasMasterData = computed(() => this.scopes().includes('MASTERDATA'))
  readonly claims = computed((): Record<string, any> | undefined => {
    this.accessToken();
    return this.oauthService.getIdentityClaims();
  })
  readonly username = computed((): string | undefined => this.claims()?.['preferred_username'])
  readonly eff = effect(() => console.log(this.scopes(), this.claims()));

  constructor() {
    this.oauthService.configure({
      issuer: environment.issuer,
      redirectUri: `${window.location.origin}/index.html`,
      clientId: 'grocery-manager-frontend',
      responseType: 'code',
      scope: 'openid profile offline_access MASTERDATA',
      showDebugInformation: true,
    });
    this.oauthService.setupAutomaticSilentRefresh();
    this.oauthService.events.subscribe(() => {
      if(!this.oauthService.hasValidAccessToken()) this.accessToken_.set(undefined);
      else this.accessToken_.set(this.oauthService.getAccessToken());
    })
    this.settle = this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {});
  }

  logout(): void {
    this.oauthService.logOut();
  }
}
