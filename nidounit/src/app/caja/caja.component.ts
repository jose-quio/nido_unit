import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';

@Component({
  selector: 'app-caja',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './caja.component.html',
  styleUrl: './caja.component.scss'
})
export class CajaComponent {
  totalCapital: number = 0;
  constructor(private http: HttpClient, private miServicio:BackserviceService, private fb: FormBuilder){    
    this.obtenerCapital();
  }
  
  obtenerCapital() {
    this.miServicio.getTotalCapital().subscribe(
      (data) => {
        if (data && data.totalCapital !== null) {
          this.totalCapital = data.totalCapital;
          
        }
      },
      (error) => {
        console.error('Error al obtener el total del capital:', error);
      }
    );
  }

  
  

  
}
