import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';
import { UrlserviceService } from './urlservice.service';
import { AuthUser } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BackserviceService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient, private urlService: UrlserviceService) { }

  // Operaciones de Edificios
  registrarApartamento(apartamento: any): Observable<any> {
    if (!apartamento.company?.id) {
      return throwError(() => new Error('ID de compañía no proporcionado'));
    }

    return this.http.post<any>(this.urlService.apiUrlRegistrarApartamento, apartamento);
  }

  getApartamentos(): Observable<any[]> {
    const companyId = localStorage.getItem('idCompany');
    const url = `${this.urlService.apiUrlGetApartamentos}/company/${companyId}`;
    return this.http.get<any[]>(url);
  }


  eliminarApartamento(codigoApartamento: string): Observable<any> {
    return this.http.delete<any>(`${this.urlService.apiUrlDeleteApartamento}/${codigoApartamento}`);
  }

  guardarEdicion(codigoApartamento: string, apartamento: any): Observable<any> {
    return this.http.put<any>(`${this.urlService.apiUrlActualizarApartamento}/${codigoApartamento}`, apartamento);
  }

  // Operaciones de Departamentos
  registrarApartamentoHab(apartamentoHab: any): Observable<any> {
    return this.http.post<any>(this.urlService.apiUrlRegistrarApartamentoHab, apartamentoHab);
  }

  getApartamentosHab(): Observable<any[]> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.get<any[]>(`${this.urlService.apiUrlGetApartamentosHab}/${companyId}`);
  }

  actualizarApartamentoHab(codigoHabitacion: string, apartamentoHab: any): Observable<any> {
    return this.http.put<any>(`${this.urlService.apiUrlUpdateApartamentoHab}/${codigoHabitacion}`, apartamentoHab);
  }

  eliminarApartamentoHab(codigoHabitacion: string): Observable<any> {
    return this.http.delete<any>(`${this.urlService.apiUrlDeleteApartamentoHab}/${codigoHabitacion}`);
  }

  // Operaciones de Caja
  getTotalCapital(): Observable<any> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.get<any>(`${this.urlService.apiUrlGetTotalCapital}/${companyId}/caja`);
  }

  guardarPago(pago: any): Observable<any> {
    return this.http.post(this.urlService.apiUrlGuardarPago, pago);
  }

  // Operaciones de Pago
  getPagosDNI(dni: string): Observable<any> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.get<any>(`${this.urlService.apiUrlGetPagosDNI}/${companyId}/dni/${dni}`);
  }

  getTodosLosPagos(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetTodosPagos);
  }

  procesarPago(pagoId: number): Observable<string> {
    return this.http.put(`${this.urlService.apiUrlGetTodosPagos}/${pagoId}/pagar`, {}, {
      responseType: 'text'
    });
  }
  // Operaciones de propietario
  getDepartamentosDisponibles(): Observable<any[]> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.get<any[]>(`${this.urlService.apiUrlGetDepartamentosDisponibles}/${companyId}`);
  }

  registrarPropietario(propietario: any): Observable<any> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.post<any>(`${this.urlService.apiUrlRegistrarPropietario}/${companyId}`, propietario);
  }

  getPropietarios(): Observable<any[]> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.get<any[]>(`${this.urlService.apiUrlGetPropietarios}/${companyId}`);
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
    nombreCompany: string;
  }> {
    return this.http.post<{
      token: string;
      userId: number;
      username: string;
      roles: string[];
      idCompany: number;
      nombreCompany: string;
    }>(
      this.urlService.apiUrlLogin,
      { email, password },
      { withCredentials: true }
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post(
      this.urlService.apiUrlRegister,
      userData,
      { withCredentials: true }
    );
  }

  syncUserWithBackend(userData: AuthUser): Observable<any> {
    return this.http.post(this.urlService.apiUrlSyncUser, userData);
  }

  validateToken(token: string): Observable<any> {
    return this.http.post(this.urlService.apiUrlValidateToken, { token });
  }

  refreshToken(): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(
      `${this.baseUrl}/api/auth/refresh`,
      {},
      {
        withCredentials: true
      }
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
    }, {
      headers,
      withCredentials: true
    });
  }




  // Operaciones de Compañía

  registrarCompania(companyData: any, token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    return this.http.post(`${this.baseUrl}/api/company`, companyData, { headers, withCredentials: true });
  }

  asociarUsuarioCompania(userId: number, companyId: number, token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    return this.http.put(
      `${this.baseUrl}/api/users/${userId}/company/${companyId}`,
      { companyId },
      { headers, withCredentials: true }
    );
  }

  getCompanias(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetCompanias);
  }

  getCompaniaByUser(userId: number): Observable<any> {
    const url = `${this.urlService.apiUrlGetCompaniaByUser}/${userId}/company`;
    return this.http.get<any>(url);
  }


  //Contratos
  getContratos(): Observable<any[]> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.get<any[]>(`${this.urlService.apiUrlGetContratos}/${companyId}`);
  }

  postContrato(contrato: any): Observable<any> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.post<any>(`${this.urlService.apiUrlPostContrato}/${companyId}`, contrato);
  }

  putContrato(id: number, contrato: any): Observable<any> {
    return this.http.put<any>(`${this.urlService.apiUrlPutContrato}/${id}`, contrato);
  }

  deleteContrato(id: number): Observable<any> {
    return this.http.delete<any>(`${this.urlService.apiUrlDeleteContrato}/${id}`);
  }

  //Colaboradores
  guardarColaborador(colaborador: any): Observable<any> {
    return this.http.post(this.urlService.apiUrlGuardarColaborador, colaborador);
  }

  obtenerColaboradores(): Observable<any> {
    const companyId = localStorage.getItem('idCompany');
    return this.http.get(`${this.urlService.apiUrlObtenerColaboradores}/${companyId}`);
  }

  eliminarColaborador(id: number): Observable<any> {
    return this.http.delete(`${this.urlService.apiUrlEliminarColaborador}/${id}`);
  }

  actualizarColaborador(id: number, colaborador: any): Observable<any> {
    return this.http.put(`${this.urlService.apiUrlActualizarColaborador}/${id}`, colaborador);

  }

  //Gastos
  createGasto(idCompany: string, gastoData: any): Observable<any> {
    return this.http.post(`${this.urlService.apiUrlRegistrarGastos}/${idCompany}`, gastoData);
  }

  getGastos(idCompany: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.urlService.apiUrlGetGastos}/${idCompany}`);
  }

  deleteGasto(gastoId: number): Observable<any> {
    return this.http.delete(`${this.urlService.apiUrlEliminarGastos}/${gastoId}`);
  }
}