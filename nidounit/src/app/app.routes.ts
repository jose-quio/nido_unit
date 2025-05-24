import { Routes } from '@angular/router';
import { ApartamentoComponent } from './edificio/apartamento.component';
import { CajaComponent } from './caja/caja.component';
import { PagoComponent } from './pago/pago.component';
import { PropietarioComponent } from './propietario/propietario.component';
import { ApartamentoHabComponent } from './departamento/apartamento-hab.component';
import { LoginComponent } from './login/login.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: '', 
    canActivate: [authGuard], 
    children: [
      { path: '', redirectTo: '/apartamento', pathMatch: 'full' },
      { path: 'apartamento', component: ApartamentoComponent },
      { path: 'apartamentoHab', component: ApartamentoHabComponent },
      { path: 'propietario', component: PropietarioComponent },
      { path: 'pago', component: PagoComponent },
      { path: 'caja', component: CajaComponent },
    ]
  },
  { path: '**', redirectTo: '/login' } 
];