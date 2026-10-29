import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { StudentService, ThemeService } from './services';

describe('StudentService', () => {
  let service: StudentService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(StudentService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('requests filtered, paged students', () => {
    service.list({ q: 'Amira', status: 'ACTIVE', page: 1, size: 5 }).subscribe();
    const request = http.expectOne(
      (req) => req.url === '/api/v1/students' && req.params.get('q') === 'Amira',
    );
    expect(request.request.params.get('status')).toBe('ACTIVE');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('size')).toBe('5');
    request.flush({ content: [], totalElements: 0, totalPages: 0, number: 1, size: 5 });
  });
  it('loads a student by id', () => {
    service.get(7).subscribe((student) => expect(student.id).toBe(7));
    http.expectOne('/api/v1/students/7').flush({ id: 7 });
  });
});

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.removeItem('sms-theme');
    TestBed.configureTestingModule({ providers: [ThemeService] });
  });
  it('persists a theme toggle on the document root', () => {
    const theme = TestBed.inject(ThemeService);
    theme.theme.set('dark');
    theme.toggle();
    expect(theme.theme()).toBe('light');
    expect(localStorage.getItem('sms-theme')).toBe('light');
    expect(document.documentElement.dataset['theme']).toBe('light');
  });
});
