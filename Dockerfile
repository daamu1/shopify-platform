FROM amazoncorretto:17-alpine

ARG SERVICE

WORKDIR /app

COPY ${SERVICE}/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
