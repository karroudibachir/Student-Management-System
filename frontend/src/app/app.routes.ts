import { Routes } from '@angular/router';
import { adminGuard, authGuard, staffGuard } from './auth.service';

export const routes:Routes=[
  {path:'login',loadComponent:()=>import('./auth-pages').then(m=>m.LoginPage)},
  {path:'forbidden',canActivate:[authGuard],loadComponent:()=>import('./auth-pages').then(m=>m.ForbiddenPage)},
  {path:'',pathMatch:'full',redirectTo:'dashboard'},
  {path:'dashboard',canActivate:[authGuard],loadComponent:()=>import('./pages').then(m=>m.DashboardPage)},
  {path:'students',canActivate:[authGuard],loadComponent:()=>import('./pages').then(m=>m.StudentsPage)},
  {path:'students/new',canActivate:[authGuard,staffGuard],loadComponent:()=>import('./pages').then(m=>m.StudentFormPage)},
  {path:'students/:id/edit',canActivate:[authGuard,staffGuard],loadComponent:()=>import('./pages').then(m=>m.StudentFormPage)},
  {path:'students/:id',canActivate:[authGuard],loadComponent:()=>import('./pages').then(m=>m.StudentDetailPage)},
  {path:'admin/users',canActivate:[authGuard,adminGuard],loadComponent:()=>import('./auth-pages').then(m=>m.RoleManagementPage)},
  {path:'**',loadComponent:()=>import('./pages').then(m=>m.NotFoundPage)}
];
