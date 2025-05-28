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
    username: '',
    nombre: '',
    email: '',
    password: '',
    confirmPassword: ''
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
      const response = await this.authService.loginWithEmailPassword(
        this.loginData.email,
        this.loginData.password
      );

      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify({
        userId: response.userId,
        username: response.username,
        roles: response.roles
      }));

      this.successMessage = 'Login exitoso';
      setTimeout(() => this.router.navigateByUrl(this.returnUrl), 1000);

    } catch (error: any) {
      this.errorMessage = this.getUserFriendlyError(error);
    } finally {
      this.isLoading = false;
    }
  }

  private getUserFriendlyError(error: any): string {
    if (error?.error?.message) {
      return error.error.message;
    }
    if (error.message.includes('404')) {
      return 'Usuario o contraseña incorrectos';
    }
    if (error.message.includes('Network Error')) {
      return 'Error de conexión. Verifica tu internet';
    }
    return 'Error al iniciar sesión. Intenta nuevamente';
  }
  async onRegister(): Promise<void> {
    if (!this.validateRegisterForm()) return;

    this.isLoading = true;
    this.clearMessages();

    try {
      const result = await this.authService.registerWithEmailPassword(
        this.registerData.username,
        this.registerData.password,
        this.registerData.nombre,
        this.registerData.email
      );

      if (result.success) {
        this.successMessage = 'Registro exitoso. Redirigiendo al registro de compañía...';

        await this.router.navigate(['/companyregister'], {
          queryParams: { userId: result.userId }
        });
      }
    } catch (error: any) {
      this.errorMessage = this.getUserFriendlyError(error);
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
        await this.router.navigate(['/companyregister'], {
          queryParams: { userId: result.userId }
        });
      } else {
        await this.router.navigateByUrl(this.returnUrl);
      }

    } catch (error: any) {
      this.errorMessage = this.getUserFriendlyError(error);
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
      !this.registerData.confirmPassword || !this.registerData.nombre ||
      !this.registerData.username) {
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
    this.registerData = { username: '', email: '', password: '', confirmPassword: '', nombre: '' };
  }
}