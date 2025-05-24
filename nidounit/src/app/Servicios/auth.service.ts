import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { Auth, signInWithEmailAndPassword, createUserWithEmailAndPassword, signInWithPopup, GoogleAuthProvider, signOut, onAuthStateChanged, User, updateProfile } from '@angular/fire/auth';
import { Observable, BehaviorSubject, from, of } from 'rxjs';
import { switchMap, catchError, tap } from 'rxjs/operators';
import { BackserviceService } from './backservice.service';

export interface AuthUser {
  uid: string;
  email: string | null;
  displayName: string | null;
  photoURL: string | null;
  token?: string;
  refreshToken?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private router: Router,
    private auth: Auth,
    private backService: BackserviceService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.initAuthState();
  }

  private initAuthState(): void {
    if (isPlatformBrowser(this.platformId)) {
      onAuthStateChanged(this.auth, async (user) => {
        if (user) {
          const idToken = await user.getIdToken();
          this.validateTokenWithBackend(idToken).subscribe(
            (response) => {
              const authUser: AuthUser = {
                uid: user.uid,
                email: user.email,
                displayName: user.displayName,
                photoURL: user.photoURL,
                token: idToken,
                refreshToken: user.refreshToken
              };
              this.currentUserSubject.next(authUser);
              localStorage.setItem('isLoggedIn', 'true');
              localStorage.setItem('currentUser', JSON.stringify(authUser));
            },
            (error) => {
              console.error('Token validation failed:', error);
              this.logout();
            }
          );
        } else {
          this.clearLocalStorage();
        }
      });

      this.loadStoredUser();
    }
  }

  private loadStoredUser(): void {
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      const user: AuthUser = JSON.parse(storedUser);
      if (user.token) {
        this.validateTokenWithBackend(user.token).subscribe(
          () => this.currentUserSubject.next(user),
          () => this.clearLocalStorage()
        );
      }
    }
  }

  private clearLocalStorage(): void {
    this.currentUserSubject.next(null);
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('currentUser');
  }

  private validateTokenWithBackend(token: string): Observable<any> {
    return this.backService.validateToken(token).pipe(
      catchError(error => {
        if (error.status === 401) {
          throw error;
        }
        return of(error);
      })
    );
  }

  async loginWithEmailPassword(email: string, password: string): Promise<boolean> {
    try {
      const credential = await signInWithEmailAndPassword(this.auth, email, password);
      
      const idToken = await credential.user.getIdToken();
      
      await this.backService.login(email, password).toPromise();
      
      await this.syncUserWithBackend(credential.user);
      
      return true;
    } catch (error: any) {
      console.error('Error en login:', error);
      throw this.handleAuthError(error);
    }
  }

  async registerWithEmailPassword(email: string, password: string, displayName?: string): Promise<boolean> {
    try {
      const credential = await createUserWithEmailAndPassword(this.auth, email, password);
      
      if (displayName && credential.user) {
        await updateProfile(credential.user, { displayName });
      }

      const userData = {
        email,
        password,
        displayName,
        uid: credential.user.uid
      };
      await this.backService.register(userData).toPromise();
      
      await this.syncUserWithBackend(credential.user);
      
      return true;
    } catch (error: any) {
      console.error('Error en registro:', error);
      throw this.handleAuthError(error);
    }
  }

  async loginWithGoogle(): Promise<boolean> {
    try {
      const provider = new GoogleAuthProvider();
      provider.addScope('email');
      provider.addScope('profile');
      
      const credential = await signInWithPopup(this.auth, provider);
      const idToken = await credential.user.getIdToken();
      
      await this.backService.login(credential.user.email || '', idToken).toPromise();
      
      await this.syncUserWithBackend(credential.user);
      
      return true;
    } catch (error: any) {
      console.error('Error en login con Google:', error);
      throw this.handleAuthError(error);
    }
  }

  private async syncUserWithBackend(user: User): Promise<void> {
    try {
      const idToken = await user.getIdToken();
      const userData = {
        uid: user.uid,
        email: user.email,
        displayName: user.displayName,
        photoURL: user.photoURL,
        provider: user.providerData[0]?.providerId || 'email',
        token: idToken
      };

      await this.backService.syncUserWithBackend(userData).toPromise();
    } catch (error) {
      console.error('Error sincronizando usuario con backend:', error);
      throw error;
    }
  }

  async logout(): Promise<void> {
    try {
      await this.backService.logout().toPromise();
      
      await signOut(this.auth);
      
      this.clearLocalStorage();
      
      this.router.navigate(['/login']);
    } catch (error) {
      console.error('Error en logout:', error);
      throw error;
    }
  }

  refreshToken(): Observable<boolean> {
    const currentUser = this.currentUserSubject.value;
    if (!currentUser?.refreshToken) {
      return of(false);
    }

    return this.backService.refreshToken(currentUser.refreshToken).pipe(
      tap((response: any) => {
        if (response.token) {
          const updatedUser = {
            ...currentUser,
            token: response.token,
            refreshToken: response.refreshToken || currentUser.refreshToken
          };
          this.currentUserSubject.next(updatedUser);
          localStorage.setItem('currentUser', JSON.stringify(updatedUser));
        }
      }),
      switchMap(() => of(true)),
      catchError(() => {
        this.logout();
        return of(false);
      })
    );
  }

  isAuthenticated(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
      const currentUser = this.currentUserSubject.value;
      return isLoggedIn && currentUser !== null;
    }
    return false;
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  getCurrentUserObservable(): Observable<AuthUser | null> {
    return this.currentUser$;
  }

  getToken(): string | null {
    return this.currentUserSubject.value?.token || null;
  }

  private handleAuthError(error: any): string {
    if (error.code && error.code.startsWith('auth/')) {
      switch (error.code) {
        case 'auth/user-not-found':
          return 'Usuario no encontrado';
        case 'auth/wrong-password':
          return 'Contraseña incorrecta';
        case 'auth/email-already-in-use':
          return 'El email ya está en uso';
        case 'auth/weak-password':
          return 'La contraseña es muy débil';
        case 'auth/invalid-email':
          return 'Email inválido';
        case 'auth/user-disabled':
          return 'Usuario deshabilitado';
        case 'auth/popup-closed-by-user':
          return 'Ventana cerrada por el usuario';
        case 'auth/cancelled-popup-request':
          return 'Solicitud de popup cancelada';
        case 'auth/operation-not-allowed':
          return 'Operación no permitida';
        case 'auth/network-request-failed':
          return 'Error de red. Por favor verifica tu conexión a internet';
        default:
          return 'Error de autenticación: ' + (error.message || 'Error desconocido');
      }
    }
    
    if (error.error && error.error.message) {
      return error.error.message;
    }
    
    return 'Error de autenticación: ' + (error.message || 'Error desconocido');
  }
}