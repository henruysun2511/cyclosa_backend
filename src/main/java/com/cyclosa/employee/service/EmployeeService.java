package com.cyclosa.employee.service;

import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.dto.request.*;
import com.cyclosa.employee.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    PageData<EmployeeResponse> getEmployees(UUID companyId, EmployeeFilter filter, Pageable pageable);

    EmployeeDetailResponse getEmployeeById(UUID companyId, UUID id);

    EmployeeDetailResponse createEmployee(UUID companyId, CreateEmployeeRequest request);

    EmployeeDetailResponse updatePersonalInfo(UUID companyId, UUID id, UpdatePersonalInfoRequest request);

    EmployeeDetailResponse updateEmploymentInfo(UUID companyId, UUID id, UpdateEmploymentInfoRequest request);

    EmployeeDetailResponse changeStatus(UUID companyId, UUID id, ChangeEmployeeStatusRequest request);

    List<EmployeeDependentResponse> getDependents(UUID companyId, UUID employeeId);

    EmployeeDependentResponse createDependent(UUID companyId, UUID employeeId, CreateDependentRequest request);

    void deleteDependent(UUID companyId, UUID employeeId, UUID dependentId);

    List<EmployeeEmergencyContactResponse> getEmergencyContacts(UUID companyId, UUID employeeId);

    EmployeeEmergencyContactResponse createEmergencyContact(UUID companyId, UUID employeeId, CreateEmergencyContactRequest request);

    void deleteEmergencyContact(UUID companyId, UUID employeeId, UUID contactId);

    List<EmployeeHistoryResponse> getEmployeeHistory(UUID companyId, UUID employeeId);
}
