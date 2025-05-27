import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterOutlet } from '@angular/router';
import { AuthService } from './Servicios/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'nidounit';

  constructor(private router: Router, public authService: AuthService) { }

  get localStorage(): Storage | null {
    return typeof window !== 'undefined' ? window.localStorage : null;
  }

  logout(): void {
    this.localStorage?.removeItem('token');
    this.localStorage?.removeItem('user');
    this.localStorage?.removeItem('isLoggedIn')
    this.localStorage?.removeItem('currentUser')
    this.router.navigate(['/login']);
  }
}