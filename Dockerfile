FROM eclipse-temurin:21-jdk
WORKDIR /app

RUN apt-get update && apt-get install -y \
    libx11-6 libxext6 libxrender1 libxtst6 libxi6 libgtk-3-0 mesa-utils wget unzip \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /javafx-sdk \
    && wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-sdk.zip \
    && unzip javafx.zip -d /javafx-sdk \
    && mv /javafx-sdk/javafx-sdk-21.0.2/lib /javafx-sdk/lib \
    && rm -rf /javafx-sdk/javafx-sdk-21.0.2 javafx.zip

COPY target/notevault.jar app.jar

ENV DISPLAY=host.docker.internal:0.0

ENV DB_HOST=host.docker.internal
ENV DB_PORT=3306
ENV DB_USER=databaseuser
ENV DB_PASSWORD=secretpassword
ENV DB_NAME=notevault_db

CMD ["java", "-Dprism.order=sw", "--module-path", "/javafx-sdk/lib", "--add-modules", "javafx.controls,javafx.fxml,javafx.swing", "-jar", "app.jar"]

## Use Maven + JDK for building
#FROM maven:3.9.6-amazoncorretto-21 AS build
#LABEL authors="swostikalama"
#
#WORKDIR /app
#
## Copy pom.xml and the pre-built jar
#COPY pom.xml .
#COPY target/notevault.jar app.jar
#
## If you want Maven to build again (optional), uncomment:
## RUN mvn package -DskipTests
#
## Use a lightweight JDK image for runtime
#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#
## Copy the jar built in the previous stage
#COPY --from=build /app/app.jar ./app.jar
#
## Optional: JavaFX setup (if app needs GUI)
## RUN apt-get update && apt-get install -y \
##     libx11-6 libxext6 libxrender1 libxtst6 libxi6 libgtk-3-0 mesa-utils wget unzip \
##     && rm -rf /var/lib/apt/lists/*
##
## RUN mkdir -p /javafx-sdk \
##     && wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-sdk.zip \
##     && unzip javafx.zip -d /javafx-sdk \
##     && mv /javafx-sdk/javafx-sdk-21.0.2/lib /javafx-sdk/lib \
##     && rm -rf /javafx-sdk/javafx-sdk-21.0.2 javafx.zip
#
## Environment variables
#ENV DB_HOST=host.docker.internal
#ENV DB_PORT=3307
#ENV DB_USER=databaseuser
#ENV DB_PASSWORD=secretpassword
#ENV DB_NAME=notevault_db
#
## Run the application
#CMD ["java", "-jar", "app.jar"]