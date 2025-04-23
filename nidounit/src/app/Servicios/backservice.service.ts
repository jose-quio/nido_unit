import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UrlserviceService } from './urlservice.service';

@Injectable({
  providedIn: 'root'
})
export class BackserviceService {

  constructor(private http: HttpClient, private urlService: UrlserviceService) { }

  // Operaciones de Apartamentos
  registrarApartamento(apartamento: any): Observable<any> {
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

  //Operaciones de Caja
  getTotalCapital(): Observable<any> {
    return this.http.get<any>(this.urlService.apiUrlGetTotalCapital);
  }

  guardarPago(pago: any): Observable<any> {
    return this.http.post(this.urlService.apiUrlGuardarPago, pago);
  }

  //Operaciones de Pago
  getPagosDNI(dni: string): Observable<any> {
    return this.http.get<any>(`${this.urlService.apiUrlGetPagosDNI}/${dni}`);
  }

  getTodosLosPagos(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetTodosPagos);
  }
  //Operaciones de propietario
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
}
