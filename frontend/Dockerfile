FROM node:24-alpine AS build
WORKDIR /workspace
ARG VITE_API_BASE_URL=/api
ARG VITE_WS_BASE_URL=/ws
ARG VITE_STICKER_BASE_URL=
ARG NPM_REGISTRY=https://registry.npmmirror.com
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_WS_BASE_URL=$VITE_WS_BASE_URL
ENV VITE_STICKER_BASE_URL=$VITE_STICKER_BASE_URL
COPY package*.json .npmrc ./
RUN --mount=type=cache,target=/root/.npm npm ci --registry=$NPM_REGISTRY
COPY . .
RUN npm run build

FROM node:24-alpine
WORKDIR /app
ARG NPM_REGISTRY=https://registry.npmmirror.com
RUN --mount=type=cache,target=/root/.npm npm install -g serve --registry=$NPM_REGISTRY
COPY --from=build /workspace/dist ./dist
EXPOSE 5173
CMD ["serve", "-s", "dist", "-l", "5173"]
