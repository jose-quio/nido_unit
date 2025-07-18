FROM node:20-alpine as build

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .

RUN npm run build

FROM node:20-alpine

WORKDIR /app

COPY --from=build /app/dist/nidounit /app/dist/nidounit
COPY --from=build /app/node_modules /app/node_modules

EXPOSE 4000

CMD ["node", "dist/nidounit/server/server.mjs"]
