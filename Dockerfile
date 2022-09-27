#
# This dockerfile expects a compiled artifact in the target folder.
# Call "mvn clean package" first!
#

FROM openjdk:17-jdk-slim

LABEL org.opencontainers.image.title="Rabbarkable"
LABEL org.opencontainers.image.description="RabbitMQ (AMQP) connector for the reMarkable API"
LABEL org.opencontainers.image.authors="Stefan Haun <mail@tuxathome.de>"
LABEL org.opencontainers.image.source="https://github.com/penguineer/Rabbarkable"
LABEL org.opencontainers.image.licenses="MIT"

RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080
HEALTHCHECK --interval=5s CMD curl --fail http://localhost:8080/health || exit 1

COPY target/rabbarkable-*.jar /usr/local/lib/rabbarkable.jar

ENTRYPOINT ["java","-jar","/usr/local/lib/rabbarkable.jar"]
