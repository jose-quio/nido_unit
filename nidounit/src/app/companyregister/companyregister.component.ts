import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BackserviceService } from '../Servicios/backservice.service';
import { AuthService } from '../Servicios/auth.service';

@Component({
  selector: 'app-company-register',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './companyregister.component.html',
  styleUrl: './companyregister.component.scss'
})
export class CompanyRegisterComponent implements OnInit {
  isLoading = false;
  errorMessage: string = '';
  successMessage: string = '';
  userId: number | null = null;

  companyData = {
    nombre: '',
    direccion: '',
    telefono: '',
    ruc: ''
  };

  constructor(
    private backService: BackserviceService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Obtener el userId del usuario autenticado
    this.userId = this.authService.getUserId();
    
    if (!this.userId) {
      this.errorMessage = 'Error: No se pudo obtener la información del usuario';
      // Redirigir al login si no hay usuario
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 2000);
    }
  }

  async onRegisterCompany(): Promise<void> {
    if (!this.validateForm()) return;

    this.isLoading = true;
    this.clearMessages();

    try {
      // 1. Registrar la compañía
      const companyResponse = await this.backService.registrarCompania(this.companyData).toPromise();
      const companyId = companyResponse.id;

      // 2. Asociar el usuario con la compañía
      if (this.userId && companyId) {
        await this.backService.asociarUsuarioCompania(this.userId, companyId).toPromise();
      }

      this.successMessage = 'Compañía registrada exitosamente. Redirigiendo...';
      
      setTimeout(() => {
        this.router.navigate(['/dashboard']); // O la ruta que quieras después del registro
      }, 2000);

    } catch (error: any) {
      console.error('Error al registrar compañía:', error);
      this.errorMessage = this.handleError(error);
    } finally {
      this.isLoading = false;
    }
  }

  onSkipCompanyRegistration(): void {
    // Permitir que el usuario omita el registro de compañía por ahora
    this.router.navigate(['/dashboard']);
  }

  private validateForm(): boolean {
    if (!this.companyData.nombre || !this.companyData.direccion || 
        !this.companyData.telefono || !this.companyData.ruc) {
      this.errorMessage = 'Por favor complete todos los campos';
      return false;
    }

    if (this.companyData.ruc.length !== 11) {
      this.errorMessage = 'El RUC debe tener 11 dígitos';
      return false;
    }

    if (!/^\d+$/.test(this.companyData.ruc)) {
      this.errorMessage = 'El RUC debe contener solo números';
      return false;
    }

    if (!/^\d{9,}$/.test(this.companyData.telefono)) {
      this.errorMessage = 'El teléfono debe tener al menos 9 dígitos';
      return false;
    }

    return true;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private handleError(error: any): string {
    if (error.error && error.error.message) {
      return error.error.message;
    }
    
    if (error.message) {
      return error.message;
    }

    return 'Error al registrar la compañía. Por favor intente nuevamente.';
  }
}