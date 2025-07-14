import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { environment } from '../../environments/environment.staging';
import { BackserviceService } from '../Servicios/backservice.service';
import { AuthService } from '../Servicios/auth.service';


@Component({
  selector: 'app-apartamento',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './apartamento.component.html',
  styleUrl: './apartamento.component.scss'
})
export class ApartamentoComponent {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient, private backService: BackserviceService, public authService: AuthService) {
    this.obtenerApartamentos();
  }
  apartamentos: any[] = [];
  isEditing: boolean = false;
  isEditingRowIndex: number | null = null;

  FormularioApartamento = new FormGroup({
    nombre: new FormControl<string>('', [Validators.required]),  
    direccion: new FormControl<string>('', [Validators.required]),
    nroPisos: new FormControl<number | null>(null, [Validators.required]),  
    tipo: new FormControl<string>('residencial', [Validators.required]),
    descripcion: new FormControl<string>('')
});

  onSubmit() {
    if (this.FormularioApartamento.valid) {
      const idCompany = localStorage.getItem('idCompany');
      
      if (!idCompany) {
        console.error('No se encontró idCompany en el localStorage');
        return;
      }

      const buildingData = {
        ...this.FormularioApartamento.value,
        company: { id: Number(idCompany) }
      };

      console.log('Enviando datos:', buildingData);
      
      this.backService.registrarApartamento(buildingData)
        .subscribe({
          next: (response) => {
            console.log('Edificio registrado con éxito:', response);
            this.obtenerApartamentos();
            this.FormularioApartamento.reset({ tipo: 'residencial' });
          },
          error: (error) => {
            console.error('Error al registrar:', error);
          }
        });
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
            alert("Edificio eliminado con éxito");
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