import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../Servicios/auth.service';
import { BackserviceService } from '../Servicios/backservice.service';

interface Colaborador {
  id: number;
  username: string;
  nombre: string;
  email: string;
  roles: {
    id: number;
    nombre: string;
  }[];
  activo: boolean;
  fechaRegistro: string;
  companyId: number;
}

interface Role {
  id: number;
  name: string;
  description: string;
}

@Component({
  selector: 'app-colaboradores',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './colaboradores.component.html',
  styleUrls: ['./colaboradores.component.scss']
})
export class ColaboradoresComponent implements OnInit {
  colaboradorForm: FormGroup;
  colaboradores: Colaborador[] = [];
  availableRoles: Role[] = [
    {
      id: 1,
      name: 'ADMIN_COMPANY',
      description: 'Acceso completo al sistema - Puede gestionar edificios, departamentos, propietarios, contratos, pagos y caja'
    },
    {
      id: 2,
      name: 'MANAGER_EDIFICIO',
      description: 'Gestión de edificios - Puede ver propietarios, contratos y pagos'
    }
  ];

  selectedRoleIds: number[] = [];
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    public authService: AuthService,
    private backService: BackserviceService
  ) {
    this.colaboradorForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      nombre: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      roleIds: [[]]
    });
  }
  
  ngOnInit(): void {
    this.cargarColaboradores();
  }

  onRoleChange(event: any, roleId: number): void {
    if (event.target.checked) {
      this.selectedRoleIds.push(roleId);
    } else {
      this.selectedRoleIds = this.selectedRoleIds.filter(id => id !== roleId);
    }
  }

  onSubmit(): void {
    if (this.colaboradorForm.valid && this.selectedRoleIds.length > 0) {
      this.isSubmitting = true;
      this.clearMessages();

      const currentUser = this.authService.getCurrentUser();
      const companyId = currentUser?.idCompany;

      if (!companyId) {
        this.errorMessage = 'No se pudo obtener el ID de la empresa';
        this.isSubmitting = false;
        return;
      }

      const colaboradorData = {
        username: this.colaboradorForm.value.username,
        password: this.colaboradorForm.value.password,
        nombre: this.colaboradorForm.value.nombre,
        email: this.colaboradorForm.value.email,
        companyId: companyId,
        roleIds: this.selectedRoleIds
      };

      this.backService.guardarColaborador(colaboradorData).subscribe({
        next: (response) => {
          this.successMessage = 'Colaborador registrado exitosamente';
          this.resetForm();
          this.cargarColaboradores();
          this.isSubmitting = false;
        },
        error: (error) => {
          this.errorMessage = this.getErrorMessage(error);
          this.isSubmitting = false;
        }
      });
    } else {
      this.errorMessage = 'Por favor complete todos los campos y seleccione al menos un rol';
    }
  }

  private resetForm(): void {
    this.colaboradorForm.reset();
    this.selectedRoleIds = [];
    // Desmarcar todos los checkboxes
    const checkboxes = document.querySelectorAll('input[type="checkbox"]');
    checkboxes.forEach((checkbox: any) => {
      checkbox.checked = false;
    });
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  private getErrorMessage(error: any): string {
    if (error.error && error.error.message) {
      return error.error.message;
    }
    if (error.status === 409) {
      return 'El usuario o email ya existe';
    }
    if (error.status === 400) {
      return 'Datos inválidos. Verifique la información ingresada';
    }
    return 'Error al registrar el colaborador. Intente nuevamente';
  }

  cargarColaboradores(): void {
    this.backService.obtenerColaboradores().subscribe({
      next: (response) => {
        this.colaboradores = response;
      },
      error: (error) => {
        console.error('Error al cargar colaboradores:', error);
      }
    });
  }

  editarColaborador(colaborador: Colaborador): void {
    // Implementar lógica de edición
    console.log('Editando colaborador:', colaborador);
  }

  eliminarColaborador(id: number): void {
    if (confirm('¿Está seguro de que desea eliminar este colaborador?')) {
      this.backService.eliminarColaborador(id).subscribe({
        next: (response) => {
          this.successMessage = 'Colaborador eliminado exitosamente';
          this.cargarColaboradores();
        },
        error: (error) => {
          this.errorMessage = 'Error al eliminar el colaborador';
        }
      });
    }
  }

  getRoleClass(role: string): string {
    switch (role) {
      case 'ADMIN_COMPANY':
        return 'role-admin';
      case 'MANAGER_EDIFICIO':
        return 'role-manager';
      case 'USUARIO_BASICO':
        return 'role-user';
      default:
        return 'role-user';
    }
  }
}