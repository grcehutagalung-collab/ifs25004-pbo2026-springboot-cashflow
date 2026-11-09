# ifs25004-pbo2026-springboot-cashflow

Aplikasi Cashflow (Spring Boot + Vaadin, Clean Architecture, data in-memory).

## Jalankan lokal
```
.\mvnw.cmd spring-boot:run
```
Buka http://localhost:8080 (port dari `.env`: `APP_PORT`).

## Build production (untuk Lighthouse)
```
.\mvnw.cmd -Pproduction clean package
java -jar target\ifs25004-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

## Deploy (Docker, mis. Render)
1. Push proyek ke GitHub.
2. Render -> New -> Web Service -> pilih repo -> Runtime: **Docker** -> Instance: Free.
3. Setelah deploy selesai, salin URL `https://....onrender.com` ke Delcom Grading.
