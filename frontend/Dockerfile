FROM node:24-alpine AS build
WORKDIR /workspace
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM node:24-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=build /workspace/dist ./dist
EXPOSE 5173
CMD ["serve", "-s", "dist", "-l", "5173"]
