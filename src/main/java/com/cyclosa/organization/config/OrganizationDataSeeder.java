package com.cyclosa.organization.config;

import com.cyclosa.organization.entity.*;
import com.cyclosa.organization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class OrganizationDataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final CompanyRepository companyRepository;
    private final RegionRepository regionRepository;
    private final BranchRepository branchRepository;
    private final CostCenterRepository costCenterRepository;
    private final JobTitleRepository jobTitleRepository;
    private final JobLevelRepository jobLevelRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentVersionRepository departmentVersionRepository;

    public static final UUID DEFAULT_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    @Transactional
    public void run(String... args) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM companies WHERE id = ? OR code = ?",
                Integer.class,
                DEFAULT_COMPANY_ID,
                "CYC_CORP"
        );
        if (count != null && count > 0) {
            log.info("Dữ liệu tổ chức mặc định đã tồn tại trong database, bỏ qua Seeding.");
            return;
        }

        log.info("Bắt đầu khởi tạo dữ liệu mẫu Module Quản lý tổ chức CYCLOSA...");

        // 1. Chèn Công ty mặc định với UUID cố định qua JdbcTemplate để tránh xung đột Hibernate merge()
        jdbcTemplate.update("""
            INSERT INTO companies (id, code, name, tax_code, address, phone, email, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
            ON CONFLICT (id) DO NOTHING
        """,
                DEFAULT_COMPANY_ID,
                "CYC_CORP",
                "Công ty Cổ phần Tập đoàn CYCLOSA",
                "0109998888",
                "Tòa nhà Keangnam Landmark 72, Đường Phạm Hùng, Nam Từ Liêm, Hà Nội",
                "02473008888",
                "contact@cyclosa.vn",
                "ACTIVE"
        );

        Company company = companyRepository.findById(DEFAULT_COMPANY_ID)
                .orElseGet(() -> companyRepository.findByCode("CYC_CORP")
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy công ty mặc định sau khi seed")));

        // 2. Vùng miền
        Region regNorth = Region.builder()
                .companyId(company.getId())
                .code("REG_NORTH")
                .name("Khu vực Miền Bắc")
                .build();
        regNorth = regionRepository.save(regNorth);

        Region regSouth = Region.builder()
                .companyId(company.getId())
                .code("REG_SOUTH")
                .name("Khu vực Miền Nam")
                .build();
        regSouth = regionRepository.save(regSouth);

        // 3. Chi nhánh
        Branch branchHn = Branch.builder()
                .regionId(regNorth.getId())
                .code("BR_HN_HQ")
                .name("Trụ sở chính Hà Nội")
                .address("Tầng 25, Keangnam Landmark 72, Hà Nội")
                .latitude(new BigDecimal("21.0169380"))
                .longitude(new BigDecimal("105.7836360"))
                .attendanceRadiusMeters(150)
                .isActive(true)
                .build();
        branchRepository.save(branchHn);

        Branch branchHcm = Branch.builder()
                .regionId(regSouth.getId())
                .code("BR_HCM_01")
                .name("Chi nhánh Hồ Chí Minh")
                .address("Tầng 12, Landmark 81, Vinhomes Central Park, TP.HCM")
                .latitude(new BigDecimal("10.7951230"))
                .longitude(new BigDecimal("106.7218330"))
                .attendanceRadiusMeters(150)
                .isActive(true)
                .build();
        branchRepository.save(branchHcm);

        // 4. Trung tâm chi phí
        CostCenter ccBod = costCenterRepository.save(CostCenter.builder()
                .companyId(company.getId()).code("CC_BOD").name("Khối Điều hành Ban Tổng Giám đốc")
                .description("Ngân sách điều hành cấp cao").build());

        CostCenter ccTech = costCenterRepository.save(CostCenter.builder()
                .companyId(company.getId()).code("CC_TECH").name("Khối Công nghệ & Sản phẩm")
                .description("Ngân sách R&D và hạ tầng kỹ thuật").build());

        CostCenter ccHr = costCenterRepository.save(CostCenter.builder()
                .companyId(company.getId()).code("CC_HR_ADMIN").name("Khối Nhân sự & Hành chính")
                .description("Ngân sách quản trị nhân sự").build());

        CostCenter ccFin = costCenterRepository.save(CostCenter.builder()
                .companyId(company.getId()).code("CC_FINANCE").name("Khối Tài chính Kế toán")
                .description("Ngân sách tài chính").build());

        // 5. Cấp bậc chức danh
        jobLevelRepository.save(JobLevel.builder().companyId(company.getId()).code("LVL_01").name("Level 1 - Thực tập sinh (Intern)").rankOrder(1).build());
        jobLevelRepository.save(JobLevel.builder().companyId(company.getId()).code("LVL_02").name("Level 2 - Nhân viên (Junior)").rankOrder(2).build());
        jobLevelRepository.save(JobLevel.builder().companyId(company.getId()).code("LVL_03").name("Level 3 - Chuyên viên (Mid-level)").rankOrder(3).build());
        jobLevelRepository.save(JobLevel.builder().companyId(company.getId()).code("LVL_04").name("Level 4 - Chuyên viên cao cấp (Senior)").rankOrder(4).build());
        jobLevelRepository.save(JobLevel.builder().companyId(company.getId()).code("LVL_05").name("Level 5 - Trưởng nhóm (Team Leader)").rankOrder(5).build());
        jobLevelRepository.save(JobLevel.builder().companyId(company.getId()).code("LVL_06").name("Level 6 - Trưởng phòng (Manager)").rankOrder(6).build());
        jobLevelRepository.save(JobLevel.builder().companyId(company.getId()).code("LVL_07").name("Level 7 - Giám đốc khối (Director)").rankOrder(7).build());
        jobLevelRepository.save(JobLevel.builder().companyId(company.getId()).code("LVL_08").name("Level 8 - Ban Giám đốc điều hành (C-Level)").rankOrder(8).build());

        // 6. Chức danh công việc
        jobTitleRepository.save(JobTitle.builder().companyId(company.getId()).code("CEO").name("Tổng Giám đốc Điều hành").build());
        jobTitleRepository.save(JobTitle.builder().companyId(company.getId()).code("CTO").name("Giám đốc Công nghệ").build());
        jobTitleRepository.save(JobTitle.builder().companyId(company.getId()).code("CFO").name("Giám đốc Tài chính").build());
        jobTitleRepository.save(JobTitle.builder().companyId(company.getId()).code("CHRO").name("Giám đốc Nhân sự").build());
        jobTitleRepository.save(JobTitle.builder().companyId(company.getId()).code("DEV_LEAD").name("Trưởng nhóm Lập trình").build());
        jobTitleRepository.save(JobTitle.builder().companyId(company.getId()).code("DEV_SR").name("Kỹ sư Phần mềm cao cấp").build());
        jobTitleRepository.save(JobTitle.builder().companyId(company.getId()).code("QA_LEAD").name("Trưởng nhóm Quản lý Chất lượng").build());
        jobTitleRepository.save(JobTitle.builder().companyId(company.getId()).code("HR_SPEC").name("Chuyên viên Nhân sự").build());

        // 7. Cây phòng ban
        LocalDate effectiveDate = LocalDate.of(2026, 1, 1);

        // Dept BOD (Root)
        Department deptBod = departmentRepository.save(Department.builder().companyId(company.getId()).code("DEPT_BOD").build());
        departmentVersionRepository.save(DepartmentVersion.builder()
                .department(deptBod).name("Ban Tổng Giám đốc").effectiveFrom(effectiveDate)
                .parentDepartmentId(null).costCenterId(ccBod.getId()).isPermissionInherited(true).build());

        // Dept TECH (con của BOD)
        Department deptTech = departmentRepository.save(Department.builder().companyId(company.getId()).code("DEPT_TECH").build());
        departmentVersionRepository.save(DepartmentVersion.builder()
                .department(deptTech).name("Khối Công nghệ & Sản phẩm").effectiveFrom(effectiveDate)
                .parentDepartmentId(deptBod.getId()).costCenterId(ccTech.getId()).isPermissionInherited(true).build());

        // Dept DEV (con của TECH)
        Department deptDev = departmentRepository.save(Department.builder().companyId(company.getId()).code("DEPT_DEV").build());
        departmentVersionRepository.save(DepartmentVersion.builder()
                .department(deptDev).name("Phòng Phát triển Phần mềm").effectiveFrom(effectiveDate)
                .parentDepartmentId(deptTech.getId()).costCenterId(ccTech.getId()).isPermissionInherited(true).build());

        // Dept QA (con của TECH)
        Department deptQa = departmentRepository.save(Department.builder().companyId(company.getId()).code("DEPT_QA").build());
        departmentVersionRepository.save(DepartmentVersion.builder()
                .department(deptQa).name("Phòng Đảm bảo Chất lượng (QA)").effectiveFrom(effectiveDate)
                .parentDepartmentId(deptTech.getId()).costCenterId(ccTech.getId()).isPermissionInherited(true).build());

        // Dept HR (con của BOD)
        Department deptHr = departmentRepository.save(Department.builder().companyId(company.getId()).code("DEPT_HR").build());
        departmentVersionRepository.save(DepartmentVersion.builder()
                .department(deptHr).name("Ban Nhân sự & Hành chính").effectiveFrom(effectiveDate)
                .parentDepartmentId(deptBod.getId()).costCenterId(ccHr.getId()).isPermissionInherited(true).build());

        // Dept FIN (con của BOD)
        Department deptFin = departmentRepository.save(Department.builder().companyId(company.getId()).code("DEPT_FIN").build());
        departmentVersionRepository.save(DepartmentVersion.builder()
                .department(deptFin).name("Phòng Tài chính Kế toán").effectiveFrom(effectiveDate)
                .parentDepartmentId(deptBod.getId()).costCenterId(ccFin.getId()).isPermissionInherited(true).build());

        log.info("Hoàn tất nạp dữ liệu mẫu Module Quản lý tổ chức thành công!");
    }
}
