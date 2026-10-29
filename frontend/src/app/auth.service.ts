import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment';

export type AppRole = 'ADMIN' | 'STUDENT';
export interface CurrentUser { subject:string; email:string|null; displayName:string|null; roles:AppRole[] }
interface LoginResponse { accessToken:string; user:CurrentUser }

@Injectable({providedIn:'root'})
export class AuthService {
  private readonly http=inject(HttpClient);
  private readonly router=inject(Router);
  private readonly tokenKey='sms-access-token';
  readonly user=signal<CurrentUser|null>(null);
  readonly ready=signal(false);
  readonly error=signal('');
  readonly authenticated=computed(()=>!!this.user());
  readonly isAdmin=computed(()=>this.user()?.roles.includes('ADMIN')??false);
  readonly isStudent=computed(()=>this.user()?.roles.includes('STUDENT')??false);

  async initialize():Promise<void>{
    const token=sessionStorage.getItem(this.tokenKey);
    if(token){
      try{this.user.set(await firstValueFrom(this.http.get<CurrentUser>(`${environment.apiUrl}/auth/me`)))}
      catch{this.clearSession()}
    }
    this.ready.set(true);
  }

  async login(email:string,password:string,returnUrl?:string):Promise<void>{
    this.error.set('');
    const response=await firstValueFrom(this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`,{email,password}));
    sessionStorage.setItem(this.tokenKey,response.accessToken);
    this.user.set(response.user);
    const defaultUrl=response.user.roles.includes('STUDENT')?'/profile':'/dashboard';
    const safeUrl=returnUrl?.startsWith('/')&&!returnUrl.startsWith('//')?returnUrl:defaultUrl;
    await this.router.navigateByUrl(safeUrl);
  }

  logout():void{this.clearSession();void this.router.navigateByUrl('/login')}
  accessToken():string|null{return sessionStorage.getItem(this.tokenKey)}
  async loadCurrentUser():Promise<CurrentUser>{
    const user=await firstValueFrom(this.http.get<CurrentUser>(`${environment.apiUrl}/auth/me`));
    this.user.set(user);return user;
  }
  private clearSession():void{sessionStorage.removeItem(this.tokenKey);this.user.set(null)}
}

export const bearerTokenInterceptor:HttpInterceptorFn=(request,next)=>{
  const token=inject(AuthService).accessToken();
  if(!token||!request.url.startsWith(environment.apiUrl))return next(request);
  return next(request.clone({setHeaders:{Authorization:`Bearer ${token}`}}));
};

export const authGuard:CanActivateFn=(_route,state)=>{
  const auth=inject(AuthService);const router=inject(Router);
  return auth.authenticated()?true:router.createUrlTree(['/login'],{queryParams:{returnUrl:state.url}});
};
export const studentGuard:CanActivateFn=()=>{
  const auth=inject(AuthService);const router=inject(Router);
  return auth.isStudent()?true:router.createUrlTree(['/forbidden']);
};
export const adminGuard:CanActivateFn=()=>{
  const auth=inject(AuthService);const router=inject(Router);
  return auth.isAdmin()?true:router.createUrlTree(['/forbidden']);
};
export const homeGuard:CanActivateFn=()=>{
  const auth=inject(AuthService);const router=inject(Router);
  return router.createUrlTree([auth.isStudent()?'/profile':'/dashboard']);
};
