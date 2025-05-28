import { HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../Servicios/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const refreshTokenInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    let isRefreshing = false;

    const handle401Error = (request: HttpRequest<unknown>) => {
        if (!isRefreshing) {
            isRefreshing = true;

            return authService.refreshToken().pipe(
                switchMap((token: string | null) => {
                    isRefreshing = false;
                    if (token) {
                        return next(addTokenHeader(request, token));
                    }
                    authService.logout();
                    router.navigate(['/login']);
                    return throwError(() => new Error('Session expired'));
                }),
                catchError((err) => {
                    isRefreshing = false;
                    authService.logout();
                    router.navigate(['/login']);
                    return throwError(() => err);
                })
            );
        }

        return next(request);
    };

    const addTokenHeader = (request: HttpRequest<unknown>, token: string) => {
        return request.clone({
            setHeaders: {
                'Authorization': `Bearer ${token}`
            }
        });
    };

    return next(req).pipe(
        catchError((error) => {
            if (error.status === 401) {
                return handle401Error(req);
            }
            return throwError(() => error);
        })
    );
};