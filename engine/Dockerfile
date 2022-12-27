FROM openjdk:17-alpine3.13
COPY target/dependency-jars /run/dependency-jars
ADD target/green-cloud-engine.jar /run/application.jar

WORKDIR /run
ENTRYPOINT java -cp application.jar $MAIN_CLASS $GC_PARAMS $GC_HOST_PARAMS
