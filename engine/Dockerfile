FROM openjdk:17-alpine3.13
COPY target/dependency-jars /run/dependency-jars
ADD target/green-cloud-engine.jar /run/application.jar

ENTRYPOINT java -jar run/application.jar -D exec.mainClass="runner.EngineRunner"
