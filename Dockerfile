FROM library/openjdk:8-jre-alpine

WORKDIR /opt/docker

COPY package*.json ./

RUN apk update && apk add bash curl npm && npm install && cp -rf node_modules/* /usr/lib/node_modules/
