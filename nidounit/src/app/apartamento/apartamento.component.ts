import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { response } from 'express';
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

  constructor(private http: HttpClient,private backService: BackserviceService) {
    this.obtenerApartamentos();
  }
  apartamentos: any[] = [];
  isEditing: boolean = false;
  isEditingRowIndex: number | null = null;

  FormularioApartamento = new FormGroup({
    Ubicacion: new FormControl(''),
    NumeroPisos: new FormControl(''),
    Alto: new FormControl(''),
    Largo: new FormControl(''),
    Ancho: new FormControl('')
  })

  onSubmit() {
    if (this.FormularioApartamento.valid) {
      this.backService.registrarApartamento(this.FormularioApartamento.value)
        .subscribe(
          response => {
            console.log('Apartamento registrado con éxito:', response);
            this.obtenerApartamentos();
          },
          error => {
            console.error('Error al registrar el apartamento:', error);
          }
        );
    } else {
      console.warn('Formulario inválido');
    }
  }
  obtenerApartamentos() {
    this.backService.getApartamentos().subscribe(
      data => {
        this.apartamentos = data;
        console.log('Apartamentos obtenidos:', this.apartamentos);
      },
      error => {
        console.error('Error al obtener los apartamentos:', error);
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
    console.log('Eliminar apartamento con código:', apartamento.CodigoApartamento);  
    
    if (confirm('¿Estás seguro de que deseas eliminar este apartamento?')) {
      this.backService.eliminarApartamento(apartamento.CodigoApartamento)
        .subscribe(
          response => {
            this.apartamentos.splice(index, 1); 
            console.log('Apartamento eliminado:', response);
            alert(response); 
          },
          error => {
            console.error('Error al eliminar el apartamento:', error);
          }
        );
    }
  }
  
  
  guardarEdicion(index: number) {
    const apartamento = this.apartamentos[index];
    console.log('Código del apartamento a actualizar:', apartamento.CodigoApartamento);

    this.backService.guardarEdicion(apartamento.CodigoApartamento, apartamento)
      .subscribe(
        response => {
          console.log('Apartamento actualizado:', response);
          this.isEditingRowIndex = null;  
          this.obtenerApartamentos();     
        },
        error => {
          console.error('Error al actualizar el apartamento:', error);
        }
      );
  }
} 
