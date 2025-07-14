import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';
import { AuthService } from '../Servicios/auth.service';

@Component({
  selector: 'app-apartamento-hab',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './apartamento-hab.component.html',
  styleUrl: './apartamento-hab.component.scss'
})
export class ApartamentoHabComponent {
  constructor(private miServicio: BackserviceService, public authService: AuthService) {
    this.obtenerApartamentos();
    this.obtenerApartamentosHab();
  }

  apartamentosHab: any[] = [];
  isEditingHab: boolean = false;
  isEditingRowIndexHab: number | null = null;
  apartamentos: any[] = [];
  errorMessage: string | null = null;

  FormularioApartamentoHab = new FormGroup({
    edificioId: new FormControl<number | null>(null, [Validators.required]),
    numero: new FormControl<string>('', [Validators.required]),
    piso: new FormControl<string>('', [Validators.required, Validators.min(0)]),
    nroHabitaciones: new FormControl<string>('', [Validators.required, Validators.min(1)]),
    area: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    precioVenta: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    precioAlquiler: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    disponible: new FormControl<boolean>(true)
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
    this.miServicio.getEdificioSimple().subscribe(
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
    this.errorMessage = null; 

    if (this.FormularioApartamentoHab.valid) {
      const formData = {
        numero: this.FormularioApartamentoHab.value.numero,
        piso: String(this.FormularioApartamentoHab.value.piso),
        nroHabitaciones: String(this.FormularioApartamentoHab.value.nroHabitaciones),
        area: Number(this.FormularioApartamentoHab.value.area),
        precioVenta: Number(this.FormularioApartamentoHab.value.precioVenta),
        precioAlquiler: Number(this.FormularioApartamentoHab.value.precioAlquiler),
        disponible: Boolean(this.FormularioApartamentoHab.value.disponible),
        edificio: {
          id: Number(this.FormularioApartamentoHab.value.edificioId)
        }
      };

      console.log('Datos a enviar:', formData);

      this.miServicio.registrarApartamentoHab(formData)
        .subscribe(
          response => {
            console.log('Departamento registrado con éxito:', response);
            this.obtenerApartamentosHab();
            this.FormularioApartamentoHab.reset({
              disponible: true
            });
          },
          error => {
            if (error.status === 409) {
              this.errorMessage = 'Ya existe un departamento con este número en el edificio seleccionado';
            } else {
              console.error('Error al registrar el departamento:', error);
              this.errorMessage = 'Ocurrió un error al registrar el departamento';
            }
          }
        );
    } else {
      console.warn('Formulario inválido', this.FormularioApartamentoHab.errors);
      this.errorMessage = 'Por favor complete todos los campos requeridos correctamente';
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
    console.log('Actualizando departamento con código:', apartamentoHab.id);

    this.miServicio.actualizarApartamentoHab(apartamentoHab.id, apartamentoHab)
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
    console.log('Eliminar departamento con código:', apartamentoHab.id);

    if (confirm('¿Estás seguro de que deseas eliminar este departamento?')) {
      this.miServicio.eliminarApartamentoHab(apartamentoHab.id)
        .subscribe(
          response => {
            this.apartamentosHab.splice(index, 1);
            console.log('Departamento eliminado:', response);
            alert("Departamento eliminado con éxito");
          },
          error => {
            console.error('Error al eliminar el departamento:', error);
          }
        );
    }
  }


}