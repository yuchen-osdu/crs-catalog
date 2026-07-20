FROM openjdk:8-jdk-alpine

EXPOSE 8080

COPY provider/crs-catalog-azure/crs-catalog-aks/target/crs-catalog-aks-1.0.0.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

