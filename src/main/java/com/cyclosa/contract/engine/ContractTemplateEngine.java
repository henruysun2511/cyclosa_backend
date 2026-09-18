package com.cyclosa.contract.engine;

import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.util.VietnameseNumberToWordsConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Engine thực hiện Mail-Merge thay thế các biến giữ chỗ trong Mẫu hợp đồng
 * thành nội dung văn bản hoàn chỉnh.
 */
@Slf4j
@Component
public class ContractTemplateEngine {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("#,###");

    /**
     * Render văn bản hợp đồng bằng cách thay thế các biến placeholder {{...}}.
     */
    public String render(String templateContent, Map<String, String> variables) {
        if (templateContent == null) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return templateContent;
        }

        String result = templateContent;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * Xây dựng tập biến chuẩn hóa từ đối tượng Contract và các thông tin liên quan.
     */
    public Map<String, String> buildStandardVariables(Contract contract,
                                                      String employeeName,
                                                      String employeeCode,
                                                      String nationalId,
                                                      String nationalIdIssueDate,
                                                      String nationalIdIssuePlace,
                                                      String permanentAddress,
                                                      String currentAddress,
                                                      String phoneNumber,
                                                      String positionTitle,
                                                      String departmentName,
                                                      String companyName,
                                                      String companyTaxCode,
                                                      String companyAddress,
                                                      String representativeName,
                                                      String representativeTitle) {
        Map<String, String> vars = new HashMap<>();

        // Thông tin hợp đồng
        vars.put("contract_number", contract.getContractNumber());
        vars.put("contract_type_desc", contract.getContractType().getDescription());
        vars.put("start_date", contract.getStartDate() != null ? contract.getStartDate().format(DATE_FORMATTER) : "");
        vars.put("end_date", contract.getEndDate() != null ? contract.getEndDate().format(DATE_FORMATTER) : "Không xác định thời hạn");
        vars.put("sign_date", contract.getSignDate() != null ? contract.getSignDate().format(DATE_FORMATTER) : "");
        vars.put("working_hours", contract.getWorkingHoursType() != null ? contract.getWorkingHoursType().getDescription() : "");
        vars.put("work_location", contract.getWorkLocationAddress() != null ? contract.getWorkLocationAddress() : "");

        // Tiền lương và phụ cấp
        if (contract.getBasicSalary() != null) {
            vars.put("basic_salary", CURRENCY_FORMATTER.format(contract.getBasicSalary()) + " VNĐ");
            vars.put("basic_salary_words", VietnameseNumberToWordsConverter.convertToWords(contract.getBasicSalary()));
        } else {
            vars.put("basic_salary", "0 VNĐ");
            vars.put("basic_salary_words", "Không đồng chẵn");
        }

        if (contract.getInsuranceSalary() != null) {
            vars.put("insurance_salary", CURRENCY_FORMATTER.format(contract.getInsuranceSalary()) + " VNĐ");
        } else {
            vars.put("insurance_salary", vars.get("basic_salary"));
        }

        vars.put("allowance_lunch", contract.getAllowanceLunch() != null ? CURRENCY_FORMATTER.format(contract.getAllowanceLunch()) + " VNĐ" : "0 VNĐ");
        vars.put("allowance_phone", contract.getAllowancePhone() != null ? CURRENCY_FORMATTER.format(contract.getAllowancePhone()) + " VNĐ" : "0 VNĐ");
        vars.put("allowance_transport", contract.getAllowanceTransport() != null ? CURRENCY_FORMATTER.format(contract.getAllowanceTransport()) + " VNĐ" : "0 VNĐ");
        vars.put("allowance_other", contract.getAllowanceOther() != null ? CURRENCY_FORMATTER.format(contract.getAllowanceOther()) + " VNĐ" : "0 VNĐ");

        // Người lao động
        vars.put("employee_name", employeeName != null ? employeeName : "");
        vars.put("employee_code", employeeCode != null ? employeeCode : "");
        vars.put("national_id", nationalId != null ? nationalId : "");
        vars.put("national_id_issue_date", nationalIdIssueDate != null ? nationalIdIssueDate : "");
        vars.put("national_id_issue_place", nationalIdIssuePlace != null ? nationalIdIssuePlace : "");
        vars.put("permanent_address", permanentAddress != null ? permanentAddress : "");
        vars.put("current_address", currentAddress != null ? currentAddress : "");
        vars.put("phone_number", phoneNumber != null ? phoneNumber : "");
        vars.put("position_title", positionTitle != null ? positionTitle : "");
        vars.put("department_name", departmentName != null ? departmentName : "");

        // Người sử dụng lao động
        vars.put("company_name", companyName != null ? companyName : "");
        vars.put("company_tax_code", companyTaxCode != null ? companyTaxCode : "");
        vars.put("company_address", companyAddress != null ? companyAddress : "");
        vars.put("company_representative", representativeName != null ? representativeName : "");
        vars.put("representative_title", representativeTitle != null ? representativeTitle : "");

        return vars;
    }
}
