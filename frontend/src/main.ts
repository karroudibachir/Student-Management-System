import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { errorInterceptor, loadingInterceptor } from './app/services';
bootstrapApplication(AppComponent,{providers:[provideRouter(routes,withInMemoryScrolling({scrollPositionRestoration:'enabled'})),provideHttpClient(withInterceptors([loadingInterceptor,errorInterceptor]))]}).catch(console.error);
