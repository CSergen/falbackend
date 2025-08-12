# \# ☕ Fal Bakma Uygulaması Backend

# 

# Bu proje, \*\*Flutter\*\* tabanlı bir mobil uygulamanın backend servisidir. Kullanıcılar kahve falı görsellerini yükler, \*\*Gemini AI API\*\* aracılığıyla yorum alır ve geçmiş yorumlarını görüntüleyebilir.

# 

# ---

# 

# \## 🚀 Özellikler

# 

# \* \*\*JWT Authentication\*\* (Access + Refresh Token desteği)

# \* Kullanıcı \*\*kayıt\*\* ve \*\*giriş\*\* işlemleri

# \* \*\*Multipart\*\* ve \*\*Base64\*\* formatında resim yükleme

# \* \*\*Gemini AI\*\* ile fal yorumlama

# \* PostgreSQL veritabanında geçmiş yorum saklama

# \* Kategori bazlı fal yorumlama

# \* Docker Compose ile hızlı kurulum

# \* AWS üzerinde deployment uyumu

# 

# ---

# 

# \## 🛠 Teknolojiler

# 

# \* \*\*Java 21\*\*

# \* \*\*Spring Boot 3\*\*

# \* \*\*PostgreSQL\*\*

# \* \*\*Docker \& Docker Compose\*\*

# \* \*\*Feign Client\*\* (Gemini API entegrasyonu için)

# \* \*\*JWT\*\* ile güvenli kimlik doğrulama

# 

# ---

# 

# \## 📂 Proje Yapısı

# 

# ```plaintext

# falbackend/

# ├── src/

# │   ├── main/java/com/reisfal/falbackend

# │   │   ├── config/       → CORS, güvenlik ayarları

# │   │   ├── controller/   → API endpointleri

# │   │   ├── dto/          → Veri transfer objeleri

# │   │   ├── model/        → JPA entity sınıfları

# │   │   ├── repository/   → Veritabanı işlemleri

# │   │   ├── security/     → JWT ve auth filtreleri

# │   │   ├── service/      → İş mantığı ve Gemini API entegrasyonu

# │   └── resources/

# │       ├── application.properties → Konfigürasyonlar

# ├── Dockerfile

# ├── docker-compose.yml

# └── README.md

# ```

# 

# ---

# 

# \## ⚡ Kurulum ve Çalıştırma

# 

# \### 1️⃣ Gerekli araçlar

# 

# \* Docker \& Docker Compose

# \* Git

# \* Java 21

# \* Maven

# 

# \### 2️⃣ Kaynak kodu klonla

# 

# ```bash

# git clone https://github.com/KULLANICI\_ADIN/falbackend.git

# cd falbackend

# ```

# 

# \### 3️⃣ Docker ile çalıştır

# 

# ```bash

# docker compose up --build

# ```

# 

# > PostgreSQL `localhost:5432` üzerinde çalışır, backend `localhost:8080` portunda yayında olur.

# 

# \### 4️⃣ Test et

# 

# ```bash

# curl http://localhost:8080/api/auth/test

# ```

# 

# ---

# 

# \## 🔑 Ortam Değişkenleri

# 

# `application.properties` içinde gerekli ayarlar:

# 

# ```properties

# spring.datasource.url=jdbc:postgresql://postgres:5432/faldb

# spring.datasource.username=sergen

# spring.datasource.password=1234

# jwt.secret=SECRET\_KEY

# jwt.access.expiration=900000

# jwt.refresh.expiration=604800000

# gemini.api.key=GEMINI\_API\_KEY

# ```

# 

# ---

# 

# \## 📌 API Endpointleri

# 

# | Method | Endpoint             | Açıklama                             |

# | ------ | -------------------- | ------------------------------------ |

# | POST   | `/api/auth/register` | Kullanıcı kaydı                      |

# | POST   | `/api/auth/login`    | Giriş yap (JWT al)                   |

# | POST   | `/api/fal`           | Base64 görsel yükle, AI yorumu al    |

# | POST   | `/fortune`           | Multipart görsel yükle, AI yorumu al |

# | GET    | `/fortunes`          | Geçmiş yorumları listele             |

# 

# ---

# 

# \## 📝 Lisans

# 

# Bu proje MIT lisansı ile lisanslanmıştır.



