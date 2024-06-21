FROM node:lts-alpine3.20

WORKDIR /workdir

COPY package*.json ./

RUN npm install

COPY docs /docs
COPY target/universal/stage/ ./

RUN adduser -u 2004 -D docker
RUN chmod +x /workdir/bin/codacy-stylelint
RUN apk --no-cache add openjdk11-jre-headless bash

USER docker

WORKDIR /src
ENTRYPOINT ["/workdir/bin/codacy-stylelint"]
