import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpInterceptorFn, HttpParams } from '@angular/common/http';
import { catchError, finalize, Observable, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { Page, Student, StudentInput, StudentStats } from './student.model';

@Injectable({providedIn:'root'}) export class StudentService {
 private readonly base=environment.apiUrl+'/students';
 constructor(private readonly http:HttpClient){}
 list(filters:{q?:string;major?:string;status?:string;year?:string;page?:number;size?:number;sort?:string}={}):Observable<Page<Student>>{let p=new HttpParams();for(const [k,v] of Object.entries(filters))if(v!==undefined&&v!=='')p=p.set(k,v);return this.http.get<Page<Student>>(this.base,{params:p});}
 get(id:number):Observable<Student>{return this.http.get<Student>(`${this.base}/${id}`)}
 create(student:StudentInput):Observable<Student>{return this.http.post<Student>(this.base,student)}
 update(id:number,student:StudentInput):Observable<Student>{return this.http.put<Student>(`${this.base}/${id}`,student)}
 delete(id:number):Observable<void>{return this.http.delete<void>(`${this.base}/${id}`)}
 stats():Observable<StudentStats>{return this.http.get<StudentStats>(this.base+'/stats')}
 majors():Observable<string[]>{return this.http.get<string[]>(this.base+'/majors')}
}
@Injectable({providedIn:'root'}) export class ThemeService {
 readonly theme=signal<'dark'|'light'>(this.initial()); readonly isDark=computed(()=>this.theme()==='dark');
 private initial():'dark'|'light'{const saved=typeof localStorage!=='undefined'?localStorage.getItem('sms-theme'):null;return saved==='light'||saved==='dark'?saved:(typeof matchMedia!=='undefined'&&matchMedia('(prefers-color-scheme: light)').matches?'light':'dark')}
 constructor(){this.apply()} toggle(){this.theme.update(v=>v==='dark'?'light':'dark');this.apply()}
 private apply(){if(typeof document!=='undefined'){document.documentElement.dataset['theme']=this.theme();localStorage.setItem('sms-theme',this.theme())}}
}
@Injectable({providedIn:'root'}) export class ToastService {readonly message=signal('');readonly kind=signal<'success'|'error'>('success');show(message:string,kind:'success'|'error'='success'){this.message.set(message);this.kind.set(kind);setTimeout(()=>this.message.set(''),3200)}}
@Injectable({providedIn:'root'}) export class LoadingService {private readonly pending=signal(0);readonly active=computed(()=>this.pending()>0);begin(){this.pending.update(n=>n+1)}end(){this.pending.update(n=>Math.max(0,n-1))}}
export const loadingInterceptor:HttpInterceptorFn=(request,next)=>{const loading=inject(LoadingService);loading.begin();return next(request).pipe(finalize(()=>loading.end()))};
export const errorInterceptor:HttpInterceptorFn=(request,next)=>{const toast=inject(ToastService);return next(request).pipe(catchError((error:HttpErrorResponse)=>{if(error.status===0)toast.show('Connection lost. Check your network and try again.','error');else if(error.status>=500)toast.show('Something went wrong on our side. Please try again.','error');return throwError(()=>error)}))};
