import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { environment } from '../../enviroments/environment.staging';
import { BackserviceService } from '../Servicios/backservice.service';


@Component({
  selector: 'app-apartamento',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './apartamento.component.html',
  styleUrl: './apartamento.component.scss'
})
export class ApartamentoComponent {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient, private backService: BackserviceService) {
    this.obtenerApartamentos();
  }
  apartamentos: any[] = [];
  isEditing: boolean = false;
  isEditingRowIndex: number | null = null;

  FormularioApartamento = new FormGroup({
    nombre: new FormControl<string>(''),  
    direccion: new FormControl<string>(''),
    nroPisos: new FormControl<number | null>(null),  
    tipo: new FormControl<string>('residencial'),
    descripcion: new FormControl<string>('')
  });

  onSubmit() {
    console.log('Valores del formulario:', this.FormularioApartamento.value);
    console.log('Estado del formulario:', this.FormularioApartamento.status);
    
    if (this.FormularioApartamento.valid) {
      console.log('Enviando datos:', JSON.stringify(this.FormularioApartamento.value));
      this.backService.registrarApartamento(this.FormularioApartamento.value)
        .subscribe(
          response => {
            console.log('Edificio registrado con éxito:', response);
            this.obtenerApartamentos();
            this.FormularioApartamento.reset({
              tipo: 'residencial'  
            });
          },
          error => {
            console.error('Error al registrar el edificio:', error);
          }
        );
    } else {
      console.warn('Formulario inválido', this.FormularioApartamento.errors);
    }
  }

  obtenerApartamentos() {
    this.backService.getApartamentos().subscribe(
      data => {
        this.apartamentos = data;
        console.log('Edificios obtenidos:', this.apartamentos);
      },
      error => {
        console.error('Error al obtener los edificios:', error);
      }
    );
  }

  editar(index: number) {
    const apartamento = this.apartamentos[index];
    this.isEditingRowIndex = index;
  }

  isEditingRow(index: number): boolean {
    return this.isEditingRowIndex === index;
  }

  eliminar(index: number) {
    const apartamento = this.apartamentos[index];
    console.log('Eliminar edificio con código:', apartamento.id);

    if (confirm('¿Estás seguro de que deseas eliminar este edificio?')) {
      this.backService.eliminarApartamento(apartamento.id)
        .subscribe(
          response => {
            this.apartamentos.splice(index, 1);
            console.log('Edificio eliminado:', response);
            alert(response);
          },
          error => {
            console.error('Error al eliminar el edificio:', error);
          }
        );
    }
  }

  guardarEdicion(index: number) {
    const apartamento = this.apartamentos[index];
    console.log('Código del edificio a actualizar:', apartamento.id);

    this.backService.guardarEdicion(apartamento.id, apartamento)
      .subscribe(
        response => {
          console.log('Edificio actualizado:', response);
          this.isEditingRowIndex = null;
          this.obtenerApartamentos();
        },
        error => {
          console.error('Error al actualizar el edificio:', error);
        }
      );
  }
}