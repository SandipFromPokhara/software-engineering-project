#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#
#RUN apt-get update && apt-get install -y \
#    libx11-6 libxext6 libxrender1 libxtst6 libxi6 libgtk-3-0 mesa-utils wget unzip \
#    && rm -rf /var/lib/apt/lists/*
#
#RUN mkdir -p /javafx-sdk \
#    && wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.11/openjfx-21.0.11-ea+2_linux-aarch64_bin-sdk.zip \
#    && unzip javafx.zip -d /javafx-sdk \
#    && mv /javafx-sdk/javafx-sdk-21.0.11/lib /javafx-sdk/lib \
#    && rm -rf /javafx-sdk/javafx-sdk-21.0.11 javafx.zip
#
#COPY target/notevault.jar app.jar
#
#ENV DISPLAY=host.docker.internal:0.0
#
##CMD ["java", "-Dprism.order=sw", "--module-path", "/javafx-sdk/lib", "--add-modules", "javafx.controls,javafx.fxml,javafx.swing", "-jar", "app.jar"]
#CMD ["java", "--module-path", "/javafx-sdk/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "app.jar"]

FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install JavaFX dependencies + fontconfig
RUN apt-get update && apt-get install -y \
    libx11-6 libxext6 libxrender1 libxtst6 libxi6 libgtk-3-0 mesa-utils wget unzip fontconfig \
    && rm -rf /var/lib/apt/lists/*

# Create font directory
RUN mkdir -p /usr/share/fonts/truetype/noto

# Download required fonts manually (Myanmar, Devanagari, Sinhala)
RUN wget -O /usr/share/fonts/truetype/noto/NotoSansMyanmar-Regular.ttf \
      https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansMyanmar/NotoSansMyanmar-Regular.ttf \
 && wget -O /usr/share/fonts/truetype/noto/NotoSansDevanagari-Regular.ttf \
      https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansDevanagari/NotoSansDevanagari-Regular.ttf \
 && wget -O /usr/share/fonts/truetype/noto/NotoSansSinhala-Regular.ttf \
      https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansSinhala/NotoSansSinhala-Regular.ttf

# Rebuild font cache so JavaFX can detect fonts
RUN fc-cache -f -v

# Download and setup JavaFX SDK (ARM64 for your Mac)
RUN mkdir -p /javafx-sdk \
    && wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.11/openjfx-21.0.11-ea+2_linux-aarch64_bin-sdk.zip \
    && unzip javafx.zip -d /javafx-sdk \
    && mv /javafx-sdk/javafx-sdk-21.0.11/lib /javafx-sdk/lib \
    && rm -rf /javafx-sdk/javafx-sdk-21.0.11 javafx.zip

# Copy your application JAR
COPY target/notevault.jar app.jar

# XQuartz display forwarding
ENV DISPLAY=host.docker.internal:0.0

# Run JavaFX app
CMD ["java", "--module-path", "/javafx-sdk/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "app.jar"]
