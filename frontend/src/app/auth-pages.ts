import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService, AppRole, CurrentUser } from './auth.service';
import { ToastService } from './services';
import { environment } from '../environments/environment';

@Component({standalone:true,imports:[RouterLink],template:`
<main class="auth-screen"><section class="auth-card"><a class="brand auth-brand" routerLink="/dashboard"><span class="brand-mark">s.</span><span>student<span class="brand-light">space</span></span></a><div class="auth-illustration"><span>✳</span><i></i></div><div class="eyebrow"><span class="eyebrow-dot"></span>YOUR CAMPUS WORKSPACE</div><h1>Good things <em>start here.</em></h1><p>Sign in with your organization account to continue to StudentSpace.</p>@if(auth.error()){<div class="auth-error" role="alert">{{auth.error()}}</div>}<button class="button button-primary auth-submit" (click)="signIn()">Continue with secure sign-in <span>→</span></button><small>Protected with your organization’s single sign-on.</small></section><div class="auth-footer">STUDENTSPACE · A BETTER WAY TO KEEP TRACK</div></main>`})
export class LoginPage {
  readonly auth=inject(AuthService);private readonly route=inject(ActivatedRoute);
  ngOnInit(){if(this.auth.authenticated()){const returnUrl=this.route.snapshot.queryParamMap.get('returnUrl');if(returnUrl)sessionStorage.setItem('sms-return-url',returnUrl);this.auth.completeLoginNavigation()}}
  signIn(){void this.auth.login(this.route.snapshot.queryParamMap.get('returnUrl')??'/dashboard')}
}

@Component({standalone:true,imports:[RouterLink],template:`<div class="not-found"><div class="not-found-mark">403</div><h1>This space is <em>for the team.</em></h1><p>Your account doesn’t have permission to view this page.</p><a class="button button-primary" routerLink="/dashboard">← &nbsp;Back to the overview</a></div>`})
export class ForbiddenPage {}

interface ManagedUser extends CurrentUser { selected:AppRole[]; saving?:boolean }
@Component({standalone:true,template:`
<div class="page-heading"><div><div class="eyebrow"><span class="eyebrow-dot"></span>ACCESS CONTROL</div><h1>People &amp; <em>permissions.</em></h1><p class="subtitle">Choose what each signed-in teammate can do.</p></div></div>
<article class="panel table-panel role-panel"><div class="panel-heading role-heading"><div><h2>Workspace members</h2><p>Roles apply to StudentSpace, not to your identity provider.</p></div><button class="button button-quiet" (click)="load()">↻ &nbsp; Refresh</button></div>@if(loading()){<div class="skeleton-list"><div></div><div></div><div></div></div>}@else if(!users().length){<div class="empty-state"><div class="empty-icon">♧</div><h3>No signed-in members yet</h3><p>Members appear here after their first successful sign-in.</p></div>}@else{<div class="role-list">@for(user of users();track user.subject){<section class="role-row"><div class="avatar" [class]="'avatar tone-'+(user.displayName?.charCodeAt(0) ?? 0)%5">{{initials(user)}}</div><div class="role-identity"><strong>{{user.displayName||user.email||'Workspace member'}}</strong><span>{{user.email||user.subject}}</span></div><span class="role-id">{{user.subject}}</span><div class="role-options">@for(role of roles;track role){<label><input type="checkbox" [checked]="user.selected.includes(role)" (change)="toggle(user.subject,role,$any($event.target).checked)"><span>{{roleLabel(role)}}</span></label>}</div><button class="button button-primary role-save" [disabled]="user.saving" (click)="save(user)">{{user.saving?'Saving…':'Save roles'}}</button></section>}</div>}<div class="role-legend"><span><b>Admin</b> — manage members and all student records</span><span><b>Staff</b> — add and update student records</span><span><b>Viewer</b> — view dashboards and student profiles</span></div></article>`})
export class RoleManagementPage implements OnInit {
  readonly users=signal<ManagedUser[]>([]);readonly loading=signal(true);readonly roles:AppRole[]=['ADMIN','STAFF','VIEWER'];
  private readonly http=inject(HttpClient);private readonly toast=inject(ToastService);private readonly auth=inject(AuthService);
  ngOnInit(){this.load()}
  load(){this.loading.set(true);this.http.get<CurrentUser[]>(`${environment.apiUrl}/admin/users`).subscribe({next:users=>{this.users.set(users.map(u=>({...u,selected:[...u.roles]})));this.loading.set(false)},error:()=>{this.loading.set(false);this.toast.show('Could not load workspace members.','error')}})}
  toggle(subject:string,role:AppRole,checked:boolean){this.users.update(users=>users.map(user=>user.subject!==subject?user:{...user,selected:checked?[...new Set([...user.selected,role])]:user.selected.filter(r=>r!==role)}))}
  save(user:ManagedUser){this.users.update(users=>users.map(u=>u.subject===user.subject?{...u,saving:true}:u));this.http.put<CurrentUser>(`${environment.apiUrl}/admin/users/${encodeURIComponent(user.subject)}/roles`,{roles:user.selected}).subscribe({next:updated=>{this.users.update(users=>users.map(u=>u.subject===user.subject?{...updated,selected:[...updated.roles],saving:false}:u));this.toast.show('Workspace roles updated.');if(this.auth.user()?.subject===updated.subject)void this.auth.loadCurrentUser()},error:e=>{this.users.update(users=>users.map(u=>u.subject===user.subject?{...u,saving:false}:u));this.toast.show(e.status===400?(e.error?.message??'At least one administrator must remain.'):'Could not update workspace roles.','error')}})}
  initials(user:CurrentUser){const label=user.displayName||user.email||'U';return label.split(/[\s@._-]+/).filter(Boolean).slice(0,2).map(x=>x[0]).join('').toUpperCase()}
  roleLabel(role:AppRole){return role[0]+role.slice(1).toLowerCase()}
}
