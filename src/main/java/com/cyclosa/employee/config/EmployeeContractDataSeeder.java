package com.cyclosa.employee.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class EmployeeContractDataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public static final UUID DEFAULT_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    @Transactional
    public void run(String... args) {
        Integer empCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM employees WHERE company_id = ?",
                Integer.class,
                DEFAULT_COMPANY_ID
        );

        if (empCount != null && empCount > 0) {
            log.info("Dữ liệu nhân sự và hợp đồng mặc định đã tồn tại trong database ({}), bỏ qua Seeding.", empCount);
            return;
        }

        log.info("Bắt đầu khởi tạo dữ liệu mẫu Module Nhân sự (05) & Hợp đồng (08) CYCLOSA...");

        try {
            // Lấy ID các phòng ban, chi nhánh, chức danh, cấp bậc
            Map<String, UUID> depts = queryMap("SELECT code, id FROM departments WHERE company_id = ?", DEFAULT_COMPANY_ID);
            Map<String, UUID> branches = queryMap("SELECT code, id FROM branches");
            Map<String, UUID> titles = queryMap("SELECT code, id FROM job_titles WHERE company_id = ?", DEFAULT_COMPANY_ID);
            Map<String, UUID> levels = queryMap("SELECT code, id FROM job_levels WHERE company_id = ?", DEFAULT_COMPANY_ID);

            if (depts.isEmpty() || branches.isEmpty() || titles.isEmpty() || levels.isEmpty()) {
                log.warn("Chưa tìm thấy đầy đủ cơ cấu tổ chức, vui lòng đảm bảo OrganizationDataSeeder đã chạy.");
                return;
            }

            UUID branchHn = branches.getOrDefault("BR_HN_HQ", branches.values().iterator().next());
            UUID branchHcm = branches.getOrDefault("BR_HCM_01", branchHn);

            UUID deptBod = depts.getOrDefault("DEPT_BOD", depts.values().iterator().next());
            UUID deptTech = depts.getOrDefault("DEPT_TECH", deptBod);
            UUID deptDev = depts.getOrDefault("DEPT_DEV", deptTech);
            UUID deptQa = depts.getOrDefault("DEPT_QA", deptTech);
            UUID deptHr = depts.getOrDefault("DEPT_HR", deptBod);
            UUID deptFin = depts.getOrDefault("DEPT_FIN", deptBod);

            UUID titleCeo = titles.get("CEO");
            UUID titleCto = titles.get("CTO");
            UUID titleChro = titles.get("CHRO");
            UUID titleDevLead = titles.get("DEV_LEAD");
            UUID titleDevSr = titles.get("DEV_SR");
            UUID titleQaLead = titles.get("QA_LEAD");
            UUID titleHrSpec = titles.get("HR_SPEC");

            UUID lvlJunior = levels.get("LVL_02");
            UUID lvlMid = levels.get("LVL_03");
            UUID lvlSr = levels.get("LVL_04");
            UUID lvlLead = levels.get("LVL_05");
            UUID lvlC = levels.get("LVL_08");

            // 1. MẪU BIỂU HỢP ĐỒNG (TEMPLATES)
            Map<String, UUID> tplVersions = new HashMap<>();

            UUID tplIndef = insertTemplate(DEFAULT_COMPANY_ID, "Mẫu HĐLĐ Không xác định thời hạn (Cấp Quản lý & C-Level)", "LABOR_CONTRACT", "INDEFINITE");
            tplVersions.put("INDEFINITE", insertTemplateVersion(tplIndef, 1, "DOCX", "https://res.cloudinary.com/dcaqsxy0q/raw/upload/v1/cyclosa/templates/hdld_khong_thoi_han.docx", "Mẫu chuẩn BLLĐ 2019 cho nhân sự nòng cốt"));

            UUID tplFixed = insertTemplate(DEFAULT_COMPANY_ID, "Mẫu HĐLĐ Xác định thời hạn (12 - 36 tháng)", "LABOR_CONTRACT", "FIXED_TERM");
            tplVersions.put("FIXED_TERM", insertTemplateVersion(tplFixed, 1, "DOCX", "https://res.cloudinary.com/dcaqsxy0q/raw/upload/v1/cyclosa/templates/hdld_co_thoi_han.docx", "Mẫu chuẩn ký kết chính thức sau thử việc"));

            UUID tplProb = insertTemplate(DEFAULT_COMPANY_ID, "Mẫu Hợp đồng Thử việc (60 ngày)", "LABOR_CONTRACT", "PROBATION");
            tplVersions.put("PROBATION", insertTemplateVersion(tplProb, 1, "DOCX", "https://res.cloudinary.com/dcaqsxy0q/raw/upload/v1/cyclosa/templates/hop_dong_thu_viec.docx", "Hợp đồng thử việc tối đa 60 ngày theo Luật Lao động"));

            UUID tplCollab = insertTemplate(DEFAULT_COMPANY_ID, "Mẫu Thỏa thuận Hợp tác / Chuyên gia tư vấn", "LABOR_CONTRACT", "SERVICE");
            tplVersions.put("SERVICE", insertTemplateVersion(tplCollab, 1, "DOCX", "https://res.cloudinary.com/dcaqsxy0q/raw/upload/v1/cyclosa/templates/thoa_thuan_chuyen_gia.docx", "Mẫu hợp đồng dịch vụ tư vấn kỹ thuật cao cấp"));

            // 2. NHÂN VIÊN MẪU
            Map<String, UUID> empCodeToId = new HashMap<>();

            // NV 1: CEO An
            UUID emp1 = insertEmployee(
                "NV-2026-00001", "Nguyễn Văn", "An", "1985-04-12", "MALE", "0912345671", "an.nguyen@cyclosa.vn",
                "Số 12 Cầu Giấy, Hà Nội", "2021-01-01", null, "8012345671", "0123456781",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                deptBod, branchHn, titleCeo, lvlC, "FULL_TIME", "ACTIVE",
                "CITIZEN_ID", "001085001234", "2021-05-10", "Cục CSQLHC về TTXH",
                "Vietcombank", "Thăng Long", "0011001234567",
                "Nguyễn Minh Anh", "2015-08-20", "CHILD", "001215009911",
                "Trần Thu Hương", "Vợ", "0912998877", "Số 12 Cầu Giấy, Hà Nội"
            );
            empCodeToId.put("NV-2026-00001", emp1);

            // NV 2: CTO Long
            UUID emp2 = insertEmployee(
                "NV-2026-00002", "Lê Hoàng", "Long", "1989-08-15", "MALE", "0923456782", "long.le@cyclosa.vn",
                "Tòa R2 Royal City, Thanh Xuân, Hà Nội", "2021-03-01", null, "8012345672", "0123456782",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                deptTech, branchHn, titleCto, lvlC, "FULL_TIME", "ACTIVE",
                "CITIZEN_ID", "001089002345", "2020-03-15", "Cục CSQLHC về TTXH",
                "Techcombank", "Hà Thành", "1903456789001",
                "Lê Bảo Nam", "2018-12-10", "CHILD", "001218008822",
                "Vũ Mai Phương", "Vợ", "0923887766", "Tòa R2 Royal City, Thanh Xuân, Hà Nội"
            );
            empCodeToId.put("NV-2026-00002", emp2);

            // NV 3: CHRO Cúc
            UUID emp3 = insertEmployee(
                "NV-2026-00003", "Trần Thị Mai", "Cúc", "1990-11-25", "FEMALE", "0934567883", "cuc.tran@cyclosa.vn",
                "Số 88 Phố Huế, Hai Bà Trưng, Hà Nội", "2021-06-01", null, "8012345673", "0123456783",
                "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400",
                deptHr, branchHn, titleChro, lvlC, "FULL_TIME", "ACTIVE",
                "CITIZEN_ID", "001190003456", "2021-08-20", "Cục CSQLHC về TTXH",
                "MB Bank", "Sở Giao Dịch", "0888999888111",
                null, null, null, null,
                "Trần Quốc Dũng", "Bố đẻ", "0934112233", "Số 88 Phố Huế, Hai Bà Trưng, Hà Nội"
            );
            empCodeToId.put("NV-2026-00003", emp3);

            // NV 4: Dev Lead Dũng (HCM)
            UUID emp4 = insertEmployee(
                "NV-2026-00004", "Phạm Minh", "Dũng", "1992-03-18", "MALE", "0945678994", "dung.pham@cyclosa.vn",
                "Vinhomes Central Park, Bình Thạnh, TP.HCM", "2022-02-15", null, "8012345674", "0123456784",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
                deptDev, branchHcm, titleDevLead, lvlLead, "FULL_TIME", "ACTIVE",
                "CITIZEN_ID", "001092004567", "2022-01-10", "Cục CSQLHC về TTXH",
                "Vietcombank", "Bến Thành", "0071001234888",
                "Phạm Tuấn Khang", "2021-05-04", "CHILD", "001221007733",
                "Lý Ngọc Hân", "Vợ", "0945112244", "Vinhomes Central Park, Bình Thạnh, TP.HCM"
            );
            empCodeToId.put("NV-2026-00004", emp4);

            // NV 5: Senior Dev Giang (HN)
            UUID emp5 = insertEmployee(
                "NV-2026-00005", "Đỗ Thu", "Giang", "1995-07-22", "FEMALE", "0956789005", "giang.do@cyclosa.vn",
                "Số 68 Hoàng Cầu, Đống Đa, Hà Nội", "2022-09-01", null, "8012345675", "0123456785",
                "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400",
                deptDev, branchHn, titleDevSr, lvlSr, "FULL_TIME", "ACTIVE",
                "CITIZEN_ID", "001195005678", "2022-06-15", "Cục CSQLHC về TTXH",
                "Techcombank", "Đông Đô", "1903456789005",
                null, null, null, null,
                "Đỗ Văn Hưng", "Bố đẻ", "0956223344", "Số 68 Hoàng Cầu, Đống Đa, Hà Nội"
            );
            empCodeToId.put("NV-2026-00005", emp5);

            // NV 6: Senior Dev Hùng (HCM - HĐ sắp hết hạn)
            UUID emp6 = insertEmployee(
                "NV-2026-00006", "Bùi Quốc", "Hùng", "1994-12-08", "MALE", "0967890116", "hung.bui@cyclosa.vn",
                "Masteri Thảo Điền, Quận 2, TP.HCM", "2023-04-01", null, "8012345676", "0123456786",
                "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400",
                deptDev, branchHcm, titleDevSr, lvlSr, "FULL_TIME", "ACTIVE",
                "CITIZEN_ID", "001094006789", "2021-11-20", "Cục CSQLHC về TTXH",
                "ACB", "Sài Gòn", "2468101214",
                null, null, null, null,
                "Bùi Thị Lan", "Mẹ đẻ", "0967334455", "Masteri Thảo Điền, Quận 2, TP.HCM"
            );
            empCodeToId.put("NV-2026-00006", emp6);

            // NV 7: QA Lead Lan (HN)
            UUID emp7 = insertEmployee(
                "NV-2026-00007", "Hoàng Bích", "Lan", "1993-09-14", "FEMALE", "0978901227", "lan.hoang@cyclosa.vn",
                "Số 15 Lê Văn Lương, Cầu Giấy, Hà Nội", "2023-01-15", null, "8012345677", "0123456787",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400",
                deptQa, branchHn, titleQaLead, lvlLead, "FULL_TIME", "ACTIVE",
                "CITIZEN_ID", "001193007890", "2022-08-10", "Cục CSQLHC về TTXH",
                "VietinBank", "Đô Thành", "101876543210",
                null, null, null, null,
                "Hoàng Văn Minh", "Anh trai", "0978445566", "Số 15 Lê Văn Lương, Cầu Giấy, Hà Nội"
            );
            empCodeToId.put("NV-2026-00007", emp7);

            // NV 8: HR Spec Oanh (HN - PENDING_SIGNATURE)
            UUID emp8 = insertEmployee(
                "NV-2026-00008", "Vũ Kiều", "Oanh", "1997-02-28", "FEMALE", "0989012338", "oanh.vu@cyclosa.vn",
                "Số 24 Kim Mã, Ba Đình, Hà Nội", "2023-08-01", null, "8012345678", "0123456788",
                "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?w=400",
                deptHr, branchHn, titleHrSpec, lvlMid, "FULL_TIME", "ACTIVE",
                "CITIZEN_ID", "001197008901", "2023-03-12", "Cục CSQLHC về TTXH",
                "BIDV", "Hà Tây", "45010001234567",
                null, null, null, null,
                "Vũ Đình Toàn", "Bố đẻ", "0989556677", "Số 24 Kim Mã, Ba Đình, Hà Nội"
            );
            empCodeToId.put("NV-2026-00008", emp8);

            // NV 9: Thử việc Huy (Junior)
            UUID emp9 = insertEmployee(
                "NV-2026-00009", "Đặng Quang", "Huy", "2001-05-19", "MALE", "0990123449", "huy.dang@cyclosa.vn",
                "Số 102 Trần Phú, Hà Đông, Hà Nội",
                LocalDate.now().minusDays(35).toString(),
                LocalDate.now().plusDays(25).toString(),
                "8012345679", "0123456789",
                "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400",
                deptDev, branchHn, titleDevSr, lvlJunior, "FULL_TIME", "PROBATION",
                "CITIZEN_ID", "001201009012", "2023-02-18", "Cục CSQLHC về TTXH",
                "TPBank", "Hà Nội", "03456789001",
                null, null, null, null,
                "Đặng Văn Tùng", "Bố đẻ", "0990667788", "Số 102 Trần Phú, Hà Đông, Hà Nội"
            );
            empCodeToId.put("NV-2026-00009", emp9);

            // NV 10: Nghỉ chế độ Thảo (FIN, ON_LEAVE)
            UUID emp10 = insertEmployee(
                "NV-2026-00010", "Ngô Phương", "Thảo", "1996-10-10", "FEMALE", "0901234550", "thao.ngo@cyclosa.vn",
                "Số 72 Nguyễn Trãi, Thanh Xuân, Hà Nội", "2022-11-01", null, "8012345680", "0123456790",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                deptFin, branchHn, titleHrSpec, lvlMid, "FULL_TIME", "ON_LEAVE",
                "CITIZEN_ID", "001196010123", "2021-09-25", "Cục CSQLHC về TTXH",
                "Techcombank", "Thanh Xuân", "1903456789010",
                "Nguyễn Gia Huy", "2026-08-01", "CHILD", "001226001122",
                "Nguyễn Văn Tuấn", "Chồng", "0901778899", "Số 72 Nguyễn Trãi, Thanh Xuân, Hà Nội"
            );
            empCodeToId.put("NV-2026-00010", emp10);

            // Cập nhật người quản lý
            updateManager(emp2, emp1);
            updateManager(emp3, emp1);
            updateManager(emp4, emp2);
            updateManager(emp5, emp2);
            updateManager(emp6, emp4);
            updateManager(emp7, emp2);
            updateManager(emp8, emp3);
            updateManager(emp9, emp2);
            updateManager(emp10, emp1);

            // 3. HỢP ĐỒNG LAO ĐỘNG
            insertContract("HDLD-2026-00001", emp1, DEFAULT_COMPANY_ID, emp1, tplVersions.get("INDEFINITE"), "INDEFINITE", titleCeo, deptBod, branchHn, "Trụ sở Keangnam Landmark 72, Hà Nội", "40 giờ/tuần", new BigDecimal("55000000"), new BigDecimal("36000000"), "BANK_TRANSFER", "2021-01-01", null, "2020-12-28", "ACTIVE");
            insertContract("HDLD-2026-00002", emp2, DEFAULT_COMPANY_ID, emp1, tplVersions.get("INDEFINITE"), "INDEFINITE", titleCto, deptTech, branchHn, "Trụ sở Keangnam Landmark 72, Hà Nội", "40 giờ/tuần", new BigDecimal("45000000"), new BigDecimal("36000000"), "BANK_TRANSFER", "2021-03-01", null, "2021-02-25", "ACTIVE");
            insertContract("HDLD-2026-00003", emp3, DEFAULT_COMPANY_ID, emp1, tplVersions.get("INDEFINITE"), "INDEFINITE", titleChro, deptHr, branchHn, "Trụ sở Keangnam Landmark 72, Hà Nội", "40 giờ/tuần", new BigDecimal("35000000"), new BigDecimal("32000000"), "BANK_TRANSFER", "2021-06-01", null, "2021-05-28", "ACTIVE");
            insertContract("HDLD-2026-00004", emp4, DEFAULT_COMPANY_ID, emp1, tplVersions.get("FIXED_TERM"), "FIXED_TERM", titleDevLead, deptDev, branchHcm, "Chi nhánh Landmark 81, TP.HCM", "40 giờ/tuần", new BigDecimal("38000000"), new BigDecimal("30000000"), "BANK_TRANSFER", "2022-02-15", "2025-02-14", "2022-02-10", "ACTIVE");
            insertContract("HDLD-2026-00005", emp5, DEFAULT_COMPANY_ID, emp1, tplVersions.get("FIXED_TERM"), "FIXED_TERM", titleDevSr, deptDev, branchHn, "Trụ sở Keangnam Landmark 72, Hà Nội", "40 giờ/tuần", new BigDecimal("30000000"), new BigDecimal("25000000"), "BANK_TRANSFER", "2022-09-01", "2024-08-31", "2022-08-28", "ACTIVE");

            LocalDate soon = LocalDate.now().plusDays(15);
            insertContract("HDLD-2026-00006", emp6, DEFAULT_COMPANY_ID, emp1, tplVersions.get("FIXED_TERM"), "FIXED_TERM", titleDevSr, deptDev, branchHcm, "Chi nhánh Landmark 81, TP.HCM", "40 giờ/tuần", new BigDecimal("29000000"), new BigDecimal("25000000"), "BANK_TRANSFER", soon.minusYears(1).toString(), soon.toString(), soon.minusYears(1).toString(), "ACTIVE");

            insertContract("HDLD-2026-00007", emp7, DEFAULT_COMPANY_ID, emp1, tplVersions.get("FIXED_TERM"), "FIXED_TERM", titleQaLead, deptQa, branchHn, "Trụ sở Keangnam Landmark 72, Hà Nội", "40 giờ/tuần", new BigDecimal("25000000"), new BigDecimal("20000000"), "BANK_TRANSFER", "2023-01-15", "2025-01-14", "2023-01-10", "ACTIVE");
            insertContract("HDLD-2026-00008", emp8, DEFAULT_COMPANY_ID, emp1, tplVersions.get("FIXED_TERM"), "FIXED_TERM", titleHrSpec, deptHr, branchHn, "Trụ sở Keangnam Landmark 72, Hà Nội", "44 giờ/tuần", new BigDecimal("16000000"), new BigDecimal("12000000"), "BANK_TRANSFER", "2023-08-01", "2024-07-31", null, "PENDING_SIGNATURE");
            insertContract("HDTV-2026-00009", emp9, DEFAULT_COMPANY_ID, emp1, tplVersions.get("PROBATION"), "PROBATION", titleDevSr, deptDev, branchHn, "Trụ sở Keangnam Landmark 72, Hà Nội", "40 giờ/tuần", new BigDecimal("15000000"), null, "BANK_TRANSFER", LocalDate.now().minusDays(35).toString(), LocalDate.now().plusDays(25).toString(), LocalDate.now().minusDays(35).toString(), "ACTIVE");
            insertContract("HDLD-2026-00010-NHAP", emp10, DEFAULT_COMPANY_ID, emp1, tplVersions.get("FIXED_TERM"), "FIXED_TERM", titleHrSpec, deptFin, branchHn, "Trụ sở Keangnam Landmark 72, Hà Nội", "40 giờ/tuần", new BigDecimal("18000000"), new BigDecimal("15000000"), "BANK_TRANSFER", LocalDate.now().toString(), LocalDate.now().plusYears(1).toString(), null, "DRAFT");

            log.info("Khởi tạo thành công 10 hồ sơ nhân viên và 10 hợp đồng lao động mẫu vào cơ sở dữ liệu!");

        } catch (Exception e) {
            log.error("Lỗi khi nạp dữ liệu nhân viên và hợp đồng: {}", e.getMessage(), e);
        }
    }

    private Map<String, UUID> queryMap(String sql, Object... params) {
        Map<String, UUID> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(rs.getString(1), (UUID) rs.getObject(2));
        }, params);
        return map;
    }

    private UUID insertTemplate(UUID companyId, String name, String kind, String contractType) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO contract_templates (id, company_id, name, template_kind, contract_type, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, true, NOW())
            ON CONFLICT (id) DO NOTHING
        """, id, companyId, name, kind, contractType);
        return id;
    }

    private UUID insertTemplateVersion(UUID templateId, int verNo, String format, String fileUrl, String desc) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO contract_template_versions (id, template_id, version_no, format, file_url, description, created_at)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            ON CONFLICT (id) DO NOTHING
        """, id, templateId, verNo, format, fileUrl, desc);
        return id;
    }

    private UUID insertEmployee(
        String code, String lastName, String firstName, String dob, String gender, String phone, String email,
        String address, String startDate, String probationEndDate, String taxCode, String insuranceNo, String portraitUrl,
        UUID deptId, UUID branchId, UUID titleId, UUID levelId, String empType, String status,
        String docType, String docNum, String docDate, String docPlace,
        String bankName, String bankBranch, String bankAccount,
        String depName, String depDob, String depRel, String depIdNum,
        String emerName, String emerRel, String emerPhone, String emerAddr
    ) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO employees (
                id, company_id, employee_code, last_name, first_name, date_of_birth, gender,
                phone, personal_email, address, start_date, probation_end_date,
                personal_tax_code, social_insurance_number, portrait_url, created_at
            ) VALUES (?, ?, ?, ?, ?, ?::date, ?, ?, ?, ?, ?::date, ?::date, ?, ?, ?, NOW())
            ON CONFLICT (employee_code) DO NOTHING
        """, id, DEFAULT_COMPANY_ID, code, lastName, firstName, dob, gender, phone, email, address, startDate, probationEndDate, taxCode, insuranceNo, portraitUrl);

        // Work assignment
        jdbcTemplate.update("""
            INSERT INTO employee_work_assignments (
                id, employee_id, effective_from, department_id, branch_id,
                job_title_id, job_level_id, employment_type, change_reason, created_at
            ) VALUES (gen_random_uuid(), ?, ?::date, ?, ?, ?, ?, ?, 'HIRE', NOW())
            ON CONFLICT (employee_id, effective_from) DO NOTHING
        """, id, startDate, deptId, branchId, titleId, levelId, empType);

        // Status history
        jdbcTemplate.update("""
            INSERT INTO employee_status_history (
                id, employee_id, status, effective_from, note, created_at
            ) VALUES (gen_random_uuid(), ?, ?, ?::date, 'Tiếp nhận nhân sự', NOW())
            ON CONFLICT (employee_id, effective_from) DO NOTHING
        """, id, status, startDate);

        // Identity doc
        if (docNum != null) {
            jdbcTemplate.update("""
                INSERT INTO employee_identity_documents (
                    id, employee_id, document_type, document_number, issue_date, issue_place, created_at
                ) VALUES (gen_random_uuid(), ?, ?, ?, ?::date, ?, NOW())
                ON CONFLICT (document_type, document_number) DO NOTHING
            """, id, docType, docNum, docDate, docPlace);
        }

        // Bank account
        if (bankAccount != null) {
            jdbcTemplate.update("""
                INSERT INTO employee_bank_accounts (
                    id, employee_id, bank_name, bank_branch_name, account_number, created_at
                ) VALUES (gen_random_uuid(), ?, ?, ?, ?, NOW())
                ON CONFLICT (employee_id) DO NOTHING
            """, id, bankName, bankBranch, bankAccount);
        }

        // Dependent
        if (depName != null) {
            jdbcTemplate.update("""
                INSERT INTO employee_dependents (
                    id, employee_id, full_name, date_of_birth, relationship, identity_number, tax_deduction_from, created_at
                ) VALUES (gen_random_uuid(), ?, ?, ?::date, ?, ?, ?::date, NOW())
            """, id, depName, depDob, depRel, depIdNum, startDate);
        }

        // Emergency contact
        if (emerName != null) {
            jdbcTemplate.update("""
                INSERT INTO employee_emergency_contacts (
                    id, employee_id, full_name, relationship, phone, address, created_at
                ) VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, NOW())
            """, id, emerName, emerRel, emerPhone, emerAddr);
        }

        return id;
    }

    private void updateManager(UUID empId, UUID mgrId) {
        jdbcTemplate.update("UPDATE employee_work_assignments SET manager_employee_id = ? WHERE employee_id = ?", mgrId, empId);
    }

    private void insertContract(
        String no, UUID empId, UUID compId, UUID repId, UUID tplVerId, String type, UUID titleId, UUID deptId, UUID branchId,
        String loc, String time, BigDecimal base, BigDecimal ins, String pay, String start, String end, String signed, String st
    ) {
        UUID cId = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO contracts (
                id, contract_no, employee_id, employer_company_id, employer_representative_id,
                template_version_id, contract_type, job_title_id, department_id, branch_id,
                work_location, working_time_description, base_salary, insurance_salary,
                payment_method, pay_day_of_month, signing_form, start_date, end_date,
                signed_date, status, created_at
            ) VALUES (
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, 5, 'ELECTRONIC', ?::date, ?::date,
                ?::date, ?, NOW()
            )
            ON CONFLICT (contract_no) DO NOTHING
        """, cId, no, empId, compId, repId, tplVerId, type, titleId, deptId, branchId, loc, time, base, ins, pay, start, end, signed, st);

        // Phụ cấp ăn trưa mặc định
        jdbcTemplate.update("""
            INSERT INTO contract_allowances (id, contract_id, name, amount, is_taxable, created_at)
            VALUES (gen_random_uuid(), (SELECT id FROM contracts WHERE contract_no = ?), 'Phụ cấp ăn trưa', 730000, false, NOW())
            ON CONFLICT (contract_id, name) DO NOTHING
        """, no);
    }
}

