import { Routes } from '@angular/router';
import { adminGuard, authGuard, homeGuard, studentGuard } from './auth.service';

export const routes:Routes=[
  {path:'login',loadComponent:()=>import('./auth-pages').then(m=>m.LoginPage)},
  {path:'forbidden',canActivate:[authGuard],loadComponent:()=>import('./auth-pages').then(m=>m.ForbiddenPage)},
  {path:'',pathMatch:'full',canActivate:[authGuard,homeGuard],loadComponent:()=>import('./auth-pages').then(m=>m.ForbiddenPage)},
  {path:'dashboard',canActivate:[authGuard,adminGuard],loadComponent:()=>import('./pages').then(m=>m.DashboardPage)},
  {path:'students',canActivate:[authGuard,adminGuard],loadComponent:()=>import('./pages').then(m=>m.StudentsPage)},
  {path:'students/new',canActivate:[authGuard,adminGuard],loadComponent:()=>import('./pages').then(m=>m.StudentFormPage)},
  {path:'students/:id/edit',canActivate:[authGuard,adminGuard],loadComponent:()=>import('./pages').then(m=>m.StudentFormPage)},
  {path:'students/:id',canActivate:[authGuard,adminGuard],loadComponent:()=>import('./pages').then(m=>m.StudentDetailPage)},
  {path:'admin/accounts',canActivate:[authGuard,adminGuard],loadComponent:()=>import('./auth-pages').then(m=>m.RoleManagementPage)},
  {path:'profile',canActivate:[authGuard,studentGuard],loadComponent:()=>import('./auth-pages').then(m=>m.StudentProfilePage)},
  {path:'**',loadComponent:()=>import('./pages').then(m=>m.NotFoundPage)}
];
