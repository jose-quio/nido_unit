import { Routes } from '@angular/router';
import path from 'node:path';
import { ApartamentoComponent } from './apartamento/apartamento.component';
import { CajaComponent } from './caja/caja.component';
import { PagoComponent } from './pago/pago.component';
import { PropietarioComponent } from './propietario/propietario.component';
import { ApartamentoHabComponent } from './apartamento-hab/apartamento-hab.component';
import { AppComponent } from './app.component';


export const routes: Routes = [
    {path: '', redirectTo: '/apartamento', pathMatch: 'full' },
    {path: 'apartamento', component: ApartamentoComponent},
    {path: 'apartamentoHab', component: ApartamentoHabComponent},
    {path: 'propietario', component: PropietarioComponent},
    {path: 'pago', component: PagoComponent},
    {path: 'caja', component: CajaComponent},
];
