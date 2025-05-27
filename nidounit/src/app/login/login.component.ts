import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../Servicios/auth.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  isLoginMode = true;
  isLoading = false;
  
  loginData = {
    email: '',
    password: ''
  };
  
  registerData = {
    email: '',
    password: '',
    confirmPassword: '',
    displayName: ''
  };
  
  returnUrl: string;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  ngOnInit() {
    
    if (this.authService.isAuthenticated()) {
      this.router.navigateByUrl(this.returnUrl);
    }
  }

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.clearMessages();
    this.resetForms();
  }

  async onLogin(): Promise<void> {
    if (!this.validateLoginForm()) return;

    this.isLoading = true;
    this.clearMessages();

    try {
      await this.authService.loginWithEmailPassword(
        this.loginData.email, 
        this.loginData.password
      );
      
      this.successMessage = 'Login exitoso';
      setTimeout(() => {
        this.router.navigateByUrl(this.returnUrl);
      }, 1000);
      
    } catch (error: any) {
      this.errorMessage = error;
    } finally {
      this.isLoading = false;
    }
  }

  async onRegister(): Promise<void> {
    if (!this.validateRegisterForm()) return;

    this.isLoading = true;
    this.clearMessages();

    try {
      const result = await this.authService.registerWithEmailPassword(
        this.registerData.email,
        this.registerData.password,
        this.registerData.displayName
      );
      
      if (result.success && result.userId) {
        this.successMessage = 'Registro exitoso. Redirigiendo al registro de compañía...';
        setTimeout(() => {
          this.router.navigate(['/company-register'], { 
            queryParams: { userId: result.userId } 
          });
        }, 1500);
      }
      
    } catch (error: any) {
      this.errorMessage = error;
    } finally {
      this.isLoading = false;
    }
  }

  async onGoogleLogin(): Promise<void> {
    this.isLoading = true;
    this.clearMessages();

    try {
      const result = await this.authService.loginWithGoogle();
      
      this.successMessage = 'Login con Google exitoso';
      
      if (result.isNewUser && result.userId) {
        setTimeout(() => {
          this.router.navigate(['/company-register'], { 
            queryParams: { userId: result.userId } 
          });
        }, 1500);
      } else {
        setTimeout(() => {
          this.router.navigateByUrl(this.returnUrl);
        }, 1000);
      }
      
    } catch (error: any) {
      this.errorMessage = error;
    } finally {
      this.isLoading = false;
    }
  }

  private validateLoginForm(): boolean {
    if (!this.loginData.email || !this.loginData.password) {
      this.errorMessage = 'Por favor complete todos los campos';
      return false;
    }

    if (!this.isValidEmail(this.loginData.email)) {
      this.errorMessage = 'Por favor ingrese un email válido';
      return false;
    }

    return true;
  }

  private validateRegisterForm(): boolean {
    if (!this.registerData.email || !this.registerData.password || 
        !this.registerData.confirmPassword || !this.registerData.displayName) {
      this.errorMessage = 'Por favor complete todos los campos';
      return false;
    }

    if (!this.isValidEmail(this.registerData.email)) {
      this.errorMessage = 'Por favor ingrese un email válido';
      return false;
    }

    if (this.registerData.password.length < 6) {
      this.errorMessage = 'La contraseña debe tener al menos 6 caracteres';
      return false;
    }

    if (this.registerData.password !== this.registerData.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden';
      return false;
    }

    return true;
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private resetForms(): void {
    this.loginData = { email: '', password: '' };
    this.registerData = { email: '', password: '', confirmPassword: '', displayName: '' };
  }
}