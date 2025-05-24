import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuth = false;

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.initAuthState();
  }

  private initAuthState(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.isAuth = localStorage.getItem('isLoggedIn') === 'true';
    }
  }

  login(username: string, password: string): boolean {
    if (username === 'admin' && password === 'admin') {
      this.isAuth = true;
      if (isPlatformBrowser(this.platformId)) {
        localStorage.setItem('isLoggedIn', 'true');
      }
      return true;
    }
    return false;
  }

  logout(): void {
    this.isAuth = false;
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('isLoggedIn');
    }
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    if (this.isAuth) return true;
    
    if (isPlatformBrowser(this.platformId)) {
      const stored = localStorage.getItem('isLoggedIn') === 'true';
      this.isAuth = stored; // Sincronizar el estado
      return stored;
    }
    
    return false;
  }
}