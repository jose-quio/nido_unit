import { Component } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { AuthService } from '../Servicios/auth.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username = '';
  password = '';
  returnUrl: string;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  login(): void {
    if (this.authService.login(this.username, this.password)) {
      this.router.navigateByUrl(this.returnUrl);
    } else {
      alert('Credenciales incorrectas');
    }
  }

  ngOnInit() {
    console.log('LoginComponent ngOnInit ejecutado'); 
  }
}
