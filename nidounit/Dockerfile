# Etapa 1: Construcción del proyecto Angular
FROM node:18-alpine as build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar dependencias e instalar
COPY package.json package-lock.json ./
RUN npm install

# Copiar todo el proyecto
COPY . .

# Compilar la app Angular en modo producción
RUN npm run build -- --configuration production

# Etapa 2: Servir la app con NGINX
FROM nginx:alpine

# Elimina la página de ejemplo
RUN rm -rf /usr/share/nginx/html/*

# Copia archivos construidos desde la etapa anterior
COPY --from=build /app/dist/nidounit /usr/share/nginx/html

# (Opcional) configuración personalizada de NGINX para rutas Angular
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Puerto por defecto
EXPOSE 80

# Comando para mantener NGINX activo
CMD ["nginx", "-g", "daemon off;"]
