import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../Servicios/auth.service';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

export const refreshTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  let isRefreshing = false;

  return next(req).pipe(
    catchError(error => {
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        if (!isRefreshing) {
          isRefreshing = true;
          
          return authService.refreshToken().pipe(
            switchMap((newToken: string | null) => {
              if (newToken) {
                const authReq = req.clone({
                  setHeaders: { Authorization: `Bearer ${newToken}` }
                });
                isRefreshing = false;
                return next(authReq);
              }
              authService.logout();
              router.navigate(['/login']);
              return throwError(() => 'Session expired');
            }),
            catchError(refreshError => {
              isRefreshing = false;
              authService.logout();
              router.navigate(['/login']);
              return throwError(() => refreshError);
            })
          );
        }
      }
      return throwError(() => error);
    })
  );
};