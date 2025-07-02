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
      cantidadMeses: [''],
      departamentoId: ['', [Validators.required]],
      propietarioId: ['', [Validators.required]]
    });

    this.FormularioContrato.get('tipo')?.valueChanges.subscribe(tipo => {
      this.onTipoChange(tipo);
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
        cantidadMeses: this.FormularioContrato.value.tipo === 'ALQUILER' ?
          parseInt(this.FormularioContrato.value.cantidadMeses) :
          null,
        departamentoId: parseInt(this.FormularioContrato.value.departamentoId),
        propietarioId: parseInt(this.FormularioContrato.value.propietarioId)
      };

      this.miServicio.postContrato(contratoData).subscribe(
        response => {
          console.log('Contrato creado exitosamente:', response);
          this.FormularioContrato.reset();
          this.obtenerContratos();
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
      cantidadMeses: contrato.tipo === 'ALQUILER' ?
        parseInt(contrato.cantidadMeses) :
        null,
      departamentoId: parseInt(contrato.departamentoId),
      propietarioId: parseInt(contrato.propietarioId)
    };

    this.miServicio.putContrato(contrato.id, contratoActualizado).subscribe(
      response => {
        console.log('Contrato actualizado exitosamente:', response);
        this.editingRowIndex = -1;
        this.obtenerContratos();
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
          this.obtenerContratos();
        },
        error => {
          console.error('Error al eliminar el contrato:', error);
        }
      );
    }
  }
  getDepartamentoInfo(departamentoId: number): string {
    const departamento = this.departamentosDisponibles.find(d => d.id === departamentoId);
    return departamento ? `${departamento.numero} (${departamento.edificio?.nombre})` : `${departamentoId}`;
  }

  getPropietarioInfo(propietarioId: number): string {
    const propietario = this.propietarios.find(p => p.id === propietarioId);
    return propietario ? `${propietario.nombres} ${propietario.apellidos}` : `${propietarioId}`;
  }

  getEstadoContrato(contrato: any): string {
    if (contrato.tipo === 'VENTA') return 'finalizado';
    if (!contrato.fechaInicio || !contrato.cantidadMeses) return 'activo';

    const fechaInicio = new Date(contrato.fechaInicio);
    const fechaFin = new Date(fechaInicio);
    fechaFin.setMonth(fechaFin.getMonth() + contrato.cantidadMeses);

    return new Date() > fechaFin ? 'vencido' : 'activo';
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES');
  }

  calcularFechaFin(fechaInicio: string, cantidadMeses: number): string {
    if (!fechaInicio || !cantidadMeses) return '';

    const fecha = new Date(fechaInicio);
    fecha.setMonth(fecha.getMonth() + cantidadMeses);
    return fecha.toLocaleDateString('es-ES');
  }

  obtenerEstadoContrato(contrato: any): string {
    if (contrato.tipo === 'VENTA') {
      return 'finalizado';
    }

    if (!contrato.fechaInicio || !contrato.cantidadMeses) return 'activo';

    const fechaInicio = new Date(contrato.fechaInicio);
    const fechaFin = new Date(fechaInicio);
    fechaFin.setMonth(fechaFin.getMonth() + contrato.cantidadMeses);

    const hoy = new Date();

    if (hoy > fechaFin) {
      return 'vencido';
    } else if (hoy >= fechaInicio && hoy <= fechaFin) {
      return 'activo';
    } else {
      return 'activo';
    }
  }

  onTipoChange(tipo: string): void {
    const cantidadMesesControl = this.FormularioContrato.get('cantidadMeses');

    if (tipo === 'ALQUILER') {
      cantidadMesesControl?.setValidators([Validators.required, Validators.min(1)]);
      cantidadMesesControl?.setValue('');
    } else if (tipo === 'VENTA') {
      cantidadMesesControl?.clearValidators();
      cantidadMesesControl?.setValue(null);
    }

    cantidadMesesControl?.updateValueAndValidity();
  }

  tieneContratosAlquiler(): boolean {
    return this.contratos.some(contrato => contrato.tipo === 'ALQUILER');
  }

  private markFormGroupTouched(): void {
    Object.keys(this.FormularioContrato.controls).forEach(key => {
      const control = this.FormularioContrato.controls[key];
      control.markAsTouched();
    });
  }
}