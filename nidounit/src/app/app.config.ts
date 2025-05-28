import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import {
  provideHttpClient,
  withFetch,
  withInterceptors
} from '@angular/common/http';
import { initializeApp, provideFirebaseApp } from '@angular/fire/app';
import { getAuth, provideAuth } from '@angular/fire/auth';
import { environment } from '../enviroments/environment';
import { authInterceptor } from './interceptors/auth.interceptor';
import { refreshTokenInterceptor } from './interceptors/refresh-token.interceptor';

let firebaseApp: any;

const getFirebaseApp = () => {
  if (!firebaseApp) {
    firebaseApp = initializeApp(environment.firebaseConfig);
  }
  return firebaseApp;
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor, refreshTokenInterceptor])
    ),
    provideRouter(routes),
    provideClientHydration(),
    provideFirebaseApp(() => getFirebaseApp()),
    provideAuth(() => getAuth())
  ]
};