FROM openjdk:8-jdk-alpine

ARG ENTITLEMENT_URL
ARG client-id

ENV ENTITLEMENT_URL=$ENTITLEMENT_URL
ENV client-id=$client-id

EXPOSE 8080

COPY /target/crs-catalog-aks-1.0.0.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

