import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { Auth, signInWithEmailAndPassword, createUserWithEmailAndPassword, signInWithPopup, GoogleAuthProvider, signOut, onAuthStateChanged, User, updateProfile } from '@angular/fire/auth';
import { Observable, BehaviorSubject, from, of, map, throwError } from 'rxjs';
import { switchMap, catchError, tap, filter, take } from 'rxjs/operators';
import { BackserviceService } from './backservice.service';

export interface AuthUser {
  uid: string;
  email: string | null;
  displayName: string | null;
  username?: string;
  token?: string;
  refreshToken?: string;
  userId?: number;
  roles?: string[];
  isNewUser?: boolean;
  idCompany?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private refreshTokenInProgress = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  private currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private nameCompanySubject!: BehaviorSubject<string | null>;
  nameCompany$!: Observable<string | null>;

  constructor(
    private router: Router,
    private auth: Auth,
    private backService: BackserviceService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    //this.initAuthState();
    this.loadUserFromStorage();
    const initialName = isPlatformBrowser(this.platformId)
      ? localStorage.getItem('nameCompany')
      : null;

    this.nameCompanySubject = new BehaviorSubject<string | null>(initialName);
    this.nameCompany$ = this.nameCompanySubject.asObservable();
  }

  isAuthenticated(): boolean {
    if (!isPlatformBrowser(this.platformId)) return false;

    const token = localStorage.getItem('token');
    const currentUser = this.currentUserSubject.value;
    const storedUser = localStorage.getItem('currentUser');

    const hasBasicAuth = !!token && !!currentUser && !!storedUser;

    const isCompanyRegisterRoute = window.location.pathname.includes('/companyregister');

    return hasBasicAuth && (isCompanyRegisterRoute || currentUser?.idCompany !== null);
  }

  hasCompany(): boolean {
    if (!isPlatformBrowser(this.platformId)) return false;

    const currentUser = this.currentUserSubject.value;
    const storedUser = localStorage.getItem('currentUser');

    return !!currentUser?.idCompany ||
      (!!storedUser && JSON.parse(storedUser)?.idCompany);
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
                token: idToken,
                refreshToken: user.refreshToken,
                userId: response.userId
              };
              this.currentUserSubject.next(authUser);
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
    localStorage.removeItem('token');
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
    idCompany?: number;
  }> {
    const response = await this.backService.login(email, password).toPromise();

    if (!response || !response.token) {
      throw new Error('Respuesta inválida del servidor');
    }

    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('token', response.token);
    

    const authUser: AuthUser = {
      uid: response.userId.toString(),
      email: email,
      displayName: response.username,
      token: response.token,
      userId: response.userId,
      roles: response.roles,
      idCompany: response.idCompany
    };

    this.currentUserSubject.next(authUser);
    localStorage.setItem('currentUser', JSON.stringify(authUser));
    if (response.idCompany) {
        localStorage.setItem('idCompany', response.idCompany.toString());
      }
    return {
      token: response.token,
      userId: response.userId,
      username: response.username,
      roles: response.roles,
      idCompany: response.idCompany
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

      if (response && response.userId && response.token) {
        const authUser: AuthUser = {
          uid: response.userId.toString(),
          email: email,
          displayName: nombre,
          username: response.username || username,
          token: response.token,
          refreshToken: '',
          userId: response.userId,
          roles: response.roles
        };

        localStorage.setItem('token', response.token);
        localStorage.setItem('currentUser', JSON.stringify(authUser));
        localStorage.setItem('idCompany', response.idCompany);

        this.currentUserSubject.next(authUser);

        return { success: true, userId: response.userId };
      }

      throw new Error('El registro fue exitoso pero no se recibieron todos los datos necesarios');

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

      const result = await signInWithPopup(this.auth, provider);

      const credential = GoogleAuthProvider.credentialFromResult(result);
      if (!credential) {
        throw new Error('No se pudo obtener las credenciales de Google');
      }

      const googleAccessToken = credential.accessToken;
      if (!googleAccessToken) {
        throw new Error('No se pudo obtener el Access Token de Google');
      }

      const loginResponse = await this.backService.loginWithGoogle(googleAccessToken).toPromise();

      const authUser: AuthUser = {
        uid: result.user.uid,
        email: result.user.email,
        displayName: result.user.displayName,
        token: loginResponse.token,
        userId: loginResponse.userId,
        isNewUser: loginResponse.isNewUser,
        idCompany: loginResponse.idCompany
      };

      this.currentUserSubject.next(authUser);

      localStorage.setItem('currentUser', JSON.stringify(authUser));
      localStorage.setItem('token', loginResponse.token);
      localStorage.setItem('isLoggedIn', 'true');
      if (loginResponse.idCompany) {
        localStorage.setItem('idCompany', loginResponse.idCompany);
      }

      if (loginResponse.idCompany === null) {
        this.router.navigate(['/companyregister'], {
          queryParams: { userId: loginResponse.userId }
        });
      } else {
        this.router.navigate(['/apartamento']);
      }

      return { success: true, userId: loginResponse.userId, isNewUser: loginResponse.isNewUser };

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

  refreshToken(): Observable<string | null> {
    if (this.refreshTokenInProgress) {
      return this.refreshTokenSubject.asObservable().pipe(
        filter(token => token !== null),
        take(1)
      );
    } else {
      this.refreshTokenInProgress = true;
      this.refreshTokenSubject.next(null);

      return this.backService.refreshToken().pipe(
        tap((response: { token: string }) => {
          this.refreshTokenInProgress = false;
          localStorage.setItem('token', response.token);
          this.refreshTokenSubject.next(response.token);
        }),
        catchError(error => {
          this.refreshTokenInProgress = false;
          this.refreshTokenSubject.error(error);
          return throwError(() => error);
        }),
        map(response => response.token)
      );
    }
  }

  private refreshInProgress = false;

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

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  getCurrentUserObservable(): Observable<AuthUser | null> {
    return this.currentUser$;
  }

  getToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('token');
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
    this.currentUserSubject.next(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('currentUser');
    localStorage.removeItem('isLoggedIn');
  }

  getAuthData(): { token: string | null, user: any | null } {
    return {
      token: localStorage.getItem('token'),
      user: JSON.parse(localStorage.getItem('user') || 'null')
    };
  }

  updateCompanyInfo(companyId: number): void {
    const currentUser = this.currentUserSubject.value;
    if (currentUser) {
      const updatedUser = { ...currentUser, idCompany: companyId };
      this.currentUserSubject.next(updatedUser);
      localStorage.setItem('currentUser', JSON.stringify(updatedUser));
    }
  }

  async logout(): Promise<void> {
    try {
      this.currentUserSubject.next(null);

      localStorage.removeItem('token');
      localStorage.removeItem('currentUser');
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('user');
      localStorage.removeItem('idCompany');
      localStorage.removeItem('nameCompany');
      this.clearNameCompany();
      if (this.auth.currentUser) {
        await signOut(this.auth);
      }

      this.backService.logout().subscribe({
        next: () => console.log('Backend logout successful'),
        error: (error) => console.warn('Backend logout failed, but local logout completed:', error)
      });

      this.router.navigate(['/login']);

    } catch (error) {
      console.error('Error durante logout:', error);
      this.currentUserSubject.next(null);
      localStorage.clear();
      this.router.navigate(['/login']);
    }
  }

  private nameCompanySubject$() {
    return this.nameCompanySubject.asObservable();
  }

  setNameCompany(value: string) {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('nameCompany', value);
    }
    this.nameCompanySubject.next(value);
  }
  clearNameCompany(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('nameCompany');
    }
    this.nameCompanySubject.next(null);
  }

}