# Annotations sử dụng trong Worksphere/HRM

Tài liệu liệt kê các annotation đang được dùng trong code (`src/main/java/com/hrm`), giải thích vai trò và vị trí sử dụng.

---

## 1. Khởi tạo ứng dụng & cấu hình

| Annotation | Ý nghĩa | Dùng ở đâu |
|---|---|---|
| `@SpringBootApplication` | Gộp `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan` — đánh dấu đây là app Spring Boot và quét toàn bộ package `com.hrm` | `HrmApplication.java` |
| `@Configuration` | Đánh dấu class là nơi khai báo bean bằng method `@Bean` | `SecurityConfig`, `RedisConfig`, `OpenAPIConfig`, `RabbitMQConfig`... |
| `@Bean` | Khai báo 1 bean (đối tượng) cho Spring quản lý, thường ở trong `@Configuration` | `SecurityConfig` (filterChain, passwordEncoder), `RedisConfig`... |
| `@Value("${...}")` | Đọc giá trị từ `application.yml` / biến môi trường, có default | `SecurityConfig` (cors origins), `EmailService`... |
| `@PostConstruct` | Method chạy 1 lần ngay sau khi bean được tạo xong | service khởi tạo dữ liệu |
| `@EnableCaching` | Bật cơ chế cache của Spring | `HrmApplication.java` |
| `@EnableJpaAuditing` | Bật tự động gán `createdAt`/`updatedAt` (kết hợp `@CreatedDate`/`@LastModifiedDate`) | `HrmApplication.java` |
| `@EnableScheduling` | Bật chạy job định kỳ (`@Scheduled`) | `HrmApplication.java` |
| `@EnableWebSecurity` | Bật bảo mật web của Spring Security | `SecurityConfig.java` |
| `@EnableMethodSecurity` | Bật `@PreAuthorize` trên từng method | `SecurityConfig.java` |

---

## 2. Đăng ký bean (Stereotype)

| Annotation | Ý nghĩa | Dùng ở đâu |
|---|---|---|
| `@Component` | Đánh dấu class là bean chung, Spring quét và quản lý | `JwtAuthFilter`, `DepartmentMapper` (trước khi chuyển MapStruct), utility... |
| `@Service` | Bean tầng nghiệp vụ | `DepartmentService`, `AuthService`, `NotificationService`... |
| `@Repository` | Bean tầng truy cập dữ liệu (JPA repo) | `DepartmentRepository`, `UserRepository`, `PositionRepository`... |
| `@RestController` | Controller trả dữ liệu JSON | `DepartmentController`, `AuthController`, `NotificationController`... |
| `@RestControllerAdvice` | Bắt exception global cho mọi controller, trả JSON lỗi thống nhất | `GlobalExceptionHandler` |

---

## 3. Lombok (giảm boilerplate)

| Annotation | Ý nghĩa |
|---|---|
| `@Getter` / `@Setter` | Tự sinh getter / setter cho mọi field |
| `@Data` | Tự sinh getter, setter, `toString`, `equals`, `hashCode` |
| `@Builder` | Tạo builder pattern để khởi tạo object dễ đọc; `@Builder.Default` gán giá trị mặc định cho field |
| `@NoArgsConstructor` | Sinh constructor không tham số (bắt buộc cho JPA entity) |
| `@AllArgsConstructor` | Sinh constructor đủ tham số |
| `@RequiredArgsConstructor` | Sinh constructor nhận các field `final` — Spring dùng để **inject dependency** |
| `@Slf4j` | Tạo biến `log` để ghi log |

**Ví dụ** — `DepartmentService`:
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository deptRepository;  // final → có trong constructor
    private final DepartmentMapper mapper;
}
```

---

## 4. Web / HTTP (Controller)

| Annotation | Ý nghĩa |
|---|---|
| `@RequestMapping("/api/v1/departments")` | URL gốc của controller; `@RequestMapping` đơn lẻ để đáp ứng mọi method |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@PatchMapping` / `@DeleteMapping` | Gán HTTP method + path cho handler |
| `@RequestBody` | Đọc JSON body và map vào DTO (`@Valid` đi kèm để validate) |
| `@PathVariable` | Lấy giá trị từ đường dẫn, VD `/departments/{id}` |
| `@RequestParam` | Lấy giá trị từ query string, VD `?keyword=IT` |
| `@Valid` | Kích hoạt Bean Validation trên object truyền vào trước khi xử lý |

**Ví dụ** — `DepartmentController`:
```java
@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable Long id) { ... }

@PostMapping
public ResponseEntity<ApiResponse<DepartmentResponse>> create(
        @Valid @RequestBody CreateDepartmentRequest request) { ... }
```

---

## 5. Bảo mật

| Annotation | Ý nghĩa |
|---|---|
| `@PreAuthorize("...")` | Chặn trước khi vào method nếu user không thỏa điều kiện (expression SpEL) |

**Ví dụ:**
```java
@PreAuthorize("hasRole('ADMIN')")                    // chỉ ADMIN
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")       // ADMIN hoặc MANAGER
@PreAuthorize("isAuthenticated()")                   // chỉ cần đã đăng nhập
```

Sai quyền → `AccessDeniedException` → `GlobalExceptionHandler` trả **403**.

---

## 6. Validation (Bean Validation — chạy khi có `@Valid`)

| Annotation | Kiểm tra | Ví dụ trong code |
|---|---|---|
| `@NotBlank` | Chuỗi không rỗng/không toàn khoảng trắng | `name` trong `CreateDepartmentRequest` |
| `@NotNull` | Không null | field bắt buộc |
| `@Size(min, max)` | Độ dài chuỗi/collection | `name` 2-100 ký tự |
| `@Min(0)` | Số >= giá trị | `baseSalary` trong `EmployeeDto` |
| `@Positive` | Số dương | `managerId` |
| `@Email` | Định dạng email hợp lệ | `email` trong `AuthDto` |
| `@Pattern(regexp)` | Khớp biểu thức chính quy | `code` phòng ban `^[A-Z0-9_]{2,20}$` |
| `@PastOrPresent` | Ngày không được ở tương lai | `hiredDate` trong `EmployeeDto` |

Message lỗi nằm trong thuộc tính `message = "..."`, được `GlobalExceptionHandler.handleValidation` lấy ra trả client.

---

## 7. JPA / Hibernate

| Annotation | Ý nghĩa |
|---|---|
| `@Entity` | Đánh dấu class là bảng trong DB |
| `@Table(name, uniqueConstraints)` | Chỉ định tên bảng; `@UniqueConstraint` khai báo ràng buộc unique trên nhiều cột |
| `@Id` | Khóa chính |
| `@GeneratedValue(strategy = IDENTITY)` | Tự sinh id (auto-increment) |
| `@Column(name, nullable, length...)` | Cấu hình cột |
| `@Version` | Optimistic lock — chống 2 người sửa cùng 1 record |
| `@ManyToOne` / `@OneToMany` | Quan hệ nhiều-một / một-nhiều |
| `@JoinColumn` | Cột khóa ngoại |
| `@Enumerated(EnumType.STRING)` | Lưu enum dưới dạng chuỗi |
| `@MappedSuperclass` | Class cha chứa field dùng chung, không phải bảng | `BaseEntity` |
| `@EntityListeners(AuditingEntityListener.class)` | Gắn listener JPA (cho auditing) | `BaseEntity` |
| `@CreatedDate` / `@LastModifiedDate` | Tự gán `createdAt` / `updatedAt` khi insert/update | `BaseEntity` |
| `@SQLDelete` | Thay lệnh DELETE bằng UPDATE (soft delete) | `Department`, `User`, `Position` |
| `@SQLRestriction` | Tự thêm `WHERE deleted_at IS NULL` vào mọi query | `Department`, `User`, `Position` |
| `@Query` | Viết query JPQL/native tùy chỉnh | `DepartmentRepository` |
| `@Param` | Gán tham số vào query | `DepartmentRepository` |
| `@Modifying` | Đánh dấu query INSERT/UPDATE/DELETE | `NotificationRepository` |

**Ví dụ** — `Department`:
```java
@Entity
@Table(name = "departments")
@SQLDelete(sql = "UPDATE departments SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Integer version;
}
```

---

## 8. Transaction & Cache

| Annotation | Ý nghĩa |
|---|---|
| `@Transactional` | Bọc method trong 1 transaction; exception → rollback toàn bộ |
| `@Cacheable(name, key)` | Đọc từ cache trước, miss mới gọi method rồi lưu lại |
| `@CacheEvict(name, allEntries/key)` | Xóa cache sau khi write |
| `@CachePut(name, key)` | Luôn chạy method rồi lưu kết quả vào cache |
| `@Caching(...)` | Kết hợp nhiều annotation cache trong 1 method |

**Ví dụ** — `DepartmentService.update`:
```java
@Transactional
@Caching(
    put   = { @CachePut(value = "departments", key = "#id") },
    evict = { @CacheEvict(value = "dept-list", allEntries = true) }
)
public DepartmentResponse update(Long id, UpdateDepartmentRequest req) { ... }
```

---

## 9. Exception handling

| Annotation | Ý nghĩa |
|---|---|
| `@ExceptionHandler(XException.class)` | Khai báo method xử lý 1 loại exception — Spring gọi tự động khi exception đó được throw |

**Ví dụ** — `GlobalExceptionHandler`:
```java
@ExceptionHandler(AppException.class)
public ResponseEntity<ApiResponse<Void>> handleApp(AppException ex) { ... }
```

---

## 10. MapStruct (mapper tự sinh code)

| Annotation | Ý nghĩa |
|---|---|
| `@Mapper(componentModel = "spring")` | Đánh dấu interface là mapper; Spring sinh bean implementation khi compile |
| `@Mapping(target, source, expression, ignore, constant)` | Quy định cách map 1 field; `expression` cho logic tùy chỉnh (trim, toUpperCase) |
| `@MappingTarget` | Tham số nhận dữ liệu được cập nhật (dùng cho method update) |

**Ví dụ** — `DepartmentMapper`:
```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentMapper {
    @Mapping(target = "name", expression = "java(req.getName().trim())")
    @Mapping(target = "code", expression = "java(req.getCode().toUpperCase().trim())")
    @Mapping(target = "version", constant = "0")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Department toEntity(CreateDepartmentRequest req);
}
```

---

## 11. Async / Scheduling / Messaging

| Annotation | Ý nghĩa |
|---|---|
| `@Async` | Chạy method trong thread riêng (không chặn request) | `EmailService.sendEmail` |
| `@Scheduled(cron = "...")` | Chạy method theo lịch | `NotificationService` (cleanup 2h sáng) |
| `@RabbitListener(queues = "...")` | Lắng nghe message từ RabbitMQ | `NotificationConsumer` |

---

## 12. Swagger / OpenAPI

| Annotation | Ý nghĩa |
|---|---|
| `@Operation(summary, description)` | Mô tả 1 endpoint trên Swagger UI |
| `@Tag(name, description)` | Nhóm các endpoint lại trên Swagger UI |
| `@SecurityRequirement(name = "bearerAuth")` | Khai báo endpoint cần JWT token |

**Ví dụ** — `DepartmentController`:
```java
@Tag(name = "Department Management", description = "Quản lý phòng ban")
@SecurityRequirement(name = "bearerAuth")
@GetMapping("/{id}")
@Operation(summary = "Lấy thông tin phòng ban theo ID")
public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable Long id) { ... }
```

---

## 13. Khác

| Annotation | Ý nghĩa |
|---|---|
| `@Override` | Khai báo method ghi đè method của class cha/interface |
| `@JsonInclude(Include.NON_NULL)` | Khi serialize JSON, bỏ qua field có giá trị null | `ApiResponse` |

---

## Ghi chú

- Annotations được xử lý bởi các công cụ khác nhau: **Lombok** (compile), **Spring** (runtime), **Hibernate/JPA** (runtime), **MapStruct** (compile — sinh code), **Bean Validation** (runtime), **SpringDoc** (sinh tài liệu).
- Quy tắc vàng để Spring thấy 1 annotation: class phải nằm trong package `com.hrm` hoặc package con của nó (phạm vi `@ComponentScan`).
