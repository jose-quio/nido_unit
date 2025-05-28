import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../Servicios/auth.service';
import { Router } from '@angular/router';
import { catchError, Observable, shareReplay, switchMap, throwError } from 'rxjs';

export const refreshTokenInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    let isRefreshing = false;
    let refreshSub: Observable<any> | null = null;

    return next(req).pipe(
        catchError(error => {
            if (error.status === 401 && !req.url.includes('/auth/')) {
                if (!isRefreshing) {
                    isRefreshing = true;
                    refreshSub = authService.refreshToken().pipe(
                        shareReplay(1) 
                    );

                    return refreshSub.pipe(
                        switchMap((newToken: string | null) => {
                            if (newToken) {
                                const authReq = req.clone({
                                    setHeaders: { Authorization: `Bearer ${newToken}` },
                                    withCredentials: true 
                                });
                                isRefreshing = false;
                                refreshSub = null;
                                return next(authReq);
                            }
                            return throwError(() => 'Failed to refresh token');
                        }),
                        catchError(refreshError => {
                            isRefreshing = false;
                            refreshSub = null;
                            authService.logout();
                            router.navigate(['/login']);
                            return throwError(() => refreshError);
                        })
                    );
                } else {
                    return refreshSub!.pipe(
                        switchMap((newToken: string | null) => {
                            if (newToken) {
                                const authReq = req.clone({
                                    setHeaders: { Authorization: `Bearer ${newToken}` },
                                    withCredentials: true 
                                });
                                return next(authReq);
                            }
                            return throwError(() => 'Failed to refresh token');
                        })
                    );
                }
            }

            // Otras solicitudes con error: reenviamos el original pero asegurando withCredentials si lo deseas
            return throwError(() => error);
        })
    );
};
