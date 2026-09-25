package com.cyclosa.employee;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.request.CreateEmployeeRequest;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.dto.response.EmployeeImportResponse;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.employee.service.EmployeeImportService;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.entity.OrganizationalUnit;
import com.cyclosa.organization.entity.Position;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.JobLevelRepository;
import com.cyclosa.organization.repository.OrganizationalUnitRepository;
import com.cyclosa.organization.repository.PositionRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class EmployeeImportServiceTest {

    @Mock EmployeeService employeeService;
    @Mock EmployeeRepository employeeRepository;
    @Mock OrganizationalUnitRepository orgUnitRepository;
    @Mock BranchRepository branchRepository;
    @Mock PositionRepository positionRepository;
    @Mock JobLevelRepository jobLevelRepository;
    @Mock PlatformTransactionManager transactionManager;
    @Mock TransactionStatus transactionStatus;

    @InjectMocks EmployeeImportService importService;

    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
    }

    @Test
    @DisplayName("generateImportTemplate tạo file Excel template hợp lệ")
    void generateImportTemplate_Success() throws IOException {
        byte[] bytes = importService.generateImportTemplate();

        assertThat(bytes).isNotEmpty();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(2);
            Sheet sheet0 = wb.getSheetAt(0);
            assertThat(sheet0.getSheetName()).isEqualTo("Danh sách nhân sự");
            assertThat(sheet0.getRow(0).getCell(2).getStringCellValue()).contains("Họ và tên");
        }
    }

    @Test
    @DisplayName("importEmployees ném ngoại lệ khi file rỗng")
    void importEmployees_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        assertThatThrownBy(() -> importService.importEmployees(companyId, file, false))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(EmployeeErrorCode.EMPTY_IMPORT_FILE));
    }

    @Test
    @DisplayName("importEmployees ném ngoại lệ khi file không đúng định dạng Excel")
    void importEmployees_InvalidExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> importService.importEmployees(companyId, file, false))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(EmployeeErrorCode.INVALID_IMPORT_FILE));
    }

    @Test
    @DisplayName("importEmployees thành công khi dữ liệu hợp lệ")
    void importEmployees_ValidData_Success() throws IOException {
        UUID unitId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID posId = UUID.randomUUID();

        OrganizationalUnit unit = OrganizationalUnit.builder().code("IT").name("Phòng IT").companyId(companyId).build();
        unit.setId(unitId);
        Branch branch = Branch.builder().code("HN").name("Chi nhánh Hà Nội").companyId(companyId).build();
        branch.setId(branchId);
        Position position = Position.builder().code("DEV").name("Lập trình viên").companyId(companyId).build();
        position.setId(posId);

        given(orgUnitRepository.findAllByCompanyIdWithDetails(companyId)).willReturn(List.of(unit));
        given(branchRepository.findByCompanyIdWithRegion(companyId)).willReturn(List.of(branch));
        given(positionRepository.findByCompanyId(companyId)).willReturn(List.of(position));
        given(jobLevelRepository.findByCompanyIdOrderByRankOrderAsc(companyId)).willReturn(List.of());
        given(employeeRepository.findByCompanyId(companyId)).willReturn(List.of());

        EmployeeDetailResponse detailResponse = EmployeeDetailResponse.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP-2026-0001")
                .fullName("Nguyễn Văn An")
                .hireDate(LocalDate.of(2026, 1, 15))
                .employmentStatus(EmploymentStatus.PROBATION)
                .build();

        given(employeeService.createEmployee(eq(companyId), any(CreateEmployeeRequest.class)))
                .willReturn(detailResponse);

        byte[] excelBytes = createTestExcel(new Object[][]{
                {1, "EMP-2026-0001", "Nguyễn Văn An", "2026-01-15", "001200001234", "NAM", "1995-05-20",
                        "", "", "IT", "HN", "DEV", "", "", "PROBATION", "FULL_TIME", "", "", "", "", "", "", "", "", "", "", "", "", "FALSE"}
        });

        MockMultipartFile file = new MockMultipartFile("file", "employees.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        EmployeeImportResponse response = importService.importEmployees(companyId, file, false);

        assertThat(response.getTotalRows()).isEqualTo(1);
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getFailureCount()).isEqualTo(0);
        assertThat(response.getSuccessfulEmployees()).hasSize(1);
        assertThat(response.getSuccessfulEmployees().get(0).getFullName()).isEqualTo("Nguyễn Văn An");
    }

    @Test
    @DisplayName("importEmployees partial failure khi có dòng lỗi và dòng hợp lệ")
    void importEmployees_PartialFailure() throws IOException {
        UUID unitId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID posId = UUID.randomUUID();

        OrganizationalUnit unit = OrganizationalUnit.builder().code("IT").name("Phòng IT").companyId(companyId).build();
        unit.setId(unitId);
        Branch branch = Branch.builder().code("HN").name("Chi nhánh Hà Nội").companyId(companyId).build();
        branch.setId(branchId);
        Position position = Position.builder().code("DEV").name("Lập trình viên").companyId(companyId).build();
        position.setId(posId);

        given(orgUnitRepository.findAllByCompanyIdWithDetails(companyId)).willReturn(List.of(unit));
        given(branchRepository.findByCompanyIdWithRegion(companyId)).willReturn(List.of(branch));
        given(positionRepository.findByCompanyId(companyId)).willReturn(List.of(position));
        given(jobLevelRepository.findByCompanyIdOrderByRankOrderAsc(companyId)).willReturn(List.of());
        given(employeeRepository.findByCompanyId(companyId)).willReturn(List.of());

        EmployeeDetailResponse detailResponse = EmployeeDetailResponse.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP-2026-0001")
                .fullName("Nguyễn Văn An")
                .hireDate(LocalDate.of(2026, 1, 15))
                .employmentStatus(EmploymentStatus.PROBATION)
                .build();

        given(employeeService.createEmployee(eq(companyId), any(CreateEmployeeRequest.class)))
                .willReturn(detailResponse);

        // Dòng 1 hợp lệ, dòng 2 thiếu Họ và tên
        byte[] excelBytes = createTestExcel(new Object[][]{
                {1, "EMP-2026-0001", "Nguyễn Văn An", "2026-01-15", "001200001234", "NAM", "1995-05-20",
                        "", "", "IT", "HN", "DEV", "", "", "PROBATION", "FULL_TIME", "", "", "", "", "", "", "", "", "", "", "", "", "FALSE"},
                {2, "EMP-2026-0002", "", "2026-01-15", "001200005678", "NU", "1998-05-20",
                        "", "", "IT", "HN", "DEV", "", "", "PROBATION", "FULL_TIME", "", "", "", "", "", "", "", "", "", "", "", "", "FALSE"}
        });

        MockMultipartFile file = new MockMultipartFile("file", "employees.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        EmployeeImportResponse response = importService.importEmployees(companyId, file, false);

        assertThat(response.getTotalRows()).isEqualTo(2);
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getFailureCount()).isEqualTo(1);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0).getRowNumber()).isEqualTo(3); // row index 2 + 1
        assertThat(response.getErrors().get(0).getReason()).contains("Họ và tên");
    }

    private byte[] createTestExcel(Object[][] rows) throws IOException {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Employees");
            Row header = sheet.createRow(0);
            String[] headers = {
                    "STT", "Mã NV", "Họ và tên", "Ngày vào làm", "Số CCCD", "Giới tính", "Ngày sinh",
                    "Ngày cấp", "Nơi cấp", "Phòng ban", "Chi nhánh", "Chức danh", "Cấp bậc", "Quản lý",
                    "Trạng thái", "Hình thức", "Email công ty", "Email cá nhân", "Số điện thoại",
                    "Mã số thuế", "Số sổ BHXH", "Số tài khoản NH", "Tên ngân hàng", "Chi nhánh NH",
                    "Hôn nhân", "Quốc tịch", "Địa chỉ thường trú", "Địa chỉ hiện tại", "Tạo User"
            };
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r + 1);
                Object[] data = rows[r];
                for (int c = 0; c < data.length; c++) {
                    if (data[c] != null) {
                        row.createCell(c).setCellValue(data[c].toString());
                    }
                }
            }
            wb.write(baos);
            return baos.toByteArray();
        }
    }
}
