import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { AuthConfig, OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

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
    this.settle = this.setup();
  }

  private async setup() {
    const config = await firstValueFrom(inject(HttpClient).get<AuthConfig>(environment.authConfigSource))
    config.redirectUri ??= `${window.location.origin}/index.html`;
    config.clientId ??= 'grocery-manager-frontend';
    config.responseType ??= 'code';
    config.scope ??= 'openid profile offline_access';
    this.oauthService.configure(config);
    this.oauthService.setupAutomaticSilentRefresh();
    this.oauthService.events.subscribe(() => {
      if(!this.oauthService.hasValidAccessToken()) this.accessToken_.set(undefined);
      else this.accessToken_.set(this.oauthService.getAccessToken());
    })
    await this.oauthService.loadDiscoveryDocumentAndTryLogin();
  }

  logout(): void {
    this.oauthService.logOut();
  }
}
