import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { environment } from '../../environments/environment.staging';
import { BackserviceService } from '../Servicios/backservice.service';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-caja',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule,CurrencyPipe],
  templateUrl: './caja.component.html',
  styleUrl: './caja.component.scss'
})
export class CajaComponent {
  totalIngresos: number = 0;
  totalGastos: number = 0;
  utilidad: number = 0;
  
  constructor(private http: HttpClient, private miServicio:BackserviceService, private fb: FormBuilder){    
    this.obtenerCapital();
  }
  
  obtenerCapital() {
    this.miServicio.getTotalCapital().subscribe(
      (data) => {
        if (data) {
          this.totalIngresos = data.totalIngresos || 0;
          this.totalGastos = data.totalGastos || 0;
          this.utilidad = data.utilidad || 0;
        }
      },
      (error) => {
        console.error('Error al obtener el total del capital:', error);
      }
    );
  }
}