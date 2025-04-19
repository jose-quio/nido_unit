import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';
import { environment } from '../../enviroments/environment.staging';


@Component({
  selector: 'app-propietario',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './propietario.component.html',
  styleUrl: './propietario.component.scss'
})
export class PropietarioComponent {
  private baseUrl = environment.apiUrl;

  propietarios: any[] = [];
  isEditingRowIndexProp: number | null = null;
  habitacionesDisponibles: any[] = [];

  constructor(private http: HttpClient, private miServicio: BackserviceService) { 
    this.obtenerPropietarios();
    this.getHabitacionesDisponibles();
  }

  FormularioPropietario = new FormGroup({
    Nombre: new FormControl(''),
    Apellido: new FormControl(''),
    DNI: new FormControl(''),
    Telefono: new FormControl(''),
    DeudaAnual: new FormControl(''),
    PagoMensual: new FormControl(''),
    CodigoHabitacion: new FormControl(''),
  });

  obtenerPropietarios() {
    this.http.get<any[]>(this.baseUrl + '/api/propietario').subscribe(
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
    
    this.miServicio.getHabitacionesDisponibles().subscribe(
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
      this.http.post<any>(this.baseUrl + '/api/propietario', this.FormularioPropietario.value)
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
      DeudaAnual: propietario.DeudaAnual,
      PagoMensual: propietario.PagoMensual,
      CodigoHabitacion: propietario.CodigoHabitacion  
    });
  }

  guardarEdicionProp(index: number) {
    const propietario = this.propietarios[index];
    const formularioData = this.FormularioPropietario.value;

    const actualizado = {
      ...formularioData,
      CodigoPropietario: propietario.CodigoPropietario
    };

    this.http.put(this.baseUrl + `/api/propietario/${propietario.CodigoPropietario}`, actualizado, { responseType: 'text' })
      .subscribe(response => {
        console.log('Propietario actualizado:', response);
        this.isEditingRowIndexProp = null;
        this.obtenerPropietarios();
        this.FormularioPropietario.reset();
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
      this.http.delete(this.baseUrl + `/api/propietario/${propietario.CodigoPropietario}`, { responseType: 'text' })
        .subscribe(
          response => {
            this.propietarios.splice(index, 1);
            console.log('Propietario eliminado:', response);
            
          },
          error => {
            console.error('Error al eliminar el propietario:', error);
          }
        );
    }
  }

}