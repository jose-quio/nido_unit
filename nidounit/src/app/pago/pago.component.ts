import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { BackserviceService } from '../Servicios/backservice.service';

@Component({
  selector: 'app-pago',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './pago.component.html',
  styleUrl: './pago.component.scss'
})
export class PagoComponent {
  PagosMensuales: any[] = [];
  
  FormularioPago = new FormGroup({
    PagoMensual: new FormControl(''),    
    FechaPago: new FormControl(''),
  });

  constructor(private miServicio: BackserviceService) {
    this.obtenerTodosLosPagos();
  }

  obtenerTodosLosPagos() {
    this.miServicio.getTodosLosPagos().subscribe(
      (data) => {
        console.log('Todos los pagos recibidos:', data);
        this.PagosMensuales = data;
      },
      (error) => {
        console.error('Error al obtener todos los pagos:', error);
      }
    );
  }

  onSubmit() {
    if (this.FormularioPago.valid) {
      const dniInput = (document.querySelector('input[placeholder="DNI"]') as HTMLInputElement).value;
  
      if (!dniInput) {
        alert('Por favor, ingresa un DNI antes de registrar el pago.');
        return;
      }
  
      const payload = {
        ...this.FormularioPago.value,
        DNI: dniInput 
      };
  
      this.miServicio.guardarPago(payload).subscribe(
        response => {
          console.log('Pago registrado con éxito:', response);
          alert('El pago se ha registrado correctamente.');
          this.obtenerTodosLosPagos();
          this.FormularioPago.reset(); 
          (document.querySelector('input[placeholder="DNI"]') as HTMLInputElement).value = ''; 
        },
        error => {
          console.error('Error al registrar el pago:', error);
          alert('Hubo un error al registrar el pago. Por favor, intenta de nuevo.');
        }
      );
    } else {
      alert('Por favor completa todos los campos.');
    }
  }
  
  buscar() {
    const dniInput = (document.querySelector('input[placeholder="DNI"]') as HTMLInputElement).value;

    this.miServicio.getPagosDNI(dniInput).subscribe(
      (data) => {
        console.log('Dato recibido del servicio:', data);
        if (data && data.PagoMensual) {
          this.FormularioPago.patchValue({
            PagoMensual: data.PagoMensual,
          });
        } else {
          alert("No se encontró ningún usuario con ese DNI");
          console.warn('No se encontró el dato para el DNI ingresado.');
        }
      },
      (error) => {
        console.error('Error al obtener el pago mensual:', error);
        alert("No se encontró ningún usuario con ese DNI");
      }
    );
  }
}