import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';
import { environment } from '../../enviroments/environment.staging';

@Component({
  selector: 'app-apartamento-hab',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './apartamento-hab.component.html',
  styleUrl: './apartamento-hab.component.scss'
})
export class ApartamentoHabComponent {
  private baseUrl = environment.apiUrl;
  constructor(private http: HttpClient, private miServicio: BackserviceService) {
    this.obtenerApartamentos();
    this.obtenerApartamentosHab();
  }
  apartamentosHab: any[] = [];
  isEditingHab: boolean = false;
  isEditingRowIndexHab: number | null = null;
  apartamentos: any[] = [];

  FormularioApartamentoHab = new FormGroup({
    CodigoApartamento: new FormControl(''),
    TipoHabitacion: new FormControl(''),
    NumeroHabitaciones: new FormControl(''),
    DimenAlto: new FormControl(''),
    DimenAncho: new FormControl(''),
    DimenLargo: new FormControl(''),
    Estado: new FormControl(''),

  })
  obtenerApartamentosHab() {
    this.http.get<any[]>(this.baseUrl + '/api/apartamento-habitacion').subscribe(
      data => {
        this.apartamentosHab = data;
        console.log('Apartamentos de habitación obtenidos:', this.apartamentosHab);
      },
      error => {
        console.error('Error al obtener los apartamentos de habitación:', error);
      }
    );
  }
  obtenerApartamentos(){
    this.miServicio.getApartamentos().subscribe(
      (data) => {
        this.apartamentos = data;
      },
      (error) => {
        console.error('Error al obtener los apartamentos', error);
      }
    );
  }
  onSubmit() {
    if (this.FormularioApartamentoHab.valid) {
      this.http.post<any>(this.baseUrl + '/api/apartamento-habitacion', this.FormularioApartamentoHab.value)
        .subscribe(
          response => {
            console.log('Apartamento de habitación registrado con éxito:', response);
            this.obtenerApartamentosHab();
            this.FormularioApartamentoHab.reset();
          },
          error => {
            console.error('Error al registrar la habitación:', error);
          }
        );
    } else {
      console.warn('Formulario inválido');
    }
  }

  editarHab(index: number) {
    const apartamentoHab = this.apartamentosHab[index];
    this.isEditingRowIndexHab = index;
    this.FormularioApartamentoHab.setValue({
      CodigoApartamento: apartamentoHab.CodigoApartamento,
      TipoHabitacion: apartamentoHab.TipoHabitacion,
      NumeroHabitaciones: apartamentoHab.NumeroHabitaciones,
      DimenAlto: apartamentoHab.DimenAlto,
      DimenAncho: apartamentoHab.DimenAncho,
      DimenLargo: apartamentoHab.DimenLargo,
      Estado: apartamentoHab.Estado
    });
  }

  isEditingRowHab(index: number): boolean {
    return this.isEditingRowIndexHab === index;
  }

  guardarEdicionHab(index: number) {
    const apartamentoHab = this.apartamentosHab[index];
  
    console.log('Datos antes de enviar:', apartamentoHab);
  
    const formularioData = this.FormularioApartamentoHab.value;
  
    if (!formularioData.TipoHabitacion) {
      console.error('El campo TipoHabitacion no puede estar vacío.');
      return;
    }
  
    const actualizado = {
      ...formularioData,  
      CodigoHabitacion: apartamentoHab.CodigoHabitacion 
    };
  
    this.http.put(this.baseUrl + `/api/apartamento-habitacion/${apartamentoHab.CodigoHabitacion}`, actualizado, { responseType: 'text' })
      .subscribe(response => {
        console.log('Apartamento de habitación actualizado:', response);
        this.isEditingRowIndexHab = null;
        this.obtenerApartamentosHab();
        this.FormularioApartamentoHab.reset();
      }, error => {
        console.error('Error al actualizar el apartamento de habitación:', error);
      });
  }

  eliminarHab(index: number) {
    const apartamentoHab = this.apartamentosHab[index];
    console.log('Eliminar apartamento de habitación con código:', apartamentoHab.CodigoApartamento);

    if (confirm('¿Estás seguro de que deseas eliminar este apartamento de habitación?')) {
      this.http.delete(this.baseUrl + `/api/apartamento-habitacion/${apartamentoHab.CodigoHabitacion}`, { responseType: 'text' })
        .subscribe(
          response => {
            this.apartamentosHab.splice(index, 1);
            console.log('Apartamento de habitación eliminado:', response);
            alert(response);  
          },
          error => {
            console.error('Error al eliminar el apartamento de habitación:', error);
          }
        );
    }
  }
}