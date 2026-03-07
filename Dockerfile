FROM eclipse-temurin:21-jdk
WORKDIR /app

RUN apt-get update && apt-get install -y \
    libx11-6 libxext6 libxrender1 libxtst6 libxi6 libgtk-3-0 mesa-utils xvfb wget unzip \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /javafx-sdk \
    && wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-sdk.zip \
    && unzip javafx.zip -d /javafx-sdk \
    && mv /javafx-sdk/javafx-sdk-21.0.2/lib /javafx-sdk/lib \
    && rm -rf /javafx-sdk/javafx-sdk-21.0.2 javafx.zip

COPY target/notevault.jar app.jar

ENV DISPLAY=:99

CMD ["sh", "-c", "Xvfb :99 -screen 0 1280x1024x24 & java -Dprism.order=sw --module-path /javafx-sdk/lib --add-modules javafx.controls,javafx.fxml,javafx.swing -jar app.jar"]