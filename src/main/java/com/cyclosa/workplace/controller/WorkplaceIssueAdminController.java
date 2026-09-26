package com.cyclosa.workplace.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.workplace.dto.filter.IssueTicketFilter;
import com.cyclosa.workplace.dto.request.UpdateIssueTicketStatusRequest;
import com.cyclosa.workplace.dto.response.IssueTicketResponse;
import com.cyclosa.workplace.service.WorkplaceIssueTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workplace/issue-tickets")
@RequiredArgsConstructor
@Tag(name = "Workplace Facility Issues", description = "Quản lý và tiếp nhận xử lý các sự cố cơ sở vật chất gắn vị trí")
public class WorkplaceIssueAdminController {

    private final WorkplaceIssueTicketService issueTicketService;

    @GetMapping
    @PreAuthorize("@perm.has('workplace.ticket.view')")
    @RequirePermission("workplace.ticket.view")
    @Operation(summary = "Lọc danh sách sự cố cơ sở vật chất có phân trang")
    public ResponseEntity<ApiResponse<PageData<IssueTicketResponse>>> filterTickets(@Valid IssueTicketFilter filter) {
        PageData<IssueTicketResponse> response = issueTicketService.filterTickets(filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('workplace.ticket.view')")
    @RequirePermission("workplace.ticket.view")
    @Operation(summary = "Xem chi tiết sự cố cơ sở vật chất")
    public ResponseEntity<ApiResponse<IssueTicketResponse>> getTicketDetail(@PathVariable UUID id) {
        IssueTicketResponse response = issueTicketService.getTicketDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@perm.has('workplace.ticket.manage')")
    @RequirePermission("workplace.ticket.manage")
    @Operation(summary = "Cập nhật tiến độ / Hoàn tất xử lý sự cố (Tự động khôi phục trạng thái bàn)")
    public ResponseEntity<ApiResponse<IssueTicketResponse>> updateTicketStatus(@PathVariable UUID id,
                                                                               @Valid @RequestBody UpdateIssueTicketStatusRequest request) {
        IssueTicketResponse response = issueTicketService.updateTicketStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
