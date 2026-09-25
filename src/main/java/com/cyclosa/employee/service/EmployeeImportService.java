package com.cyclosa.employee.service;

import com.cyclosa.common.dto.summary.*;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.request.CreateEmployeeRequest;
import com.cyclosa.employee.dto.response.*;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.employee.enums.Gender;
import com.cyclosa.employee.enums.MaritalStatus;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.entity.OrganizationalUnit;
import com.cyclosa.organization.entity.Position;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.JobLevelRepository;
import com.cyclosa.organization.repository.OrganizationalUnitRepository;
import com.cyclosa.organization.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeImportService {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final OrganizationalUnitRepository orgUnitRepository;
    private final BranchRepository branchRepository;
    private final PositionRepository positionRepository;
    private final JobLevelRepository jobLevelRepository;
    private final PlatformTransactionManager transactionManager;

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    /**
     * Nhập danh sách nhân sự hàng loạt từ file Excel.
     */
    public EmployeeImportResponse importEmployees(UUID companyId, MultipartFile file, boolean defaultAutoCreateUser) {
        if (file == null || file.isEmpty()) {
            throw new AppException(EmployeeErrorCode.EMPTY_IMPORT_FILE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new AppException(EmployeeErrorCode.INVALID_IMPORT_FILE);
        }

        UUID effectiveCompanyId = resolveCompanyId(companyId);

        // Pre-cache danh mục tổ chức để tra cứu O(1)
        CompanyLookupData lookupData = loadCompanyLookupData(effectiveCompanyId);

        DataFormatter dataFormatter = new DataFormatter();
        List<EmployeeResponse> successfulEmployees = new ArrayList<>();
        List<EmployeeImportRowError> errors = new ArrayList<>();

        // Cấu hình TransactionTemplate với REQUIRES_NEW cho từng dòng (Partial Import)
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new AppException(EmployeeErrorCode.EMPTY_IMPORT_FILE);
            }

            // Tìm và map header dòng đầu tiên
            Map<String, Integer> headerMap = mapHeaders(sheet.getRow(0), dataFormatter);

            int totalRows = 0;
            int lastRowNum = sheet.getLastRowNum();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                totalRows++;
                int displayRowNumber = r + 1; // 1-based cho người dùng

                ParsedRowData parsed = parseRow(row, headerMap, dataFormatter, lookupData, defaultAutoCreateUser);

                if (!parsed.getFieldErrors().isEmpty()) {
                    errors.add(EmployeeImportRowError.builder()
                            .rowNumber(displayRowNumber)
                            .employeeCode(parsed.getRequest().getEmployeeCode())
                            .fullName(parsed.getRequest().getFullName())
                            .nationalIdNumber(parsed.getRequest().getNationalIdNumber())
                            .reason(String.join("; ", parsed.getFieldErrors()))
                            .fieldErrors(parsed.getFieldErrors())
                            .build());
                    continue;
                }

                CreateEmployeeRequest request = parsed.getRequest();
                request.setCompanyId(effectiveCompanyId);

                try {
                    EmployeeDetailResponse created = txTemplate.execute(status ->
                            employeeService.createEmployee(effectiveCompanyId, request)
                    );

                    if (created != null) {
                        successfulEmployees.add(toSummaryResponse(created));
                    }
                } catch (AppException e) {
                    errors.add(EmployeeImportRowError.builder()
                            .rowNumber(displayRowNumber)
                            .employeeCode(request.getEmployeeCode())
                            .fullName(request.getFullName())
                            .nationalIdNumber(request.getNationalIdNumber())
                            .reason(e.getMessage())
                            .build());
                } catch (Exception e) {
                    log.error("Lỗi bất ngờ khi import dòng {}: {}", displayRowNumber, e.getMessage(), e);
                    errors.add(EmployeeImportRowError.builder()
                            .rowNumber(displayRowNumber)
                            .employeeCode(request.getEmployeeCode())
                            .fullName(request.getFullName())
                            .nationalIdNumber(request.getNationalIdNumber())
                            .reason("Lỗi hệ thống: " + (e.getMessage() != null ? e.getMessage() : "Không xác định"))
                            .build());
                }
            }

            return EmployeeImportResponse.builder()
                    .totalRows(totalRows)
                    .successCount(successfulEmployees.size())
                    .failureCount(errors.size())
                    .successfulEmployees(successfulEmployees)
                    .errors(errors)
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi đọc file Excel import: {}", e.getMessage(), e);
            throw new AppException(EmployeeErrorCode.INVALID_IMPORT_FILE);
        }
    }

    /**
     * Tạo file Excel mẫu chuẩn (.xlsx) với dữ liệu demo và hướng dẫn điền.
     */
    public byte[] generateImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Sheet 1: Mẫu nhập liệu
            Sheet sheet = workbook.createSheet("Danh sách nhân sự");

            // Cell Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle requiredHeaderStyle = workbook.createCellStyle();
            Font reqFont = workbook.createFont();
            reqFont.setBold(true);
            reqFont.setColor(IndexedColors.WHITE.getIndex());
            requiredHeaderStyle.setFont(reqFont);
            requiredHeaderStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            requiredHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            requiredHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            requiredHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            String[] headers = {
                    "STT", "Mã NV (Tùy chọn)", "* Họ và tên", "* Ngày vào làm (yyyy-MM-dd)",
                    "* Số CCCD/Hộ chiếu", "Giới tính (NAM/NU)", "Ngày sinh (yyyy-MM-dd)",
                    "Ngày cấp CCCD", "Nơi cấp CCCD", "* Mã/Tên Phòng ban",
                    "* Mã/Tên Chi nhánh", "* Mã/Tên Chức danh", "Mã/Tên Cấp bậc",
                    "Mã Quản lý", "Trạng thái (PROBATION/ACTIVE)", "Hình thức (FULL_TIME/PART_TIME)",
                    "Email công ty", "Email cá nhân", "Số điện thoại", "Mã số thuế",
                    "Số sổ BHXH", "Số tài khoản NH", "Tên ngân hàng", "Chi nhánh NH",
                    "Hôn nhân (SINGLE/MARRIED)", "Quốc tịch", "Địa chỉ thường trú", "Địa chỉ hiện tại",
                    "Tạo User (TRUE/FALSE)"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                if (headers[i].startsWith("*")) {
                    cell.setCellStyle(requiredHeaderStyle);
                } else {
                    cell.setCellStyle(headerStyle);
                }
            }

            // Dòng dữ liệu mẫu 1
            Row sampleRow1 = sheet.createRow(1);
            setRowValues(sampleRow1, new Object[]{
                    1, "EMP-2026-0001", "Nguyễn Văn An", "2026-01-15",
                    "001200001234", "NAM", "1995-05-20",
                    "2021-06-10", "Cục CSQLHC về TTXH", "PHONG_IT",
                    "CN_HANOI", "DEV_BACKEND", "LEVEL_MID",
                    "", "PROBATION", "FULL_TIME",
                    "an.nguyen@cyclosa.com", "an.nguyen95@gmail.com", "0912345678", "8012345678",
                    "0123456789", "1903666888999", "Techcombank", "Hà Nội",
                    "SINGLE", "Việt Nam", "123 Cầu Giấy, Hà Nội", "123 Cầu Giấy, Hà Nội",
                    "TRUE"
            });

            // Dòng dữ liệu mẫu 2
            Row sampleRow2 = sheet.createRow(2);
            setRowValues(sampleRow2, new Object[]{
                    2, "", "Trần Thị Bình", "2026-02-01",
                    "001200005678", "NU", "1998-11-12",
                    "2022-03-15", "Cục CSQLHC về TTXH", "PHONG_HR",
                    "CN_HANOI", "HR_SPECIALIST", "LEVEL_JUNIOR",
                    "EMP-2026-0001", "ACTIVE", "FULL_TIME",
                    "binh.tran@cyclosa.com", "binhtran.hr@gmail.com", "0987654321", "8087654321",
                    "0987654321", "102888999111", "Vietcombank", "Thăng Long",
                    "MARRIED", "Việt Nam", "456 Giải Phóng, Hà Nội", "456 Giải Phóng, Hà Nội",
                    "FALSE"
            });

            // Tự động căn chỉnh kích thước cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(currentWidth, 3500));
            }

            // Sheet 2: Hướng dẫn nhập
            Sheet guideSheet = workbook.createSheet("Hướng dẫn quy ước");
            Row r0 = guideSheet.createRow(0);
            r0.createCell(0).setCellValue("CỘT");
            r0.createCell(1).setCellValue("QUY TẮC / GIÁ TRỊ HỢP LỆ");

            String[][] guides = {
                    {"* (Màu đỏ)", "Các cột bắt buộc phải có giá trị, không được để trống."},
                    {"Ngày tháng", "Định dạng YYYY-MM-DD hoặc DD/MM/YYYY (VD: 2026-01-15 hoặc 15/01/2026)."},
                    {"Giới tính", "NAM (MALE), NU (FEMALE), KHAC (OTHER)."},
                    {"Số CCCD", "9 hoặc 12 số, không được trùng với nhân sự đã có trên hệ thống."},
                    {"Phòng ban / Chi nhánh / Chức danh", "Có thể điền Mã (Code), Tên hoặc UUID tương ứng trong cơ cấu tổ chức."},
                    {"Trạng thái", "PROBATION (Thử việc), ACTIVE (Chính thức), ON_LEAVE (Nghỉ phép/Tạm hoãn), TERMINATED (Thôi việc)."},
                    {"Hình thức làm việc", "FULL_TIME (Toàn thời gian), PART_TIME (Bán thời gian), CONTRACTOR (Hợp đồng thầu), INTERN (Thực tập sinh)."},
                    {"Hôn nhân", "SINGLE (Độc thân), MARRIED (Đã kết hôn), DIVORCED (Ly hôn), WIDOWED (Góa)."},
                    {"Tạo User", "TRUE hoặc CÓ để tự động tạo tài khoản đăng nhập theo email công ty."}
            };

            for (int i = 0; i < guides.length; i++) {
                Row r = guideSheet.createRow(i + 1);
                r.createCell(0).setCellValue(guides[i][0]);
                r.createCell(1).setCellValue(guides[i][1]);
            }
            guideSheet.autoSizeColumn(0);
            guideSheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Lỗi khi tạo file Excel template: {}", e.getMessage(), e);
            throw new AppException(EmployeeErrorCode.IMPORT_FAILED);
        }
    }

    private void setRowValues(Row row, Object[] values) {
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            if (values[i] instanceof Number num) {
                cell.setCellValue(num.doubleValue());
            } else if (values[i] != null) {
                cell.setCellValue(values[i].toString());
            }
        }
    }

    private UUID resolveCompanyId(UUID companyId) {
        if (companyId != null) {
            return companyId;
        }
        Optional<UUID> currentUserId = SecurityUtils.getCurrentUserIdOptional();
        if (currentUserId.isPresent()) {
            Optional<Employee> currentEmp = employeeRepository.findByUserId(currentUserId.get());
            if (currentEmp.isPresent() && currentEmp.get().getCompanyId() != null) {
                return currentEmp.get().getCompanyId();
            }
        }
        throw AppException.badRequest("Yêu cầu cung cấp ID công ty (header X-Company-Id)");
    }

    private CompanyLookupData loadCompanyLookupData(UUID companyId) {
        CompanyLookupData data = new CompanyLookupData();

        // 1. Org Units
        List<OrganizationalUnit> units = orgUnitRepository.findAllByCompanyIdWithDetails(companyId);
        for (OrganizationalUnit u : units) {
            data.orgUnitsById.put(u.getId().toString(), u.getId());
            if (u.getCode() != null) data.orgUnitsByCode.put(u.getCode().toUpperCase().trim(), u.getId());
            if (u.getName() != null) data.orgUnitsByName.put(u.getName().toLowerCase().trim(), u.getId());
        }

        // 2. Branches
        List<Branch> branches = branchRepository.findByCompanyIdWithRegion(companyId);
        for (Branch b : branches) {
            data.branchesById.put(b.getId().toString(), b.getId());
            if (b.getCode() != null) data.branchesByCode.put(b.getCode().toUpperCase().trim(), b.getId());
            if (b.getName() != null) data.branchesByName.put(b.getName().toLowerCase().trim(), b.getId());
        }

        // 3. Positions
        List<Position> positions = positionRepository.findByCompanyId(companyId);
        for (Position p : positions) {
            data.positionsById.put(p.getId().toString(), p.getId());
            if (p.getCode() != null) data.positionsByCode.put(p.getCode().toUpperCase().trim(), p.getId());
            if (p.getName() != null) data.positionsByName.put(p.getName().toLowerCase().trim(), p.getId());
        }

        // 4. Job Levels
        List<JobLevel> levels = jobLevelRepository.findByCompanyIdOrderByRankOrderAsc(companyId);
        for (JobLevel l : levels) {
            data.jobLevelsById.put(l.getId().toString(), l.getId());
            if (l.getCode() != null) data.jobLevelsByCode.put(l.getCode().toUpperCase().trim(), l.getId());
            if (l.getName() != null) data.jobLevelsByName.put(l.getName().toLowerCase().trim(), l.getId());
        }

        // 5. Existing Employees for manager lookup
        List<Employee> emps = employeeRepository.findByCompanyId(companyId);
        for (Employee e : emps) {
            data.managersById.put(e.getId().toString(), e.getId());
            if (e.getEmployeeCode() != null) data.managersByCode.put(e.getEmployeeCode().toUpperCase().trim(), e.getId());
            if (e.getEmploymentInfo() != null && e.getEmploymentInfo().getCompanyEmail() != null) {
                data.managersByEmail.put(e.getEmploymentInfo().getCompanyEmail().toLowerCase().trim(), e.getId());
            }
        }

        return data;
    }

    private Map<String, Integer> mapHeaders(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
            String text = formatter.formatCellValue(cell).trim().toLowerCase()
                    .replace("*", "").trim();

            // Kiểm tra các trường cụ thể / ghép từ trước để tránh conflict với từ đơn
            if (text.contains("mã nv") || text.contains("mã nhân viên") || text.equals("code") || text.contains("employee code")) {
                map.put("EMPLOYEE_CODE", i);
            } else if (text.contains("họ và tên") || text.contains("họ tên") || text.contains("full name") || text.equals("fullname") || text.equals("name")) {
                map.put("FULL_NAME", i);
            } else if (text.contains("ngày vào làm") || text.contains("ngay vao lam") || text.contains("hire date") || text.contains("hiredate")) {
                map.put("HIRE_DATE", i);
            } else if (text.contains("cccd") || text.contains("cmnd") || text.contains("hộ chiếu") || text.contains("national id")) {
                map.put("NATIONAL_ID", i);
            } else if (text.contains("ngày cấp") || text.contains("issue date")) {
                map.put("ID_ISSUE_DATE", i);
            } else if (text.contains("nơi cấp") || text.contains("issue place")) {
                map.put("ID_ISSUE_PLACE", i);
            } else if (text.contains("ngày sinh") || text.contains("dob") || text.contains("date of birth")) {
                map.put("DOB", i);
            } else if (text.contains("giới tính") || text.contains("gender") || text.contains("sex")) {
                map.put("GENDER", i);
            } else if (text.contains("chi nhánh nh") || text.contains("chi nhánh ngân hàng") || text.contains("bank branch")) {
                map.put("BANK_BRANCH", i);
            } else if (text.contains("tài khoản nh") || text.contains("tài khoản ngân hàng") || text.contains("số tài khoản") || text.contains("bank account")) {
                map.put("BANK_ACCOUNT", i);
            } else if (text.contains("tên ngân hàng") || text.contains("ngân hàng") || text.contains("bank name")) {
                map.put("BANK_NAME", i);
            } else if (text.contains("chi nhánh") || text.contains("branch")) {
                map.put("BRANCH", i);
            } else if (text.contains("phòng ban") || text.contains("đơn vị") || text.contains("department") || text.contains("unit")) {
                map.put("ORG_UNIT", i);
            } else if (text.contains("chức danh") || text.contains("vị trí") || text.contains("position") || text.contains("job title")) {
                map.put("POSITION", i);
            } else if (text.contains("cấp bậc") || text.contains("job level") || text.contains("level")) {
                map.put("JOB_LEVEL", i);
            } else if (text.contains("quản lý") || text.contains("manager")) {
                map.put("MANAGER", i);
            } else if (text.contains("loại hình") || text.contains("hình thức") || text.contains("employment type")) {
                map.put("EMPLOYMENT_TYPE", i);
            } else if (text.contains("thử việc") && (text.contains("hết hạn") || text.contains("kết thúc") || text.contains("probation end"))) {
                map.put("PROBATION_END_DATE", i);
            } else if (text.contains("trạng thái") || text.contains("status")) {
                map.put("STATUS", i);
            } else if (text.contains("email công ty") || text.contains("company email") || text.contains("work email")) {
                map.put("COMPANY_EMAIL", i);
            } else if (text.contains("email cá nhân") || text.contains("personal email") || text.equals("email")) {
                map.put("PERSONAL_EMAIL", i);
            } else if (text.contains("số điện thoại") || text.contains("phone") || text.contains("sđt") || text.contains("sdt")) {
                map.put("PHONE", i);
            } else if (text.contains("mã số thuế") || text.contains("tax code") || text.contains("mst")) {
                map.put("TAX_CODE", i);
            } else if (text.contains("sổ bhxh") || text.contains("bhxh") || text.contains("social insurance")) {
                map.put("SOCIAL_INSURANCE", i);
            } else if (text.contains("hôn nhân") || text.contains("marital")) {
                map.put("MARITAL_STATUS", i);
            } else if (text.contains("quốc tịch") || text.contains("nationality")) {
                map.put("NATIONALITY", i);
            } else if (text.contains("thường trú") || text.contains("permanent address")) {
                map.put("PERMANENT_ADDRESS", i);
            } else if (text.contains("hiện tại") || text.contains("current address")) {
                map.put("CURRENT_ADDRESS", i);
            } else if (text.contains("địa điểm làm việc") || text.contains("work location")) {
                map.put("WORK_LOCATION", i);
            } else if (text.contains("tạo user") || text.contains("tài khoản user") || text.contains("auto create user")) {
                map.put("AUTO_CREATE_USER", i);
            }
        }
        return map;
    }

    private ParsedRowData parseRow(Row row, Map<String, Integer> headerMap, DataFormatter formatter,
                                   CompanyLookupData lookup, boolean defaultAutoCreateUser) {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        List<String> fieldErrors = new ArrayList<>();

        // 1. Employee Code
        String empCode = getCellValue(row, headerMap, "EMPLOYEE_CODE", 1, formatter);
        req.setEmployeeCode(empCode);

        // 2. Full Name (Required)
        String fullName = getCellValue(row, headerMap, "FULL_NAME", 2, formatter);
        if (fullName == null || fullName.isBlank()) {
            fieldErrors.add("Họ và tên nhân viên không được để trống");
        } else {
            req.setFullName(fullName.trim());
        }

        // 3. Hire Date (Required)
        LocalDate hireDate = getCellDateValue(row, headerMap, "HIRE_DATE", 3, formatter);
        if (hireDate == null) {
            fieldErrors.add("Ngày vào làm không được để trống hoặc sai định dạng (YYYY-MM-DD)");
        } else {
            req.setHireDate(hireDate);
        }

        // 4. National ID Number (Required)
        String nationalId = getCellValue(row, headerMap, "NATIONAL_ID", 4, formatter);
        if (nationalId == null || nationalId.isBlank()) {
            fieldErrors.add("Số CCCD/Hộ chiếu không được để trống");
        } else {
            req.setNationalIdNumber(nationalId.trim());
        }

        // 5. Gender
        String genderStr = getCellValue(row, headerMap, "GENDER", 5, formatter);
        req.setGender(parseGender(genderStr));

        // 6. Date of Birth
        LocalDate dob = getCellDateValue(row, headerMap, "DOB", 6, formatter);
        req.setDateOfBirth(dob);

        // 7. National ID Issue Date & Place
        req.setNationalIdIssueDate(getCellDateValue(row, headerMap, "ID_ISSUE_DATE", 7, formatter));
        req.setNationalIdIssuePlace(getCellValue(row, headerMap, "ID_ISSUE_PLACE", 8, formatter));

        // 8. Organizational Unit (Required)
        String orgUnitVal = getCellValue(row, headerMap, "ORG_UNIT", 9, formatter);
        if (orgUnitVal == null || orgUnitVal.isBlank()) {
            fieldErrors.add("Phòng ban / đơn vị tổ chức không được để trống");
        } else {
            UUID unitId = lookup.resolveOrgUnitId(orgUnitVal);
            if (unitId == null) {
                fieldErrors.add("Không tìm thấy Phòng ban với mã/tên: '" + orgUnitVal + "'");
            } else {
                req.setOrganizationalUnitId(unitId);
            }
        }

        // 9. Branch (Required)
        String branchVal = getCellValue(row, headerMap, "BRANCH", 10, formatter);
        if (branchVal == null || branchVal.isBlank()) {
            fieldErrors.add("Chi nhánh làm việc không được để trống");
        } else {
            UUID branchId = lookup.resolveBranchId(branchVal);
            if (branchId == null) {
                fieldErrors.add("Không tìm thấy Chi nhánh với mã/tên: '" + branchVal + "'");
            } else {
                req.setBranchId(branchId);
            }
        }

        // 10. Position (Required)
        String posVal = getCellValue(row, headerMap, "POSITION", 11, formatter);
        if (posVal == null || posVal.isBlank()) {
            fieldErrors.add("Chức danh / vị trí việc làm không được để trống");
        } else {
            UUID posId = lookup.resolvePositionId(posVal);
            if (posId == null) {
                fieldErrors.add("Không tìm thấy Chức danh với mã/tên: '" + posVal + "'");
            } else {
                req.setPositionId(posId);
            }
        }

        // 11. Job Level (Optional)
        String levelVal = getCellValue(row, headerMap, "JOB_LEVEL", 12, formatter);
        if (levelVal != null && !levelVal.isBlank()) {
            UUID levelId = lookup.resolveJobLevelId(levelVal);
            req.setJobLevelId(levelId);
        }

        // 12. Manager (Optional)
        String managerVal = getCellValue(row, headerMap, "MANAGER", 13, formatter);
        if (managerVal != null && !managerVal.isBlank()) {
            UUID managerId = lookup.resolveManagerId(managerVal);
            req.setManagerEmployeeId(managerId);
        }

        // 13. Status
        String statusStr = getCellValue(row, headerMap, "STATUS", 14, formatter);
        req.setEmploymentStatus(parseStatus(statusStr));

        // 14. Employment Type
        String typeStr = getCellValue(row, headerMap, "EMPLOYMENT_TYPE", 15, formatter);
        req.setEmploymentType(parseType(typeStr));

        // 15. Other fields
        req.setCompanyEmail(getCellValue(row, headerMap, "COMPANY_EMAIL", 16, formatter));
        req.setPersonalEmail(getCellValue(row, headerMap, "PERSONAL_EMAIL", 17, formatter));
        req.setPhone(getCellValue(row, headerMap, "PHONE", 18, formatter));
        req.setTaxCode(getCellValue(row, headerMap, "TAX_CODE", 19, formatter));
        req.setSocialInsuranceNumber(getCellValue(row, headerMap, "SOCIAL_INSURANCE", 20, formatter));
        req.setBankAccountNumber(getCellValue(row, headerMap, "BANK_ACCOUNT", 21, formatter));
        req.setBankName(getCellValue(row, headerMap, "BANK_NAME", 22, formatter));
        req.setBankBranch(getCellValue(row, headerMap, "BANK_BRANCH", 23, formatter));
        req.setMaritalStatus(parseMaritalStatus(getCellValue(row, headerMap, "MARITAL_STATUS", 24, formatter)));

        String nationality = getCellValue(row, headerMap, "NATIONALITY", 25, formatter);
        if (nationality != null && !nationality.isBlank()) {
            req.setNationality(nationality.trim());
        }

        req.setPermanentAddress(getCellValue(row, headerMap, "PERMANENT_ADDRESS", 26, formatter));
        req.setCurrentAddress(getCellValue(row, headerMap, "CURRENT_ADDRESS", 27, formatter));
        req.setWorkLocation(getCellValue(row, headerMap, "WORK_LOCATION", -1, formatter));
        req.setProbationEndDate(getCellDateValue(row, headerMap, "PROBATION_END_DATE", -1, formatter));

        // Auto Create User
        String autoUserStr = getCellValue(row, headerMap, "AUTO_CREATE_USER", 28, formatter);
        if (autoUserStr != null && !autoUserStr.isBlank()) {
            String lower = autoUserStr.toLowerCase().trim();
            req.setAutoCreateUser(lower.equals("true") || lower.equals("yes") || lower.equals("có") || lower.equals("1"));
        } else {
            req.setAutoCreateUser(defaultAutoCreateUser);
        }

        return new ParsedRowData(req, fieldErrors);
    }

    private String getCellValue(Row row, Map<String, Integer> headerMap, String key, int defaultCol, DataFormatter formatter) {
        Integer colIndex = headerMap.get(key);
        if (colIndex == null) {
            colIndex = defaultCol >= 0 && defaultCol < row.getLastCellNum() ? defaultCol : null;
        }
        if (colIndex == null) return null;

        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && !DateUtil.isCellDateFormatted(cell)) {
            // Tránh chuyển số CCCD hoặc mã số sang scientific notation (1.23E9)
            double val = cell.getNumericCellValue();
            if (val == (long) val) {
                return String.valueOf((long) val);
            }
        }
        String formatted = formatter.formatCellValue(cell);
        return (formatted != null && !formatted.isBlank()) ? formatted.trim() : null;
    }

    private LocalDate getCellDateValue(Row row, Map<String, Integer> headerMap, String key, int defaultCol, DataFormatter formatter) {
        Integer colIndex = headerMap.get(key);
        if (colIndex == null) {
            colIndex = defaultCol >= 0 && defaultCol < row.getLastCellNum() ? defaultCol : null;
        }
        if (colIndex == null) return null;

        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String str = formatter.formatCellValue(cell);
        if (str == null || str.isBlank()) return null;

        str = str.trim();
        for (DateTimeFormatter dtf : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(str, dtf);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Gender parseGender(String str) {
        if (str == null || str.isBlank()) return null;
        String s = str.trim().toUpperCase();
        if (s.equals("NAM") || s.equals("MALE") || s.equals("M")) return Gender.MALE;
        if (s.equals("NỮ") || s.equals("NU") || s.equals("FEMALE") || s.equals("F")) return Gender.FEMALE;
        if (s.equals("KHÁC") || s.equals("KHAC") || s.equals("OTHER")) return Gender.OTHER;
        return null;
    }

    private EmploymentStatus parseStatus(String str) {
        if (str == null || str.isBlank()) return EmploymentStatus.PROBATION;
        String s = str.trim().toUpperCase();
        if (s.contains("CHÍNH THỨC") || s.contains("CHINH THUC") || s.equals("ACTIVE")) return EmploymentStatus.ACTIVE;
        if (s.contains("THỬ VIỆC") || s.contains("THU VIEC") || s.equals("PROBATION")) return EmploymentStatus.PROBATION;
        if (s.contains("NGHỈ VIỆC") || s.contains("NGHI VIEC") || s.equals("TERMINATED")) return EmploymentStatus.TERMINATED;
        if (s.contains("TẠM HOÃN") || s.contains("TAM HOAN") || s.equals("ON_LEAVE")) return EmploymentStatus.ON_LEAVE;
        return EmploymentStatus.PROBATION;
    }

    private EmploymentType parseType(String str) {
        if (str == null || str.isBlank()) return EmploymentType.FULL_TIME;
        String s = str.trim().toUpperCase();
        if (s.contains("BÁN THỜI GIAN") || s.contains("BAN THOI GIAN") || s.equals("PART_TIME")) return EmploymentType.PART_TIME;
        if (s.contains("HỢP ĐỒNG") || s.contains("HOP DONG") || s.equals("CONTRACTOR")) return EmploymentType.CONTRACTOR;
        if (s.contains("THỰC TẬP") || s.contains("THUC TAP") || s.equals("INTERN") || s.equals("INTERNSHIP")) return EmploymentType.INTERNSHIP;
        return EmploymentType.FULL_TIME;
    }

    private MaritalStatus parseMaritalStatus(String str) {
        if (str == null || str.isBlank()) return MaritalStatus.SINGLE;
        String s = str.trim().toUpperCase();
        if (s.contains("KẾT HÔN") || s.contains("KET HON") || s.equals("MARRIED")) return MaritalStatus.MARRIED;
        if (s.contains("LY HÔN") || s.contains("LY HON") || s.equals("DIVORCED")) return MaritalStatus.DIVORCED;
        if (s.contains("GÓA") || s.contains("GOA") || s.equals("WIDOWED")) return MaritalStatus.WIDOWED;
        return MaritalStatus.SINGLE;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private EmployeeResponse toSummaryResponse(EmployeeDetailResponse detail) {
        return EmployeeResponse.builder()
                .id(detail.getId())
                .employeeCode(detail.getEmployeeCode())
                .company(detail.getCompany())
                .userId(detail.getUserId())
                .fullName(detail.getFullName())
                .hireDate(detail.getHireDate())
                .employmentStatus(detail.getEmploymentStatus())
                .gender(detail.getGender())
                .dateOfBirth(detail.getDateOfBirth())
                .phone(detail.getPhone())
                .personalEmail(detail.getPersonalEmail())
                .photoUrl(detail.getPhotoUrl())
                .organizationalUnit(detail.getOrganizationalUnit())
                .branch(detail.getBranch())
                .position(detail.getPosition())
                .manager(detail.getManager())
                .employmentType(detail.getEmploymentType())
                .companyEmail(detail.getCompanyEmail())
                .createdAt(detail.getCreatedAt())
                .build();
    }

    private static class ParsedRowData {
        private final CreateEmployeeRequest request;
        private final List<String> fieldErrors;

        public ParsedRowData(CreateEmployeeRequest request, List<String> fieldErrors) {
            this.request = request;
            this.fieldErrors = fieldErrors;
        }

        public CreateEmployeeRequest getRequest() {
            return request;
        }

        public List<String> getFieldErrors() {
            return fieldErrors;
        }
    }

    private static class CompanyLookupData {
        final Map<String, UUID> orgUnitsById = new HashMap<>();
        final Map<String, UUID> orgUnitsByCode = new HashMap<>();
        final Map<String, UUID> orgUnitsByName = new HashMap<>();

        final Map<String, UUID> branchesById = new HashMap<>();
        final Map<String, UUID> branchesByCode = new HashMap<>();
        final Map<String, UUID> branchesByName = new HashMap<>();

        final Map<String, UUID> positionsById = new HashMap<>();
        final Map<String, UUID> positionsByCode = new HashMap<>();
        final Map<String, UUID> positionsByName = new HashMap<>();

        final Map<String, UUID> jobLevelsById = new HashMap<>();
        final Map<String, UUID> jobLevelsByCode = new HashMap<>();
        final Map<String, UUID> jobLevelsByName = new HashMap<>();

        final Map<String, UUID> managersById = new HashMap<>();
        final Map<String, UUID> managersByCode = new HashMap<>();
        final Map<String, UUID> managersByEmail = new HashMap<>();

        UUID resolveOrgUnitId(String val) {
            if (val == null) return null;
            String trimmed = val.trim();
            if (orgUnitsById.containsKey(trimmed)) return orgUnitsById.get(trimmed);
            if (orgUnitsByCode.containsKey(trimmed.toUpperCase())) return orgUnitsByCode.get(trimmed.toUpperCase());
            if (orgUnitsByName.containsKey(trimmed.toLowerCase())) return orgUnitsByName.get(trimmed.toLowerCase());
            return null;
        }

        UUID resolveBranchId(String val) {
            if (val == null) return null;
            String trimmed = val.trim();
            if (branchesById.containsKey(trimmed)) return branchesById.get(trimmed);
            if (branchesByCode.containsKey(trimmed.toUpperCase())) return branchesByCode.get(trimmed.toUpperCase());
            if (branchesByName.containsKey(trimmed.toLowerCase())) return branchesByName.get(trimmed.toLowerCase());
            return null;
        }

        UUID resolvePositionId(String val) {
            if (val == null) return null;
            String trimmed = val.trim();
            if (positionsById.containsKey(trimmed)) return positionsById.get(trimmed);
            if (positionsByCode.containsKey(trimmed.toUpperCase())) return positionsByCode.get(trimmed.toUpperCase());
            if (positionsByName.containsKey(trimmed.toLowerCase())) return positionsByName.get(trimmed.toLowerCase());
            return null;
        }

        UUID resolveJobLevelId(String val) {
            if (val == null) return null;
            String trimmed = val.trim();
            if (jobLevelsById.containsKey(trimmed)) return jobLevelsById.get(trimmed);
            if (jobLevelsByCode.containsKey(trimmed.toUpperCase())) return jobLevelsByCode.get(trimmed.toUpperCase());
            if (jobLevelsByName.containsKey(trimmed.toLowerCase())) return jobLevelsByName.get(trimmed.toLowerCase());
            return null;
        }

        UUID resolveManagerId(String val) {
            if (val == null) return null;
            String trimmed = val.trim();
            if (managersById.containsKey(trimmed)) return managersById.get(trimmed);
            if (managersByCode.containsKey(trimmed.toUpperCase())) return managersByCode.get(trimmed.toUpperCase());
            if (managersByEmail.containsKey(trimmed.toLowerCase())) return managersByEmail.get(trimmed.toLowerCase());
            return null;
        }
    }
}
