import { HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { AuthService } from '../Servicios/auth.service';
import { Router } from '@angular/router';

export function jwtInterceptor(
  request: HttpRequest<unknown>,
  next: HttpHandlerFn
) {
  const authService = inject(AuthService);
  const router = inject(Router);
  const isRefreshing = false;
  const refreshTokenSubject = new BehaviorSubject<any>(null);

  const token = authService.getToken();
  
  if (token && isApiRequest(request, authService)) {
    request = addToken(request, token);
  }

  return next(request).pipe(
    catchError(error => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        return handle401Error(request, next, authService, router);
      } else {
        return throwError(() => error);
      }
    })
  );
}

function isApiRequest(request: HttpRequest<any>, authService: any): boolean {
  return request.url.startsWith(authService['urlService']?.baseUrl || '');
}

function addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}

function handle401Error(
  request: HttpRequest<any>,
  next: HttpHandlerFn,
  authService: AuthService,
  router: Router
): Observable<any> {
 
  router.navigate(['/login']);
  return throwError(() => new Error('Session expired'));
}