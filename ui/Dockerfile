FROM node:12.7-alpine AS build

RUN mkdir /app
WORKDIR /app
COPY package.json /app
RUN npm install
COPY ./ /app
RUN npm run build-prod

FROM nginx:1.17.1-alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/dist/ui /usr/share/nginx/html

EXPOSE 80
