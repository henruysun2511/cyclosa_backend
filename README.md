# CYCLOSA Backend (`cyclosa_be`)

Backend hệ thống quản lý doanh nghiệp & HRM CYCLOSA xây dựng trên nền tảng Java Spring Boot.

---

## 1. Yêu cầu môi trường

- **Java**: OpenJDK 21 LTS trở lên
- **PostgreSQL**: 15+
- **Redis**: 6+
- **RabbitMQ**: 3.12+ (hỗ trợ notification queue)
- **Maven**: Dự án tích hợp sẵn Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

## 2. Cấu trúc thư mục

```
cyclosa_be/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/cyclosa/
│   │   │   ├── CyclosaApplication.java
│   │   │   ├── auth/              # Xác thực (JWT, OAuth2, User, AuthService, AuthController)
│   │   │   └── common/            # Cấu hình & tiện ích dùng chung
│   │   │       ├── config/        # SecurityConfig, OpenAPIConfig, RedisConfig, RabbitMQConfig, PasswordConfig
│   │   │       ├── entity/        # BaseEntity (Auditing)
│   │   │       ├── enums/         # UserRole, UserStatus
│   │   │       ├── exception/     # AppException, ErrorCode, GlobalExceptionHandler
│   │   │       ├── response/      # ApiResponse, PageData, ValidationErrorData
│   │   │       └── util/          # PageableUtils, SecurityUtils
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-local.properties
│   │       └── .env.example
│   └── test/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## 3. Cấu hình & Biến môi trường

Tạo file `.env` hoặc cấu hình trong `application-local.properties`:

```properties
DB_URL=jdbc:postgresql://localhost:5432/cyclosa_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

---

## 4. Hướng dẫn chạy

### Biên dịch:
```bash
./mvnw clean compile
```

### Chạy ứng dụng:
```bash
./mvnw spring-boot:run
```

### Swagger UI API Docs:
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`
