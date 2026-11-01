package com.cyclosa.authorization.dto.request;

import com.cyclosa.common.enums.DataScope;
import com.cyclosa.authorization.enums.DocumentAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu cấu hình quyền tài liệu số cho vai trò")
public class RoleDocumentPermissionRequest {

    @Valid
    @NotNull(message = "Danh sách quyền tài liệu không được null")
    private List<DocumentPermissionItem> permissions = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Cấu hình quyền trên một loại tài liệu")
    public static class DocumentPermissionItem {

        @NotNull(message = "Loại tài liệu không được để trống")
        private UUID documentTypeId;

        @NotNull(message = "Hành động không được để trống")
        private DocumentAction action;

        @NotNull(message = "Phạm vi dữ liệu không được để trống")
        private DataScope dataScope;
    }
}

