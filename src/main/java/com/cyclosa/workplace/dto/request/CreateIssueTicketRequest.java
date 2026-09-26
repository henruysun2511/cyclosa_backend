package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.IssueSeverity;
import com.cyclosa.workplace.enums.WorkplaceIssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateIssueTicketRequest {

    private UUID seatId; // Tùy chọn nếu xảy ra tại bàn

    private UUID nonDeskObjectId; // Tùy chọn nếu xảy ra tại phòng họp / tiện ích

    @NotNull(message = "Tầng (floorId) không được để trống")
    private UUID floorId;

    @NotNull(message = "Loại sự cố không được để trống")
    private WorkplaceIssueType issueType;

    @Builder.Default
    private IssueSeverity severity = IssueSeverity.MEDIUM;

    @NotBlank(message = "Tiêu đề sự cố không được để trống")
    @Size(max = 200, message = "Tiêu đề không vượt quá 200 ký tự")
    private String title;

    private String description;

    private BigDecimal positionX;

    private BigDecimal positionY;
}
