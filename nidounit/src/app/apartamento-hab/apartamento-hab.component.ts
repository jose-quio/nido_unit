import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';

@Component({
  selector: 'app-apartamento-hab',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './apartamento-hab.component.html',
  styleUrl: './apartamento-hab.component.scss'
})
export class ApartamentoHabComponent {
  constructor(private miServicio: BackserviceService) {
    this.obtenerApartamentos();
    this.obtenerApartamentosHab();
  }

  apartamentosHab: any[] = [];
  isEditingHab: boolean = false;
  isEditingRowIndexHab: number | null = null;
  apartamentos: any[] = [];

  FormularioApartamentoHab = new FormGroup({
    CodigoApartamento: new FormControl(''),
    Numero: new FormControl(''),
    Piso: new FormControl(''),
    NumeroHabitaciones: new FormControl(''),
    PrecioVenta: new FormControl(''),
    PrecioAlquiler: new FormControl(''),
    Estado: new FormControl('Disponible'),
  });

  obtenerApartamentosHab() {
    this.miServicio.getApartamentosHab().subscribe(
      data => {
        this.apartamentosHab = data;
        console.log('Departamentos obtenidos:', this.apartamentosHab);
      },
      error => {
        console.error('Error al obtener los departamentos:', error);
      }
    );
  }

  obtenerApartamentos() {
    this.miServicio.getApartamentos().subscribe(
      (data) => {
        this.apartamentos = data;
        console.log('Edificios obtenidos:', this.apartamentos);
      },
      (error) => {
        console.error('Error al obtener los edificios', error);
      }
    );
  }

  onSubmit() {
    if (this.FormularioApartamentoHab.valid) {
      this.miServicio.registrarApartamentoHab(this.FormularioApartamentoHab.value)
        .subscribe(
          response => {
            console.log('Departamento registrado con éxito:', response);
            this.obtenerApartamentosHab();
            this.FormularioApartamentoHab.reset({
              Estado: 'Disponible'
            });
          },
          error => {
            console.error('Error al registrar el departamento:', error);
          }
        );
    } else {
      console.warn('Formulario inválido');
    }
  }

  editarHab(index: number) {
    const apartamentoHab = this.apartamentosHab[index];
    this.isEditingRowIndexHab = index;
  }

  isEditingRowHab(index: number): boolean {
    return this.isEditingRowIndexHab === index;
  }

  guardarEdicionHab(index: number) {
    const apartamentoHab = this.apartamentosHab[index];
    console.log('Actualizando departamento con código:', apartamentoHab.CodigoHabitacion);

    this.miServicio.actualizarApartamentoHab(apartamentoHab.CodigoHabitacion, apartamentoHab)
      .subscribe(response => {
        console.log('Departamento actualizado:', response);
        this.isEditingRowIndexHab = null;
        this.obtenerApartamentosHab();
      }, error => {
        console.error('Error al actualizar el departamento:', error);
      });
  }

  eliminarHab(index: number) {
    const apartamentoHab = this.apartamentosHab[index];
    console.log('Eliminar departamento con código:', apartamentoHab.CodigoHabitacion);

    if (confirm('¿Estás seguro de que deseas eliminar este departamento?')) {
      this.miServicio.eliminarApartamentoHab(apartamentoHab.CodigoHabitacion)
        .subscribe(
          response => {
            this.apartamentosHab.splice(index, 1);
            console.log('Departamento eliminado:', response);
            alert(response);
          },
          error => {
            console.error('Error al eliminar el departamento:', error);
          }
        );
    }
  }
}