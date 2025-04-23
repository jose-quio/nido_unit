import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';

@Component({
  selector: 'app-propietario',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './propietario.component.html',
  styleUrl: './propietario.component.scss'
})
export class PropietarioComponent {
  propietarios: any[] = [];
  isEditingRowIndexProp: number | null = null;
  habitacionesDisponibles: any[] = [];

  constructor(private miServicio: BackserviceService) {
    this.obtenerPropietarios();
    this.getHabitacionesDisponibles();
  }

  FormularioPropietario = new FormGroup({
    Nombre: new FormControl(''),
    Apellido: new FormControl(''),
    DNI: new FormControl(''),
    Telefono: new FormControl(''),
    Email: new FormControl(''),
    CodigoHabitacion: new FormControl(''),
  });

  obtenerPropietarios() {
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

  getHabitacionesDisponibles() {
    console.log('Iniciando la obtención de habitaciones disponibles...');

    this.miServicio.getDepartamentosDisponibles().subscribe(
      (data) => {
        console.log('Datos recibidos del servicio:', data);
        this.habitacionesDisponibles = data;
        console.log('Habitaciones disponibles asignadas:', this.habitacionesDisponibles);
      },
      (error) => {
        console.error('Error al obtener habitaciones disponibles:', error);
      }
    );
  }

  onSubmit() {
    if (this.FormularioPropietario.valid) {
      this.miServicio.registrarPropietario(this.FormularioPropietario.value)
        .subscribe(
          response => {
            console.log('Propietario registrado con éxito:', response);
            this.obtenerPropietarios();
            this.FormularioPropietario.reset();
          },
          error => {
            console.error('Error al registrar el propietario:', error);
          }
        );
    } else {
      console.warn('Formulario inválido');
      alert('Por favor complete todos los campos requeridos.');
    }
  }

  editarPropietario(index: number) {
    const propietario = this.propietarios[index];
    this.isEditingRowIndexProp = index;
    this.FormularioPropietario.setValue({
      Nombre: propietario.Nombre,
      Apellido: propietario.Apellido,
      DNI: propietario.DNI,
      Telefono: propietario.Telefono,
      Email: propietario.Email || '',  
      CodigoHabitacion: propietario.CodigoHabitacion
    });
  }

  guardarEdicionProp(index: number) {
    const propietario = this.propietarios[index];
    console.log('Guardando edición de propietario:', propietario);

    this.miServicio.actualizarPropietario(propietario.CodigoPropietario, propietario)
      .subscribe(response => {
        console.log('Propietario actualizado:', response);
        this.isEditingRowIndexProp = null;
        this.obtenerPropietarios();
      }, error => {
        console.error('Error al actualizar el propietario:', error);
      });
  }

  isEditingRowProp(index: number): boolean {
    return this.isEditingRowIndexProp === index;
  }

  eliminarPropietario(index: number) {
    const propietario = this.propietarios[index];
    console.log('Eliminar propietario con código:', propietario.CodigoPropietario);

    if (confirm('¿Estás seguro de que deseas eliminar este propietario?')) {
      this.miServicio.eliminarPropietario(propietario.CodigoPropietario)
        .subscribe(
          response => {
            this.propietarios.splice(index, 1);
            console.log('Propietario eliminado:', response);
            alert('Propietario eliminado correctamente');
          },
          error => {
            console.error('Error al eliminar el propietario:', error);
            alert('Error al eliminar el propietario');
          }
        );
    }
  }
}