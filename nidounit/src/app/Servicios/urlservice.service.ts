import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/environment.staging';
@Injectable({
  providedIn: 'root'
})
export class UrlserviceService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }
  //CRUD Departamentos
  public apiUrlRegistrarApartamento = `${this.baseUrl}/api/apartamentos`
  public apiUrlGetApartamentos = `${this.baseUrl}/api/apartamentos`;
  public apiUrlDeleteApartamento = `${this.baseUrl}/api/apartamentos`;
  public apiUrlActualizarApartamento = `${this.baseUrl}/api/apartamentos`;
  //CRUD Habitaciones
  public apiUrlRegistrarApartamentoHab = `${this.baseUrl}/api/apartamento-habitacion`;
  public apiUrlGetApartamentosHab = `${this.baseUrl}/api/apartamento-habitacion`;
  public apiUrlUpdateApartamentoHab = `${this.baseUrl}/api/apartamento-habitacion`;
  public apiUrlDeleteApartamentoHab = `${this.baseUrl}/api/apartamento-habitacion`
  //Caja
  public apiUrlGetTotalCapital = `${this.baseUrl}/api/capital`;
  public apiUrlGuardarPago = `${this.baseUrl}/api/caja`;
  //Pagos
  public apiUrlGetPagosDNI = `${this.baseUrl}/api/pago`;
  public apiUrlGetTodosPagos = `${this.baseUrl}/api/pago`;
  //CRUD Propietarios
  public apiUrlGetHabitacionesDisponibles = `${this.baseUrl}/api/propietario/apartamento-habitacion-disponible`;
  public apiUrlRegistrarPropietario = `${this.baseUrl}/api/propietario`;
  public apiUrlGetPropietarios = `${this.baseUrl}/api/propietario`;
  public apiUrlUpdatePropietario = `${this.baseUrl}/api/propietario`;
  public apiUrlDeletePropietario = `${this.baseUrl}/api/propietario`;
}
