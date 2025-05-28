import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';
import { UrlserviceService } from './urlservice.service';
import { AuthUser } from './auth.service';
import { environment } from '../../enviroments/environment.staging';

@Injectable({
  providedIn: 'root'
})
export class BackserviceService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient, private urlService: UrlserviceService) { }

  // Operaciones de Apartamentos
  registrarApartamento(apartamento: any): Observable<any> {
    if (!apartamento.company?.id) {
      return throwError(() => new Error('ID de compañía no proporcionado'));
    }
      
    return this.http.post<any>(this.urlService.apiUrlRegistrarApartamento, apartamento);
  }

  getApartamentos(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetApartamentos);
  }

  eliminarApartamento(codigoApartamento: string): Observable<any> {
    return this.http.delete<any>(`${this.urlService.apiUrlDeleteApartamento}/${codigoApartamento}`);
  }

  guardarEdicion(codigoApartamento: string, apartamento: any): Observable<any> {
    return this.http.put<any>(`${this.urlService.apiUrlActualizarApartamento}/${codigoApartamento}`, apartamento);
  }

  // Operaciones de Apartamento-Habitación
  registrarApartamentoHab(apartamentoHab: any): Observable<any> {
    return this.http.post<any>(this.urlService.apiUrlRegistrarApartamentoHab, apartamentoHab);
  }

  getApartamentosHab(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetApartamentosHab);
  }

  actualizarApartamentoHab(codigoHabitacion: string, apartamentoHab: any): Observable<any> {
    return this.http.put<any>(`${this.urlService.apiUrlUpdateApartamentoHab}/${codigoHabitacion}`, apartamentoHab);
  }

  eliminarApartamentoHab(codigoHabitacion: string): Observable<any> {
    return this.http.delete<any>(`${this.urlService.apiUrlDeleteApartamentoHab}/${codigoHabitacion}`);
  }

  // Operaciones de Caja
  getTotalCapital(): Observable<any> {
    return this.http.get<any>(this.urlService.apiUrlGetTotalCapital);
  }

  guardarPago(pago: any): Observable<any> {
    return this.http.post(this.urlService.apiUrlGuardarPago, pago);
  }

  // Operaciones de Pago
  getPagosDNI(dni: string): Observable<any> {
    return this.http.get<any>(`${this.urlService.apiUrlGetPagosDNI}/${dni}`);
  }

  getTodosLosPagos(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetTodosPagos);
  }

  // Operaciones de propietario
  getDepartamentosDisponibles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.urlService.apiUrlGetDepartamentosDisponibles}`);
  }

  registrarPropietario(propietario: any): Observable<any> {
    return this.http.post<any>(this.urlService.apiUrlRegistrarPropietario, propietario);
  }

  getPropietarios(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetPropietarios);
  }

  actualizarPropietario(codigoPropietario: string, propietario: any): Observable<any> {
    return this.http.put<any>(`${this.urlService.apiUrlUpdatePropietario}/${codigoPropietario}`, propietario);
  }

  eliminarPropietario(codigoPropietario: string): Observable<any> {
    return this.http.delete<any>(`${this.urlService.apiUrlDeletePropietario}/${codigoPropietario}`);
  }

  getEdificioSimple(): Observable<any> {
    return this.http.get<any>(this.urlService.apiUrlGetEdificioSimple);
  }

  asignarPropietario(propietarioId: number, departamentoId: number): Observable<any> {
    const url = `${this.urlService.apiUrlAsignarDepa}/${propietarioId}/departamentos/${departamentoId}`;
    return this.http.post<any>(url, null);
  }

  // Métodos para sincronización con AuthService

  login(email: string, password: string): Observable<{
    token: string;
    userId: number;
    username: string;
    roles: string[];
    idCompany: number;
  }> {
    return this.http.post<{
      token: string;
      userId: number;
      username: string;
      roles: string[];
      idCompany: number;
    }>(this.urlService.apiUrlLogin, { email, password });
  }
  register(userData: any): Observable<any> {
    return this.http.post(this.urlService.apiUrlRegister, userData);
  }

  syncUserWithBackend(userData: AuthUser): Observable<any> {
    return this.http.post(this.urlService.apiUrlSyncUser, userData);
  }

  validateToken(token: string): Observable<any> {
    return this.http.post(this.urlService.apiUrlValidateToken, { token });
  }

  refreshToken(): Observable<{ token: string }> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      return throwError(() => 'No refresh token available');
    }

    return this.http.post<{ token: string }>(
      `${this.baseUrl}/api/auth/refresh`,
      { refreshToken }
    ).pipe(
      catchError(error => {
        // Si falla el refresh, limpia todo
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        return throwError(() => error);
      })
    );
  }

  logout(): Observable<any> {
    return this.http.post(
      this.urlService.apiUrlLogout,
      {},
      {
        withCredentials: true,
        responseType: 'text'
      }
    ).pipe(
      tap(() => console.log('Backend logout successful')),
      catchError(error => {
        if (error.status === 0) {
          console.warn('Logout: CORS/Network error, pero se completó localmente');
          return of('Logout completado (CORS ignorado)');
        }
        console.error('Error en logout:', error);
        return of('Logout completado (con error ignorado)');
      })
    );
  }

  loginWithGoogle(accessToken: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post(`${environment.apiUrl}/api/auth/login/google`, {
      access_token: accessToken
    }, { headers });
  }


  // Operaciones de Compañía

  registrarCompania(companyData: any, token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    return this.http.post(`${this.baseUrl}/api/company`, companyData, { headers });
  }

  asociarUsuarioCompania(userId: number, companyId: number, token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    return this.http.put(
      `${this.baseUrl}/api/users/${userId}/company/${companyId}`,
      { companyId },
      { headers }
    );
  }

  getCompanias(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetCompanias);
  }

  getCompaniaByUser(userId: number): Observable<any> {
    const url = `${this.urlService.apiUrlGetCompaniaByUser}/${userId}/company`;
    return this.http.get<any>(url);
  }

}