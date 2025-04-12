import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ApartamentoComponent } from './apartamento/apartamento.component';
import { ApartamentoHabComponent } from './apartamento-hab/apartamento-hab.component';
import { CajaComponent } from './caja/caja.component';
import { PagoComponent } from './pago/pago.component';
import { PropietarioComponent } from './propietario/propietario.component';

@NgModule({
  declarations: [
    AppComponent,
    ApartamentoComponent,
    ApartamentoHabComponent,
    CajaComponent,
    PagoComponent,
    PropietarioComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
