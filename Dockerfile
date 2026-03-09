#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#
#RUN apt-get update && apt-get install -y \
#    libx11-6 libxext6 libxrender1 libxtst6 libxi6 libgtk-3-0 mesa-utils wget unzip \
#    && rm -rf /var/lib/apt/lists/*
#
#RUN mkdir -p /javafx-sdk \
#    && wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-sdk.zip \
#    && unzip javafx.zip -d /javafx-sdk \
#    && mv /javafx-sdk/javafx-sdk-21.0.2/lib /javafx-sdk/lib \
#    && rm -rf /javafx-sdk/javafx-sdk-21.0.2 javafx.zip
#
#COPY target/notevault.jar app.jar
#
#ENV DISPLAY=host.docker.internal:0.0
#
#CMD ["java", "-Dprism.order=sw", "--module-path", "/javafx-sdk/lib", "--add-modules", "javafx.controls,javafx.fxml,javafx.swing", "-jar", "app.jar"]

# Use OpenJDK 21
FROM eclipse-temurin:21-jdk

# Install required libraries
RUN apt-get update && apt-get install -y \
    maven wget unzip libgtk-3-0 libgl1 libgl1-mesa-dri libx11-6 libxtst6 libxi6 libxrender1 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Download JavaFX SDK (ARM64 for Apple Silicon)
RUN wget https://download2.gluonhq.com/openjfx/21/openjfx-21_linux-aarch64_bin-sdk.zip -O /tmp/javafx.zip \
    && unzip /tmp/javafx.zip -d /opt \
    && rm /tmp/javafx.zip

ENV JAVAFX_HOME=/opt/javafx-sdk-21

WORKDIR /app

# Copy Maven files first
COPY pom.xml .

# Download dependencies
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Set CMD to run the jar (do not set DISPLAY here)
CMD ["java", \
"-Dprism.order=sw", \
"-Dprism.verbose=true", \
"--module-path", "/opt/javafx-sdk-21/lib", \
"--add-modules", "javafx.controls,javafx.fxml", \
"-jar", "target/sum-product_fx-1.0-SNAPSHOT.jar"]