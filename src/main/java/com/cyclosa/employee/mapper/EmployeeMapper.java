package com.cyclosa.employee.mapper;

import com.cyclosa.employee.dto.request.CreateDependentRequest;
import com.cyclosa.employee.dto.request.CreateEmergencyContactRequest;
import com.cyclosa.employee.dto.request.CreateEmployeeRequest;
import com.cyclosa.employee.dto.response.EmployeeDependentResponse;
import com.cyclosa.employee.dto.response.EmployeeEmergencyContactResponse;
import com.cyclosa.employee.dto.response.EmployeeHistoryResponse;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.EmployeeDependent;
import com.cyclosa.employee.entity.EmployeeEmergencyContact;
import com.cyclosa.employee.entity.EmployeeEmploymentInfo;
import com.cyclosa.employee.entity.EmployeeHistory;
import com.cyclosa.employee.entity.EmployeePersonalInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "personalInfo", ignore = true)
    @Mapping(target = "employmentInfo", ignore = true)
    @Mapping(target = "dependents", ignore = true)
    @Mapping(target = "emergencyContacts", ignore = true)
    @Mapping(target = "histories", ignore = true)
    Employee toEntity(CreateEmployeeRequest request);

    @Mapping(target = "employee", ignore = true)
    EmployeePersonalInfo toPersonalInfoEntity(CreateEmployeeRequest request);

    @Mapping(target = "employee", ignore = true)
    EmployeeEmploymentInfo toEmploymentInfoEntity(CreateEmployeeRequest request);

    @Mapping(target = "employee", ignore = true)
    EmployeeDependent toDependentEntity(CreateDependentRequest request);

    @Mapping(target = "employee", ignore = true)
    EmployeeEmergencyContact toEmergencyContactEntity(CreateEmergencyContactRequest request);

    @Mapping(target = "employeeId", source = "employee.id")
    EmployeeDependentResponse toDependentResponse(EmployeeDependent dependent);
    List<EmployeeDependentResponse> toDependentResponseList(List<EmployeeDependent> dependents);

    @Mapping(target = "employeeId", source = "employee.id")
    EmployeeEmergencyContactResponse toEmergencyContactResponse(EmployeeEmergencyContact contact);
    List<EmployeeEmergencyContactResponse> toEmergencyContactResponseList(List<EmployeeEmergencyContact> contacts);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "changedByFullName", ignore = true)
    EmployeeHistoryResponse toHistoryResponse(EmployeeHistory history);
    List<EmployeeHistoryResponse> toHistoryResponseList(List<EmployeeHistory> histories);
}
