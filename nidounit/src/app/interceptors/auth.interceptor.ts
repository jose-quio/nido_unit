import { HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../Servicios/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);

    if (req.url.includes('/api/auth/login') ||
        req.url.includes('/api/auth/refresh') ||
        req.url.includes('/api/auth/register')) {
        return next(req);
    }

    const token = authService.getToken();

    if (token) {
        const authReq = req.clone({
            setHeaders: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return next(authReq);
    }

    return next(req);
};