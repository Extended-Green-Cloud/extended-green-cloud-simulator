FROM node:18-bullseye

WORKDIR /app
ENV PATH /app/node_modules/.bin:$PATH

COPY package*.json .

RUN yarn install
COPY . .
CMD ["npm", "start"]
