# syntax=docker/dockerfile:1
# Build context: the repository root (the web needs ../src/main/resources/static/openapi.yaml
# to generate its TypeScript types). See compose.demo.yaml.

# ---------------------------------------------------------------- 1. Build the web
FROM node:24-alpine AS build
WORKDIR /workspace/frontend

# Dependencies first: this layer is only rebuilt when package-lock.json changes
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY src/main/resources/static/openapi.yaml /workspace/src/main/resources/static/openapi.yaml
COPY frontend/ ./
RUN npm run build

# ---------------------------------------------------------------- 2. Serve it with nginx
FROM nginx:stable-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/frontend/dist /usr/share/nginx/html
