import { Routes } from '@angular/router';
import { ApartamentoComponent } from './edificio/apartamento.component';
import { CajaComponent } from './caja/caja.component';
import { PagoComponent } from './pago/pago.component';
import { PropietarioComponent } from './propietario/propietario.component';
import { ApartamentoHabComponent } from './departamento/apartamento-hab.component';
import { LoginComponent } from './login/login.component';
import { authGuard } from './auth.guard';
import { CompanyRegisterComponent } from './companyregister/companyregister.component';
import { ContratosComponent } from './contratos/contratos.component';
import { ColaboradoresComponent } from './colaboradores/colaboradores.component';
import { ProhibidoComponent } from './prohibido/prohibido.component';
import { GastosComponent } from './gastos/gastos.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'companyregister', component: CompanyRegisterComponent, canActivate: [authGuard] },
  { path: 'forbidden', component: ProhibidoComponent },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: '/apartamento',
        pathMatch: 'full'
      },
      {
        path: 'apartamento',
        component: ApartamentoComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMIN_COMPANY', 'MANAGER_EDIFICIO'] }
      },
      {
        path: 'apartamentoHab',
        component: ApartamentoHabComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMIN_COMPANY', 'MANAGER_EDIFICIO'] }
      },
      {
        path: 'propietario',
        component: PropietarioComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMIN_COMPANY', 'MANAGER_EDIFICIO'] }
      },
      {
        path: 'pago',
        component: PagoComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMIN_COMPANY', 'MANAGER_EDIFICIO'] }
      },
      {
        path: 'caja',
        component: CajaComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMIN_COMPANY', 'MANAGER_EDIFICIO'] }
      },
      {
        path: 'contrato',
        component: ContratosComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMIN_COMPANY', 'MANAGER_EDIFICIO'] }
      },
      {
        path: 'colaboradores',
        component: ColaboradoresComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMIN_COMPANY'] }
      },
      {
        path: 'gastos',
        component: GastosComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMIN_COMPANY'] }
      }
    ]
  },
  { path: '**', redirectTo: '/login' }
];