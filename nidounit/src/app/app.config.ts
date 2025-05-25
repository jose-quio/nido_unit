import { ApplicationConfig, importProvidersFrom, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { initializeApp, provideFirebaseApp } from '@angular/fire/app';
import { getAuth, provideAuth } from '@angular/fire/auth';
import { environment } from '../enviroments/environment';

let firebaseApp: any;

const getFirebaseApp = () => {
  if (!firebaseApp) {
    firebaseApp = initializeApp(environment.firebaseConfig);
  }
  return firebaseApp;
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withFetch()),
    provideRouter(routes),
    provideClientHydration(),
    provideFirebaseApp(() => getFirebaseApp()),
    provideAuth(() => getAuth())
  ]
};