import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { response } from 'express';

@Component({
  selector: 'app-apartamento',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './apartamento.component.html',
  styleUrl: './apartamento.component.scss'
})
export class ApartamentoComponent {

  constructor(private http: HttpClient) {
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
      this.http.post<any>('http://localhost:3000/api/apartamentos', this.FormularioApartamento.value)
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
    this.http.get<any[]>('http://localhost:3000/api/apartamentos').subscribe(
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
      this.http.delete(`http://localhost:3000/api/apartamentos/${apartamento.CodigoApartamento}`, { responseType: 'text' })
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
  
    this.http.put('http://localhost:3000/api/apartamentos/' + apartamento.CodigoApartamento, apartamento, { responseType: 'text' })
      .subscribe(response => {
        console.log('Apartamento actualizado:', response);
        this.isEditingRowIndex = null;
        this.obtenerApartamentos();  
      }, error => {
        console.error('Error al actualizar el apartamento:', error);
      });
  }
} 
