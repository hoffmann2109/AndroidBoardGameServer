FROM amazoncorretto:17
WORKDIR /app
COPY WebSocketDemo-Server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 53206
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=53206"]

