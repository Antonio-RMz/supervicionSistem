# Etapa 1: Construcción
FROM node:22-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Etapa 2: Nginx
FROM nginx:alpine
# Nota: Dependiendo de tu versión de Angular, la ruta de salida puede variar. 
# Si te da error, cambia esto por /app/dist/gestor-proyectos-front/browser
COPY --from=build /app/dist/gestorproyectos-we/browser /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]