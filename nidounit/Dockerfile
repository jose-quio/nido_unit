# Etapa 1: Build Angular app
FROM node:20-alpine as builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --configuration=production

# Etapa 2: Servir con nginx
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder src/app/dist/nidounit /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]








