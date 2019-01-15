FROM hseeberger/scala-sbt:11.0.1_2.12.8_1.2.7 AS builder
RUN mkdir /data

ADD project /data/project
COPY build.sbt /data
COPY src /data/src

WORKDIR /data
RUN (sbt compile || true) && sbt assembly

FROM openjdk:8-alpine
LABEL maintainer="andre.missaglia@arquivei.com.br"

RUN apk add --update libc6-compat

COPY --from=builder /data/target/scala-2.12/kafka-partitioner-assembly-0.1.jar /app.jar

COPY entrypoint.sh /
ENTRYPOINT [ "/entrypoint.sh" ]