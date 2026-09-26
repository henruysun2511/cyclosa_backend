package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.DeskShape;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSeatRequest {

    @NotBlank(message = "Mã chỗ ngồi không được để trống")
    @Size(max = 100, message = "Mã chỗ ngồi không vượt quá 100 ký tự")
    private String seatCode;

    @NotNull(message = "Loại chỗ ngồi không được để trống")
    private SeatType seatType;

    @NotNull(message = "Trạng thái không được để trống")
    private SeatStatus status;

    @Builder.Default
    private DeskShape deskShape = DeskShape.RECTANGLE;

    @NotNull(message = "Tọa độ X không được để trống")
    private BigDecimal positionX;

    @NotNull(message = "Tọa độ Y không được để trống")
    private BigDecimal positionY;

    @Builder.Default
    private BigDecimal width = BigDecimal.valueOf(60);

    @Builder.Default
    private BigDecimal height = BigDecimal.valueOf(40);

    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO;

    private List<String> amenities;

    private UUID assignedEmployeeId;

    private String visualMetadata;
}
