import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UrlserviceService } from './urlservice.service';
import { environment } from '../../enviroments/environment.staging';

@Injectable({
  providedIn: 'root'
})
export class BackserviceService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient,private urlService: UrlserviceService) { }

  getApartamentos(): Observable<any[]> {
    return this.http.get<any[]>(this.urlService.apiUrlGetApartamentos);
  }

  getTotalCapital(): Observable<any> {
    return this.http.get<any>(this.urlService.apiUrlGetTotalCapital);
  }

  guardarPago(pago: any): Observable<any> {
    return this.http.post(this.baseUrl + '/api/caja', pago);
  }

  getPagos(dni: string): Observable<any> { 
    return this.http.get<any>(`${this.urlService.apiUrlGetPagos}/api/pago/${dni}`);
  }

  getTodosLosPagos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/pago`);
  }

  getHabitacionesDisponibles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.urlService.apiUrlGetHabitacionesDisponibles}`);
  }
}
