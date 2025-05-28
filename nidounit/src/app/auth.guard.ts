import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './Servicios/auth.service';
import { map, take } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  if (isPlatformBrowser(platformId)) {
    if (authService.isAuthenticated()) {
      return true;
    }
  }

  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      const isAuthenticated = authService.isAuthenticated();

      if (isAuthenticated) {
        return true;
      }

      if (isPlatformBrowser(platformId)) {
        router.navigate(['/login'], {
          queryParams: { returnUrl: state.url },
          state: { from: state.url }
        });
      }
      return false;
    })
  );
};