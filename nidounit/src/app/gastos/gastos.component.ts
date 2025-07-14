import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';
import { AuthService } from '../Servicios/auth.service';

interface Gasto {
  id: number;
  descripcion: string;
  monto: number;
  fecha: string;
  tipo: 'SERVICIO' | 'MANTENIMIENTO' | 'INFRAESTRUCTURA' | 'ADMINISTRATIVO' | 'OTRO';
}

@Component({
  selector: 'app-gastos',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    DatePipe,
    DecimalPipe
  ],
  templateUrl: './gastos.component.html',
  styleUrls: ['./gastos.component.scss']
})
export class GastosComponent implements OnInit {
  FormularioGasto: FormGroup;
  gastos: Gasto[] = [];
  idCompany: string | null = null;

  constructor(
    private fb: FormBuilder,
    private backService: BackserviceService,
    public authService: AuthService
  ) {
    this.FormularioGasto = this.fb.group({
      descripcion: ['', Validators.required],
      monto: ['', [Validators.required, Validators.min(0.01)]],
      fecha: ['', Validators.required],
      tipo: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.idCompany = localStorage.getItem('idCompany');
    this.cargarGastos();
  }

  onSubmit() {
    if (this.FormularioGasto.valid && this.idCompany) {
      const gastoData = {
        descripcion: this.FormularioGasto.get('descripcion')?.value,
        monto: parseFloat(this.FormularioGasto.get('monto')?.value),
        fecha: this.FormularioGasto.get('fecha')?.value,
        tipo: this.FormularioGasto.get('tipo')?.value
      };

      this.backService.createGasto(this.idCompany, gastoData).subscribe({
        next: (response) => {
          console.log('Gasto registrado exitosamente:', response);
          this.FormularioGasto.reset();
          this.cargarGastos(); // Recargar la lista
        },
        error: (error) => {
          console.error('Error al registrar el gasto:', error);
        }
      });
    }
  }

  cargarGastos() {
    if (this.idCompany) {
      this.backService.getGastos(this.idCompany).subscribe({
        next: (gastos) => {
          this.gastos = gastos;
        },
        error: (error) => {
          console.error('Error al cargar los gastos:', error);
        }
      });
    }
  }

  eliminar(gastoId: number) {
    if (confirm('¿Está seguro de que desea eliminar este gasto?')) {
      this.backService.deleteGasto(gastoId).subscribe({
        next: (response) => {
          console.log('Gasto eliminado exitosamente:', response);
          this.cargarGastos(); // Recargar la lista
        },
        error: (error) => {
          console.error('Error al eliminar el gasto:', error);
        }
      });
    }
  }

  getTipoGastoLabel(tipo: string): string {
    const labels: { [key: string]: string } = {
      'SERVICIO': 'Servicio',
      'MANTENIMIENTO': 'Mantenimiento',
      'INFRAESTRUCTURA': 'Infraestructura',
      'ADMINISTRATIVO': 'Administrativo',
      'OTRO': 'Otro'
    };
    return labels[tipo] || tipo;
  }

  getTotalGastos(): number {
    return this.gastos.reduce((total, gasto) => total + gasto.monto, 0);
  }
}