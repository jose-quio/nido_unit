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
  username?: string;
  photoURL: string | null;
  token?: string;
  refreshToken?: string;
  userId?: number;
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
    this.loadUserFromStorage();
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
                refreshToken: user.refreshToken,
                userId: response.userId
              };
              this.currentUserSubject.next(authUser);
              localStorage.setItem('isLoggedIn', 'true');
              localStorage.setItem('currentUser', JSON.stringify(authUser));
            },
            (error) => {
              console.error('Token validation failed:', error);
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

  async loginWithEmailPassword(email: string, password: string): Promise<{
    token: string;
    userId: number;
    username: string;
    roles: string[];
  }> {
    const response = await this.backService.login(email, password).toPromise();

    if (!response || !response.token) {
      throw new Error('Respuesta inválida del servidor');
    }

    return {
      token: response.token,
      userId: response.userId,
      username: response.username,
      roles: response.roles || []
    };
  }

  async registerWithEmailPassword(
    username: string,
    password: string,
    nombre: string,
    email: string
  ): Promise<{ success: boolean, userId: number }> {
    try {
      const registerData = {
        username,
        password,
        nombre,
        email
      };

      const response = await this.backService.register(registerData).toPromise();

      if (response && response.id) {
        const authUser: AuthUser = {
          uid: response.id.toString(),
          email: email,
          displayName: nombre,
          username: username,
          photoURL: null,
          token: 'token-placeholder',
          userId: response.id
        };

        this.currentUserSubject.next(authUser);
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('currentUser', JSON.stringify(authUser));

        return { success: true, userId: response.id };
      }

      throw new Error('El registro fue exitoso pero no se recibió ID de usuario');

    } catch (error: any) {
      console.error('Error en registro:', error);
      throw this.handleAuthError(error);
    }
  }

  async loginWithGoogle(): Promise<{ success: boolean, userId?: number, isNewUser?: boolean }> {
    try {
      const provider = new GoogleAuthProvider();
      provider.addScope('email');
      provider.addScope('profile');

      const credential = await signInWithPopup(this.auth, provider);
      const idToken = await credential.user.getIdToken();

      const loginResponse = await this.backService.login(credential.user.email || '', idToken).toPromise();

      await this.syncUserWithBackend(credential.user);

      return {
        success: true,

      };
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
        return of(false);
      })
    );
  }

  private loadUserFromStorage(): void {
    if (isPlatformBrowser(this.platformId)) {
      const userData = localStorage.getItem('currentUser');
      if (userData) {
        try {
          const user = JSON.parse(userData);
          this.currentUserSubject.next(user);
        } catch (e) {
          this.clearAuthData();
        }
      }
    }
  }

  isAuthenticated(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      return !!localStorage.getItem('token') && !!this.currentUserSubject.value;
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
    if (isPlatformBrowser(this.platformId)) {
      const userData = localStorage.getItem('currentUser');
      if (userData) {
        try {
          const user = JSON.parse(userData);
          return user.token || null;
        } catch {
          return null;
        }
      }
    }
    return null;
  }

  getUserId(): number | null {
    return this.currentUserSubject.value?.userId || null;
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

  setAuthData(token: string, userData: any): void {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
  }

  clearAuthData(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  getAuthData(): { token: string | null, user: any | null } {
    return {
      token: localStorage.getItem('token'),
      user: JSON.parse(localStorage.getItem('user') || 'null')
    };
  }


  updateCompanyInfo(companyId: number): void {
    if (typeof window !== 'undefined') {
      const userData = localStorage.getItem('currentUser');
      if (userData) {
        try {
          const user = JSON.parse(userData);
          user.companyId = companyId;
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        } catch (e) {
          console.error('Error al actualizar información de compañía', e);
        }
      }
    }
  }
}