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
  token: string | null = null;

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
    this.loadAuthData();
  }

  private loadAuthData(): void {
    this.userId = this.authService.getUserId();
    this.token = this.authService.getToken();
    
    if (!this.userId || !this.token) {
      this.errorMessage = 'Error: No se pudo obtener la información de autenticación';
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
      if (!this.token || !this.userId) {
        throw new Error('Datos de autenticación incompletos');
      }

      const companyResponse = await this.backService.registrarCompania(
        this.companyData,
        this.token
      ).toPromise();

      if (!companyResponse?.id) {
        throw new Error('No se recibió un ID válido de la compañía');
      }

      const associationResponse = await this.backService.asociarUsuarioCompania(
        this.userId,
        companyResponse.id,
        this.token
      ).toPromise();

      this.authService.updateCompanyInfo(companyResponse.id);

      this.successMessage = 'Compañía registrada y asociada exitosamente. Redirigiendo...';
      
      setTimeout(() => {
        this.router.navigate(['/dashboard']); 
      }, 2000);

    } catch (error: any) {
      console.error('Error en el proceso de registro:', error);
      this.errorMessage = this.handleError(error);
      
      if (error.status === 401) {
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      }
    } finally {
      this.isLoading = false;
    }
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
    if (error.status === 401) {
      return 'Sesión expirada. Será redirigido para iniciar sesión nuevamente.';
    }

    if (error.error) {
      if (error.error.message) {
        return error.error.message;
      }
      if (error.error.error) {
        return error.error.error;
      }
    }

    if (error.message) {
      return error.message;
    }

    return 'Error al procesar la solicitud. Por favor intente nuevamente.';
  }
}