import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './Servicios/auth.service';
import { map, take } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  console.log('[AuthGuard] Estado URL:', state.url);
  console.log('[AuthGuard] Route data completa:', route.data);

  const allowedRoles = route.data?.['roles'] as string[] | undefined;
  console.log('[AuthGuard] Roles permitidos extraídos:', allowedRoles);

  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      console.log('[AuthGuard] Usuario actual:', user);

      if (!user || !authService.isAuthenticated()) {
        console.log('[AuthGuard] No autenticado, redirigiendo a /login');
        router.navigate(['/login']);
        return false;
      }

      if (state.url !== '/companyregister' && !authService.hasCompany()) {
        console.log('[AuthGuard] Sin empresa registrada, redirigiendo a /companyregister');
        router.navigate(['/companyregister']);
        return false;
      }

      if (allowedRoles && allowedRoles.length > 0) {
        console.log('[AuthGuard] Verificando roles...');
        console.log('[AuthGuard] Roles requeridos:', allowedRoles);
        console.log('[AuthGuard] Roles del usuario:', user.roles);
        console.log('[AuthGuard] Tipo de user.roles:', typeof user.roles);
        console.log('[AuthGuard] Es array user.roles:', Array.isArray(user.roles));

        if (!user.roles || !Array.isArray(user.roles)) {
          console.log('[AuthGuard] Usuario no tiene roles válidos');
          router.navigate(['/forbidden']);
          return false;
        }

        const hasPermission = user.roles.some(userRole => {
          console.log('[AuthGuard] Comparando rol usuario:', userRole, 'con roles permitidos:', allowedRoles);
          return allowedRoles.includes(userRole);
        });

        console.log('[AuthGuard] Resultado de verificación de permisos:', hasPermission);

        if (!hasPermission) {
          console.log('[AuthGuard] Acceso denegado, redirigiendo a /forbidden');
          router.navigate(['/forbidden']);
          return false;
        }
      } else {
        console.log('[AuthGuard] No hay roles especificados para esta ruta');
      }

      console.log('[AuthGuard] Acceso permitido.');
      return true;
    })
  );
};