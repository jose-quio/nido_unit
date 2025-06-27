import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';

@Component({
  selector: 'app-contratos',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './contratos.component.html',
  styleUrl: './contratos.component.scss'
})
export class ContratosComponent implements OnInit {

  FormularioContrato: FormGroup;
  contratos: any[] = [];
  departamentosDisponibles: any[] = [];
  propietarios: any[] = [];
  editingRowIndex: number = -1;

  constructor(
    private fb: FormBuilder,
    private miServicio: BackserviceService
  ) {
    this.FormularioContrato = this.fb.group({
      tipo: ['', [Validators.required]],
      fechaInicio: ['', [Validators.required]],
      numeroMeses: ['', [Validators.required, Validators.min(1)]],
      departamentoId: ['', [Validators.required]],
      propietarioId: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.obtenerContratos();
    this.obtenerDepartamentosDisponibles();
    this.obtenerPropietarios();
  }

  onSubmit(): void {
    if (this.FormularioContrato.valid) {
      const contratoData = {
        tipo: this.FormularioContrato.value.tipo,
        fechaInicio: this.FormularioContrato.value.fechaInicio,
        numeroMeses: parseInt(this.FormularioContrato.value.numeroMeses),
        departamentoId: parseInt(this.FormularioContrato.value.departamentoId),
        propietarioId: parseInt(this.FormularioContrato.value.propietarioId)
      };

      this.miServicio.postContrato(contratoData).subscribe(
        response => {
          console.log('Contrato creado exitosamente:', response);
          this.FormularioContrato.reset();
          this.obtenerContratos(); // Actualizar la lista
        },
        error => {
          console.error('Error al crear el contrato:', error);
        }
      );
    } else {
      console.log('Formulario inválido');
      this.markFormGroupTouched();
    }
  }

  obtenerContratos(): void {
    this.miServicio.getContratos().subscribe(
      data => {
        this.contratos = data;
        console.log('Contratos obtenidos:', this.contratos);
      },
      error => {
        console.error('Error al obtener los contratos:', error);
      }
    );
  }

  obtenerDepartamentosDisponibles(): void {
    this.miServicio.getDepartamentosDisponibles().subscribe(
      data => {
        this.departamentosDisponibles = data;
        console.log('Departamentos disponibles:', this.departamentosDisponibles);
      },
      error => {
        console.error('Error al obtener departamentos:', error);
      }
    );
  }

  obtenerPropietarios(): void {
    this.miServicio.getPropietarios().subscribe(
      data => {
        this.propietarios = data;
        console.log('Propietarios obtenidos:', this.propietarios);
      },
      error => {
        console.error('Error al obtener los propietarios:', error);
      }
    );
  }

  isEditingRow(index: number): boolean {
    return this.editingRowIndex === index;
  }

  editar(index: number): void {
    this.editingRowIndex = index;
  }

  guardarEdicion(index: number): void {
    const contrato = this.contratos[index];
    
    const contratoActualizado = {
      tipo: contrato.tipo,
      fechaInicio: contrato.fechaInicio,
      numeroMeses: parseInt(contrato.numeroMeses),
      departamentoId: parseInt(contrato.departamentoId),
      propietarioId: parseInt(contrato.propietarioId)
    };

    this.miServicio.putContrato(contrato.id, contratoActualizado).subscribe(
      response => {
        console.log('Contrato actualizado exitosamente:', response);
        this.editingRowIndex = -1;
        this.obtenerContratos(); // Actualizar la lista
      },
      error => {
        console.error('Error al actualizar el contrato:', error);
      }
    );
  }

  eliminar(index: number): void {
    if (confirm('¿Está seguro de que desea eliminar este contrato?')) {
      const contratoId = this.contratos[index].id;
      
      this.miServicio.deleteContrato(contratoId).subscribe(
        response => {
          console.log('Contrato eliminado exitosamente:', response);
          this.obtenerContratos(); // Actualizar la lista
        },
        error => {
          console.error('Error al eliminar el contrato:', error);
        }
      );
    }
  }

  obtenerNombreDepartamento(departamentoId: number): string {
    const departamento = this.departamentosDisponibles.find(d => d.id === departamentoId);
    return departamento ? (departamento.nombre || `Depto ${departamento.id}`) : `ID: ${departamentoId}`;
  }

  obtenerNombrePropietario(propietarioId: number): string {
    const propietario = this.propietarios.find(p => p.id === propietarioId);
    return propietario ? `${propietario.nombre} ${propietario.apellido}` : `ID: ${propietarioId}`;
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES');
  }

  calcularFechaFin(fechaInicio: string, numeroMeses: number): string {
    if (!fechaInicio || !numeroMeses) return '';
    
    const fecha = new Date(fechaInicio);
    fecha.setMonth(fecha.getMonth() + numeroMeses);
    return fecha.toLocaleDateString('es-ES');
  }

  obtenerEstadoContrato(contrato: any): string {
    if (!contrato.fechaInicio || !contrato.numeroMeses) return 'activo';
    
    const fechaInicio = new Date(contrato.fechaInicio);
    const fechaFin = new Date(fechaInicio);
    fechaFin.setMonth(fechaFin.getMonth() + contrato.numeroMeses);
    
    const hoy = new Date();
    
    if (hoy > fechaFin) {
      return 'vencido';
    } else if (hoy >= fechaInicio && hoy <= fechaFin) {
      return 'activo';
    } else {
      return 'activo';
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.FormularioContrato.controls).forEach(key => {
      const control = this.FormularioContrato.controls[key];
      control.markAsTouched();
    });
  }
}