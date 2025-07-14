import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { BackserviceService } from '../Servicios/backservice.service';
import { AuthService } from '../Servicios/auth.service';

declare var bootstrap: any;

@Component({
  selector: 'app-pago',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './pago.component.html',
  styleUrl: './pago.component.scss'
})
export class PagoComponent {
  PagosMensuales: any[] = [];
  pagoSeleccionado: any = null;
  modalPago: any;
  
  cargandoPago: boolean = false;
  pagoExitoso: boolean = false;
  
  formularioBusqueda: FormGroup;
  dniFiltrado: string = '';
  buscandoPagos: boolean = false;
  
  constructor(private miServicio: BackserviceService, public authService: AuthService) {
    this.formularioBusqueda = new FormGroup({
      dni: new FormControl('', [Validators.required, Validators.pattern(/^\d{8}$/)])
    });
    
    this.PagosMensuales = [];
  }

  ngAfterViewInit() {
    const modalElement = document.getElementById('modalConfirmarPago');
    if (modalElement) {
      this.modalPago = new bootstrap.Modal(modalElement);
    }
  }

  buscarPagosPorDNI() {
    if (this.formularioBusqueda.valid) {
      const dni = this.formularioBusqueda.get('dni')?.value;
      this.dniFiltrado = dni;
      this.buscandoPagos = true;
      
      this.miServicio.getPagosDNI(dni).subscribe(
        (data) => {
          console.log('Pagos por DNI recibidos:', data);
          this.PagosMensuales = data;
          this.buscandoPagos = false;
        },
        (error) => {
          console.error('Error al obtener pagos por DNI:', error);
          this.PagosMensuales = [];
          this.buscandoPagos = false;
          alert('No se encontraron pagos para el DNI ingresado o hubo un error en la búsqueda.');
        }
      );
    }
  }

  limpiarBusqueda() {
    this.formularioBusqueda.reset();
    this.dniFiltrado = '';
    this.PagosMensuales = [];
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

  recargarPagos() {
    if (this.dniFiltrado) {
      this.miServicio.getPagosDNI(this.dniFiltrado).subscribe(
        (data) => {
          this.PagosMensuales = data;
        },
        (error) => {
          console.error('Error al recargar pagos por DNI:', error);
        }
      );
    } else {
      this.obtenerTodosLosPagos();
    }
  }

  abrirModalPago(pago: any) {
    if (pago.estado === 'PAGADO') {
      return; 
    }
    
    this.pagoSeleccionado = pago;
    this.cargandoPago = false;
    this.pagoExitoso = false;
    this.modalPago.show();
  }

  confirmarPago() {
    if (!this.pagoSeleccionado) {
      return;
    }

    this.cargandoPago = true;
    const pagoId = this.pagoSeleccionado.id;
    
    this.miServicio.procesarPago(pagoId).subscribe(
      (response: string) => {
        console.log('Respuesta del servidor:', response); 
        
        const index = this.PagosMensuales.findIndex(p => p.id === pagoId);
        if (index !== -1) {
          this.PagosMensuales[index].estado = 'PAGADO';
        }
        
        this.cargandoPago = false;
        this.pagoExitoso = true;
      },
      (error) => {
        console.error('Error al procesar el pago:', error);
        this.cargandoPago = false;
        alert('Hubo un error al procesar el pago. Por favor, intenta de nuevo.');
        this.cerrarModal();
      }
    );
  }

  cerrarModal() {
    this.modalPago.hide();
    this.pagoSeleccionado = null;
    this.cargandoPago = false;
    this.pagoExitoso = false;
  }

  aceptarPagoExitoso() {
    this.cerrarModal();
    this.recargarPagos();
  }
}