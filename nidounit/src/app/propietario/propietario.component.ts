import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { BackserviceService } from '../Servicios/backservice.service';
import { AuthService } from '../Servicios/auth.service';

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
  departamentosDisponibles: any[] = [];


  constructor(private miServicio: BackserviceService, public authService: AuthService) {
    this.obtenerPropietarios();
    this.obtenerDepartamentosDisponibles();

  }

  FormularioPropietario = new FormGroup({
    nombres: new FormControl('', [
      Validators.required,
      Validators.minLength(3),
      Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)
    ]),
    apellidos: new FormControl('', [
      Validators.required,
      Validators.minLength(3),
      Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)
    ]),
    dni: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
      Validators.maxLength(8),
      Validators.pattern(/^[0-9]*$/)
    ]),
    telefono: new FormControl('', [
      Validators.required,
      Validators.minLength(9),
      Validators.maxLength(9),
      Validators.pattern(/^[0-9]*$/)
    ]),
    correo: new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    departamentoId: new FormControl<number | null>(null, [
      Validators.required
    ])
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

  obtenerDepartamentosDisponibles() {
    this.miServicio.getDepartamentosDisponibles().subscribe(
      (data) => {
        this.departamentosDisponibles = data;
        console.log('Departamentos disponibles:', this.departamentosDisponibles);
      },
      (error) => {
        console.error('Error al obtener departamentos:', error);
      }
    );
  }

  onSubmit() {
    if (this.FormularioPropietario.valid) {
      const departamentoId = Number(this.FormularioPropietario.value.departamentoId);

      console.log('[1/4] Iniciando registro de propietario...');
      console.log('Datos del formulario:', JSON.stringify(this.FormularioPropietario.value, null, 2));
      console.log('ID de departamento seleccionado:', departamentoId);

      this.miServicio.registrarPropietario({
        ...this.FormularioPropietario.value,
        departamentoId: departamentoId
      }).subscribe({
        next: (nuevoPropietario) => {
          console.log('[2/4] Propietario registrado con éxito:');
          console.log('Respuesta del registro:', JSON.stringify(nuevoPropietario, null, 2));
          console.log('ID del nuevo propietario:', nuevoPropietario.id);

          const asignacionData = {
            propietarioId: nuevoPropietario.id,
            departamentoId: departamentoId
          };

          console.log('[3/4] Iniciando asignación de propietario a departamento...');
          console.log('Datos de asignación:', JSON.stringify(asignacionData, null, 2));

          this.miServicio.asignarPropietario(nuevoPropietario.id, departamentoId)
            .subscribe({
              next: (asignacionResponse) => {
                console.log('[4/4] Asignación completada con éxito:');
                console.log('Respuesta de asignación:', JSON.stringify(asignacionResponse, null, 2));

                this.obtenerPropietarios();
                this.FormularioPropietario.reset();

                console.log('Proceso completado exitosamente ✔️');
              },
              error: (asignacionError) => {
                console.error('❌ Error en asignación:', asignacionError);
                console.error('Detalles del error:', {
                  status: asignacionError.status,
                  message: asignacionError.message,
                  error: asignacionError.error
                });

                console.warn('Intentando rollback... Eliminando propietario recién creado');
                this.miServicio.eliminarPropietario(nuevoPropietario.id).subscribe({
                  next: () => console.log('Rollback completado: Propietario eliminado'),
                  error: (rollbackError) => console.error('Error en rollback:', rollbackError)
                });
              }
            });
        },
        error: (registroError) => {
          console.error('❌ Error al registrar propietario:', registroError);
          console.error('Detalles del error:', {
            status: registroError.status,
            message: registroError.message,
            error: registroError.error
          });
        }
      });
    } else {
      console.warn('Formulario inválido. No se puede enviar.');
      console.log('Errores del formulario:', this.FormularioPropietario.errors);
      console.log('Valores actuales:', this.FormularioPropietario.value);
    }
  }


  editarPropietario(index: number) {
    const propietario = this.propietarios[index];
    this.isEditingRowIndexProp = index;

    const departamentoId = propietario.departamentos?.length > 0
      ? propietario.departamentos[0].id
      : null;

    this.FormularioPropietario.patchValue({
      nombres: propietario.nombres || '',
      apellidos: propietario.apellidos || '',
      dni: propietario.dni || '',
      telefono: propietario.telefono || '',
      correo: propietario.correo || '',
      departamentoId: departamentoId
    });

    console.log('Editando propietario:', propietario);
  }

  guardarEdicionProp(index: number) {
    const propietarioEditado = this.propietarios[index];
    const departamentoId = this.FormularioPropietario.value.departamentoId;

    const datosActualizados = {
      ...propietarioEditado,
      nombres: this.propietarios[index].nombres,
      apellidos: this.propietarios[index].apellidos,
      dni: this.propietarios[index].dni,
      telefono: this.propietarios[index].telefono,
      correo: this.propietarios[index].correo,
      departamentos: departamentoId ? [{ id: departamentoId }] : []
    };

    console.log('Datos a actualizar:', datosActualizados);

    this.miServicio.actualizarPropietario(propietarioEditado.id, datosActualizados)
      .subscribe({
        next: (response) => {
          console.log('Propietario actualizado:', response);
          this.isEditingRowIndexProp = null;
          this.obtenerPropietarios();
        },
        error: (error) => {
          console.error('Error al actualizar:', error);
          this.obtenerPropietarios();
        }
      });
  }

  isEditingRowProp(index: number): boolean {
    return this.isEditingRowIndexProp === index;
  }

  eliminarPropietario(index: number) {
    const propietario = this.propietarios[index];
    console.log('Eliminar propietario con código:', propietario.id);

    if (confirm('¿Estás seguro de que deseas eliminar este propietario?')) {
      this.miServicio.eliminarPropietario(propietario.id)
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

  getDepartamentoInfo(propietario: any): string {
    if (propietario.departamentos && propietario.departamentos.length > 0) {
      const departamento = propietario.departamentos[0];
      return `Dpto ${departamento.numero}, Piso ${departamento.piso} (${departamento.edificio?.nombre || 'Sin edificio'})`;
    }
    return 'Sin departamento asignado';
  }
}