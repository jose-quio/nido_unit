import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './Servicios/auth.service';
import { map, take } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (state.url.includes('/companyregister')) {
    if (authService.isAuthenticated() && !authService.hasCompany()) {
      return true;
    }
    router.navigate(['/login']);
    return false;
  }

  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      if (authService.isAuthenticated() && authService.hasCompany()) {
        return true;
      }

      if (authService.isAuthenticated() && !authService.hasCompany()) {
        router.navigate(['/companyregister']);
        return false;
      }

      router.navigate(['/login']);
      return false;
    })
  );
};