import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import Keycloak from 'keycloak-js';
import { firstValueFrom, from, switchMap } from 'rxjs';
import { environment } from '../environments/environment';

export type AppRole = 'ADMIN' | 'STAFF' | 'VIEWER';
export interface CurrentUser { subject:string; email:string|null; displayName:string|null; roles:AppRole[] }
declare global { interface Window { smsRuntimeConfig?: { authUrl:string; realm:string; clientId:string } } }

@Injectable({providedIn:'root'})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private keycloak: Keycloak | null = null;
  readonly user = signal<CurrentUser|null>(null);
  readonly ready = signal(false);
  readonly error = signal('');
  readonly authenticated = computed(() => !!this.user());
  readonly isAdmin = computed(() => this.user()?.roles.includes('ADMIN') ?? false);
  readonly canManageStudents = computed(() => this.user()?.roles.some(r => r === 'ADMIN' || r === 'STAFF') ?? false);

  async initialize(): Promise<void> {
    const config = window.smsRuntimeConfig;
    const kc = new Keycloak({
      url: config?.authUrl ?? environment.oidcUrl,
      realm: config?.realm ?? environment.oidcRealm,
      clientId: config?.clientId ?? environment.oidcClientId,
    });
    this.keycloak = kc;
    kc.onAuthLogout = () => this.user.set(null);
    try {
      const signedIn = await kc.init({ onLoad:'check-sso', pkceMethod:'S256', checkLoginIframe:false });
      if (signedIn) {
        await this.loadCurrentUser();
      }
    } catch {
      this.error.set('Sign-in service is unavailable. Check the identity provider configuration and try again.');
    } finally {
      this.ready.set(true);
    }
  }

  async login(returnUrl = '/dashboard'): Promise<void> {
    if (!this.keycloak) return;
    const safeReturnUrl = returnUrl.startsWith('/') && !returnUrl.startsWith('//') ? returnUrl : '/dashboard';
    sessionStorage.setItem('sms-return-url', safeReturnUrl);
    await this.keycloak.login({ redirectUri: `${window.location.origin}/login` });
  }

  async logout(): Promise<void> {
    this.user.set(null);
    await this.keycloak?.logout({ redirectUri: `${window.location.origin}/login` });
  }

  async accessToken(): Promise<string|null> {
    if (!this.keycloak?.authenticated) return null;
    await this.keycloak.updateToken(30);
    return this.keycloak.token ?? null;
  }

  async loadCurrentUser(): Promise<CurrentUser> {
    const user = await firstValueFrom(this.http.get<CurrentUser>(`${environment.apiUrl}/auth/me`));
    this.user.set(user);
    return user;
  }

  completeLoginNavigation(): void {
    const returnUrl = sessionStorage.getItem('sms-return-url') || '/dashboard';
    sessionStorage.removeItem('sms-return-url');
    void this.router.navigateByUrl(returnUrl.startsWith('/') ? returnUrl : '/dashboard');
  }
}

export const bearerTokenInterceptor:HttpInterceptorFn = (request,next) => {
  const auth = inject(AuthService);
  if (!request.url.startsWith(environment.apiUrl)) return next(request);
  return from(auth.accessToken()).pipe(switchMap(token =>
    next(token ? request.clone({setHeaders:{Authorization:`Bearer ${token}`}}) : request)));
};

export const authGuard:CanActivateFn = (_route,state) => {
  const auth=inject(AuthService);const router=inject(Router);
  return auth.authenticated() ? true : router.createUrlTree(['/login'],{queryParams:{returnUrl:state.url}});
};
export const staffGuard:CanActivateFn = () => {
  const auth=inject(AuthService);const router=inject(Router);
  return auth.canManageStudents() ? true : router.createUrlTree(['/forbidden']);
};
export const adminGuard:CanActivateFn = () => {
  const auth=inject(AuthService);const router=inject(Router);
  return auth.isAdmin() ? true : router.createUrlTree(['/forbidden']);
};
