package com.cyclosa.employee.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.request.CreateIdentityDocumentRequest;
import com.cyclosa.employee.dto.request.UpdateBankAccountRequest;
import com.cyclosa.employee.dto.request.UpdateIdentityDocumentRequest;
import com.cyclosa.employee.dto.response.BankAccountResponse;
import com.cyclosa.employee.dto.response.IdentityDocumentResponse;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.EmployeeBankAccount;
import com.cyclosa.employee.entity.EmployeeIdentityDocument;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.mapper.EmployeeMapper;
import com.cyclosa.employee.repository.EmployeeBankAccountRepository;
import com.cyclosa.employee.repository.EmployeeIdentityDocumentRepository;
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
public class EmployeeDocumentService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeIdentityDocumentRepository identityDocumentRepository;
    private final EmployeeBankAccountRepository bankAccountRepository;
    private final EmployeeMapper employeeMapper;

    public List<IdentityDocumentResponse> getIdentityDocuments(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        return identityDocumentRepository.findAllByEmployeeId(employeeId)
                .stream()
                .map(employeeMapper::toIdentityDocumentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IdentityDocumentResponse createIdentityDocument(UUID employeeId, CreateIdentityDocumentRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        if (identityDocumentRepository.existsByDocumentTypeAndDocumentNumber(
                request.getDocumentType(), request.getDocumentNumber().trim())) {
            throw new AppException(EmployeeErrorCode.IDENTITY_DOCUMENT_EXISTS);
        }

        EmployeeIdentityDocument doc = EmployeeIdentityDocument.builder()
                .employee(employee)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber().trim())
                .issueDate(request.getIssueDate())
                .issuePlace(request.getIssuePlace().trim())
                .expiryDate(request.getExpiryDate())
                .build();

        EmployeeIdentityDocument saved = identityDocumentRepository.save(doc);
        return employeeMapper.toIdentityDocumentResponse(saved);
    }

    @Transactional
    public IdentityDocumentResponse updateIdentityDocument(UUID documentId, UpdateIdentityDocumentRequest request) {
        EmployeeIdentityDocument doc = identityDocumentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.IDENTITY_DOCUMENT_NOT_FOUND));

        if (identityDocumentRepository.existsByDocumentTypeAndDocumentNumberAndIdNot(
                request.getDocumentType(), request.getDocumentNumber().trim(), documentId)) {
            throw new AppException(EmployeeErrorCode.IDENTITY_DOCUMENT_EXISTS);
        }

        doc.setDocumentType(request.getDocumentType());
        doc.setDocumentNumber(request.getDocumentNumber().trim());
        doc.setIssueDate(request.getIssueDate());
        doc.setIssuePlace(request.getIssuePlace().trim());
        doc.setExpiryDate(request.getExpiryDate());

        EmployeeIdentityDocument saved = identityDocumentRepository.save(doc);
        return employeeMapper.toIdentityDocumentResponse(saved);
    }

    @Transactional
    public void deleteIdentityDocument(UUID documentId) {
        if (!identityDocumentRepository.existsById(documentId)) {
            throw new AppException(EmployeeErrorCode.IDENTITY_DOCUMENT_NOT_FOUND);
        }
        identityDocumentRepository.deleteById(documentId);
    }

    public BankAccountResponse getBankAccount(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        EmployeeBankAccount account = bankAccountRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.BANK_ACCOUNT_NOT_FOUND));
        return employeeMapper.toBankAccountResponse(account);
    }

    @Transactional
    public BankAccountResponse updateBankAccount(UUID employeeId, UpdateBankAccountRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeBankAccount account = bankAccountRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> EmployeeBankAccount.builder()
                        .employee(employee)
                        .build());

        account.setBankName(request.getBankName().trim());
        account.setBankBranchName(request.getBankBranchName() != null ? request.getBankBranchName().trim() : null);
        account.setAccountNumber(request.getAccountNumber().trim());

        EmployeeBankAccount saved = bankAccountRepository.save(account);
        return employeeMapper.toBankAccountResponse(saved);
    }
}

