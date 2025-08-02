# 1️⃣ Maven tabanlı build aşaması
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Bağımlılıkları önceden indir (cache için)
COPY pom.xml .
RUN mvn dependency:go-offline

# Tüm kaynak kodu kopyala ve jar build et
COPY . .
RUN mvn clean package -DskipTests

# 2️⃣ Final image - daha küçük boyut
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Jar dosyasını kopyala
COPY --from=builder /app/target/falbackend-0.0.1-SNAPSHOT.jar app.jar

# Port aç
EXPOSE 8080

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "app.jar"]
