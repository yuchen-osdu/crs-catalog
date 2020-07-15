FROM openjdk:8-jdk-alpine

ARG ENTITLEMENT_URL

ENV ENTITLEMENT_URL=$ENTITLEMENT_URL

EXPOSE 8080

COPY /target/crs-catalog-gke-*.jar root.jar

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar root.jar