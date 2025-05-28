import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterOutlet } from '@angular/router';
import { AuthService } from './Servicios/auth.service';
import { isPlatformBrowser } from '@angular/common';
import { Inject, PLATFORM_ID } from '@angular/core';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'nidounit';
  nameCompany: string | null = null;
  isLoggedIn: boolean = false;

  constructor(
    private router: Router,
    public authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.authService.nameCompany$.subscribe(name => {
        this.nameCompany = name;
        console.log("Nombre de la compañía: ", this.nameCompany); 
      });

    }

  }

  logout(): void {
    this.authService.logout();
  }
}
