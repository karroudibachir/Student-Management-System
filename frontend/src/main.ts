import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAppInitializer, inject } from '@angular/core';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { errorInterceptor, loadingInterceptor } from './app/services';
import { AuthService, bearerTokenInterceptor } from './app/auth.service';
bootstrapApplication(AppComponent,{providers:[provideAppInitializer(()=>inject(AuthService).initialize()),provideRouter(routes,withInMemoryScrolling({scrollPositionRestoration:'enabled'})),provideHttpClient(withInterceptors([bearerTokenInterceptor,loadingInterceptor,errorInterceptor]))]}).catch(console.error);
