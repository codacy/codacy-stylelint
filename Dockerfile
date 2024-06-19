FROM node:lts-alpine3.20

WORKDIR /workdir
COPY package*.json ./
COPY --chmod=0755 target/universal/stage/bin/codacy-stylelint ./bin/codacy-stylelint
COPY docs /docs

RUN adduser -u 2004 -D docker
RUN apk --no-cache add openjdk11-jre-headless bash
RUN npm install

USER docker

WORKDIR /src
ENTRYPOINT ["/workdir/bin/codacy-stylelint"]
