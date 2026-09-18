package com.cyclosa.workflow.service;

import com.cyclosa.workflow.entity.WorkflowStep;

import java.time.LocalDate;
import java.util.UUID;

public interface ApproverResolverService {

    /**
     * Xác định người phê duyệt được phân công cho một bước dựa trên loại approverType.
     *
     * @param step bước quy trình cần resolve
     * @param requesterEmployeeId ID nhân viên tạo yêu cầu
     * @param companyId ID công ty
     * @return ID nhân viên được chỉ định phê duyệt bước này
     */
    UUID resolveAssignedApprover(WorkflowStep step, UUID requesterEmployeeId, UUID companyId);

    /**
     * Kiểm tra xem nhân viên hiện tại có quyền duyệt bước này hay không (chính chủ hoặc được ủy quyền).
     *
     * @param assignedApproverId ID người được phân công duyệt
     * @param currentEmployeeId ID nhân viên đang thao tác
     * @param date ngày thực hiện kiểm tra
     * @return true nếu có quyền, ngược lại false
     */
    boolean isAuthorizedApprover(UUID assignedApproverId, UUID currentEmployeeId, LocalDate date);
}
