package com.cyclosa.employee.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.request.CreateDependentRequest;
import com.cyclosa.employee.dto.request.CreateEmergencyContactRequest;
import com.cyclosa.employee.dto.response.DependentResponse;
import com.cyclosa.employee.dto.response.EmergencyContactResponse;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.EmployeeDependent;
import com.cyclosa.employee.entity.EmployeeEmergencyContact;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.mapper.EmployeeMapper;
import com.cyclosa.employee.repository.EmployeeDependentRepository;
import com.cyclosa.employee.repository.EmployeeEmergencyContactRepository;
import com.cyclosa.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeFamilyService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDependentRepository dependentRepository;
    private final EmployeeEmergencyContactRepository emergencyContactRepository;
    private final EmployeeMapper employeeMapper;

    public List<DependentResponse> getDependents(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        return dependentRepository.findAllByEmployeeId(employeeId)
                .stream()
                .map(employeeMapper::toDependentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DependentResponse createDependent(UUID employeeId, CreateDependentRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        if (request.getTaxDeductionFrom() != null && request.getTaxDeductionTo() != null) {
            if (request.getTaxDeductionTo().isBefore(request.getTaxDeductionFrom())) {
                throw new AppException(EmployeeErrorCode.DEDUCTION_DATE_INVALID);
            }
        }

        EmployeeDependent dependent = EmployeeDependent.builder()
                .employee(employee)
                .fullName(request.getFullName().trim())
                .dateOfBirth(request.getDateOfBirth())
                .relationship(request.getRelationship())
                .identityNumber(request.getIdentityNumber() != null ? request.getIdentityNumber().trim() : null)
                .taxDeductionFrom(request.getTaxDeductionFrom())
                .taxDeductionTo(request.getTaxDeductionTo())
                .build();

        EmployeeDependent saved = dependentRepository.save(dependent);
        return employeeMapper.toDependentResponse(saved);
    }

    @Transactional
    public DependentResponse updateDependent(UUID dependentId, CreateDependentRequest request) {
        EmployeeDependent dependent = dependentRepository.findById(dependentId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.DEPENDENT_NOT_FOUND));

        if (request.getTaxDeductionFrom() != null && request.getTaxDeductionTo() != null) {
            if (request.getTaxDeductionTo().isBefore(request.getTaxDeductionFrom())) {
                throw new AppException(EmployeeErrorCode.DEDUCTION_DATE_INVALID);
            }
        }

        dependent.setFullName(request.getFullName().trim());
        dependent.setDateOfBirth(request.getDateOfBirth());
        dependent.setRelationship(request.getRelationship());
        dependent.setIdentityNumber(request.getIdentityNumber() != null ? request.getIdentityNumber().trim() : null);
        dependent.setTaxDeductionFrom(request.getTaxDeductionFrom());
        dependent.setTaxDeductionTo(request.getTaxDeductionTo());

        EmployeeDependent saved = dependentRepository.save(dependent);
        return employeeMapper.toDependentResponse(saved);
    }

    @Transactional
    public void deleteDependent(UUID dependentId) {
        if (!dependentRepository.existsById(dependentId)) {
            throw new AppException(EmployeeErrorCode.DEPENDENT_NOT_FOUND);
        }
        dependentRepository.deleteById(dependentId);
    }

    public List<EmergencyContactResponse> getEmergencyContacts(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        return emergencyContactRepository.findAllByEmployeeId(employeeId)
                .stream()
                .map(employeeMapper::toEmergencyContactResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmergencyContactResponse createEmergencyContact(UUID employeeId, CreateEmergencyContactRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeEmergencyContact contact = EmployeeEmergencyContact.builder()
                .employee(employee)
                .fullName(request.getFullName().trim())
                .relationship(request.getRelationship().trim())
                .phone(request.getPhone().trim())
                .address(request.getAddress())
                .build();

        EmployeeEmergencyContact saved = emergencyContactRepository.save(contact);
        return employeeMapper.toEmergencyContactResponse(saved);
    }

    @Transactional
    public EmergencyContactResponse updateEmergencyContact(UUID contactId, CreateEmergencyContactRequest request) {
        EmployeeEmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMERGENCY_CONTACT_NOT_FOUND));

        contact.setFullName(request.getFullName().trim());
        contact.setRelationship(request.getRelationship().trim());
        contact.setPhone(request.getPhone().trim());
        contact.setAddress(request.getAddress());

        EmployeeEmergencyContact saved = emergencyContactRepository.save(contact);
        return employeeMapper.toEmergencyContactResponse(saved);
    }

    @Transactional
    public void deleteEmergencyContact(UUID contactId) {
        if (!emergencyContactRepository.existsById(contactId)) {
            throw new AppException(EmployeeErrorCode.EMERGENCY_CONTACT_NOT_FOUND);
        }
        emergencyContactRepository.deleteById(contactId);
    }
}

