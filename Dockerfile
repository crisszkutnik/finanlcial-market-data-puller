FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY target/scala-3.3.4/application.jar /app

EXPOSE 8080

CMD ["java", "-jar", "application.jar"]