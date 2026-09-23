package com.cyclosa.asset.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryCheckRequest {

    private UUID companyId;

    @Size(max = 255, message = "Tiêu đề đợt kiểm kê tối đa 255 ký tự")
    private String title;

    @NotNull(message = "Ngày kiểm kê không được để trống")
    private LocalDate checkDate;

    @NotNull(message = "ID nhân viên thực hiện kiểm kê không được để trống")
    private UUID performedByEmployeeId;

    private String note;

    @NotEmpty(message = "Danh sách tài sản kiểm kê không được để trống")
    @Valid
    private List<InventoryCheckItemRequest> items;
}
