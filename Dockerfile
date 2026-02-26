# -------- BUILD STAGE --------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jdk

# Install X11 libraries for JavaFX GUI
RUN apt-get update && apt-get install -y \
    libxrender1 libxtst6 libxi6 x11-apps xvfb \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/noteVault.jar app.jar

# Allow GUI display via host X server
ENV DISPLAY=:0

# Start X virtual framebuffer and run app
CMD ["sh", "-c", "Xvfb :99 -screen 0 1024x768x16 & export DISPLAY=:99 && java -jar app.jar"]