import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.staging';
@Injectable({
  providedIn: 'root'
})
export class UrlserviceService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }
  //CRUD Departamentos
  public apiUrlRegistrarApartamento = `${this.baseUrl}/api/edificio`
  public apiUrlGetApartamentos = `${this.baseUrl}/api/edificio`;
  public apiUrlDeleteApartamento = `${this.baseUrl}/api/edificio`;
  public apiUrlActualizarApartamento = `${this.baseUrl}/api/edificio`;
  //CRUD Habitaciones
  public apiUrlRegistrarApartamentoHab = `${this.baseUrl}/api/apartamentos`;
  public apiUrlGetApartamentosHab = `${this.baseUrl}/api/apartamentos`;
  public apiUrlUpdateApartamentoHab = `${this.baseUrl}/api/apartamentos`;
  public apiUrlDeleteApartamentoHab = `${this.baseUrl}/api/apartamentos`
  //Caja
  public apiUrlGetTotalCapital = `${this.baseUrl}/api/capital`;
  public apiUrlGuardarPago = `${this.baseUrl}/api/caja`;
  //Pagos
  public apiUrlGetPagosDNI = `${this.baseUrl}/api/pagos`;
  public apiUrlGetTodosPagos = `${this.baseUrl}/api/pagos`;
  //CRUD Propietarios
  public apiUrlGetDepartamentosDisponibles = `${this.baseUrl}/api/apartamentos/disponibles`;
  public apiUrlRegistrarPropietario = `${this.baseUrl}/api/propietario`;
  public apiUrlGetPropietarios = `${this.baseUrl}/api/propietario`;
  public apiUrlUpdatePropietario = `${this.baseUrl}/api/propietario`;
  public apiUrlDeletePropietario = `${this.baseUrl}/api/propietario`;
  public apiUrlGetEdificioSimple = `${this.baseUrl}/api/edificio/EdificioSimple`;
  public apiUrlAsignarDepa = `${this.baseUrl}/api/propietario`;

  // ENDPOINTS DE AUTENTICACIÓN
  public apiUrlLogin = `${this.baseUrl}/api/auth/login`;
  public apiUrlRegister = `${this.baseUrl}/api/auth/register`;
  public apiUrlSyncUser = `${this.baseUrl}/api/auth/sync-user`;
  public apiUrlValidateToken = `${this.baseUrl}/api/auth/validate-token`;
  public apiUrlRefreshToken = `${this.baseUrl}/api/auth/refresh`;
  public apiUrlLogout = `${this.baseUrl}/api/auth/logout`;

  // ENDPOINTS DE COMPAÑÍA
  public apiUrlRegistrarCompania = `${this.baseUrl}/api/company`;
  public apiUrlGetCompanias = `${this.baseUrl}/api/company`;
  public apiUrlGetCompaniaByUser = `${this.baseUrl}/api/users`;
  
  //ENDPOINTS DE CONTRATOS
  public apiUrlGetContratos = `${this.baseUrl}/api/contratos`;
  public apiUrlPostContrato = `${this.baseUrl}/api/contratos`;
  public apiUrlPutContrato = `${this.baseUrl}/api/contratos`;
  public apiUrlDeleteContrato = `${this.baseUrl}/api/contratos`;

  //ENDPOINTS DE COLABORADORES
  public apiUrlGuardarColaborador = `${this.baseUrl}/api/users`;
  public apiUrlObtenerColaboradores = `${this.baseUrl}/api/users`;
  public apiUrlEliminarColaborador = `${this.baseUrl}/api/users`;
  public apiUrlActualizarColaborador = `${this.baseUrl}/api/users`;

  //ENDPOINT DE GASTOS
  public apiUrlRegistrarGastos = `${this.baseUrl}/api/gastos/company`;
  public apiUrlGetGastos = `${this.baseUrl}/api/gastos/company`;
  public apiUrlEliminarGastos = `${this.baseUrl}/api/gastos/`;
}
