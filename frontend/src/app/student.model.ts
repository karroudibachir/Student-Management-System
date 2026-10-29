export type StudentStatus = 'ACTIVE' | 'GRADUATED' | 'SUSPENDED';
export type Gender = 'MALE' | 'FEMALE' | 'OTHER';
export interface Student { id:number; studentCode:string; firstName:string; lastName:string; email:string; phone:string|null; dateOfBirth:string; gender:Gender|null; address:string|null; major:string; enrollmentYear:number; gpa:number|null; status:StudentStatus; photoUrl:string|null; createdAt:string; updatedAt:string }
export type StudentInput = Omit<Student,'id'|'studentCode'|'createdAt'|'updatedAt'>;
export interface Page<T> { content:T[]; totalElements:number; totalPages:number; number:number; size:number }
export interface StudentStats { total:number; active:number; graduated:number; suspended:number; averageGpa:number }
