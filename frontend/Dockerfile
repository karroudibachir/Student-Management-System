FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/nginx.conf.template
COPY 15-nginx-config.sh /docker-entrypoint.d/15-nginx-config.sh
RUN chmod +x /docker-entrypoint.d/15-nginx-config.sh
COPY --from=build /app/dist/sms/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s CMD wget -q -O /dev/null http://127.0.0.1/healthz || exit 1
