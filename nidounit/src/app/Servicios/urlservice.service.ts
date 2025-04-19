import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/environment.staging';
@Injectable({
  providedIn: 'root'
})
export class UrlserviceService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  public apiUrlGetApartamentos = `${this.baseUrl}/api/apartamentos`;
  public apiUrlGetTotalCapital = `${this.baseUrl}/api/capital`;
  public apiUrlGetPagos = `${this.baseUrl}/api/pago`;
  public apiUrlGetHabitacionesDisponibles = `${this.baseUrl}/api/propietario/apartamento-habitacion-disponible`;

  
}
