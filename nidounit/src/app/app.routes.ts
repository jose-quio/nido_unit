import { Routes } from '@angular/router';
import path from 'node:path';
import { ApartamentoComponent } from './apartamento/apartamento.component';
import { CajaComponent } from './caja/caja.component';
import { PagoComponent } from './pago/pago.component';
import { PropietarioComponent } from './propietario/propietario.component';
import { ApartamentoHabComponent } from './apartamento-hab/apartamento-hab.component';


export const routes: Routes = [
    {path: 'apartamento', component: ApartamentoComponent},
    {path: 'apartamentoHab', component: ApartamentoHabComponent},
    {path: 'propietario', component: PropietarioComponent},
    {path: 'pago', component: PagoComponent},
    {path: 'caja', component: CajaComponent}
];
