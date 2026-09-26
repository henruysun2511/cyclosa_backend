package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.ClusterType;
import com.cyclosa.workplace.enums.DeskShape;
import com.cyclosa.workplace.enums.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkGenerateSeatsRequest {

    @Min(value = 1, message = "Số thứ tự bắt đầu phải lớn hơn hoặc bằng 1")
    @Builder.Default
    private int startNumber = 1;

    @Min(value = 1, message = "Số lượng ghế cần tạo phải ít nhất là 1")
    @Builder.Default
    private int count = 6;

    private String codePrefix; // Ví dụ: "S" -> sẽ sinh "S01", "S02"... ghép cùng prefix của dãy/tầng

    @Builder.Default
    private SeatType seatType = SeatType.DEDICATED;

    @Builder.Default
    private DeskShape deskShape = DeskShape.RECTANGLE;

    @Builder.Default
    private ClusterType clusterType = ClusterType.POD_6_FACING;

    @NotNull(message = "Tọa độ X gốc không được để trống")
    private BigDecimal originX;

    @NotNull(message = "Tọa độ Y gốc không được để trống")
    private BigDecimal originY;

    @Builder.Default
    private BigDecimal deskWidth = BigDecimal.valueOf(60);

    @Builder.Default
    private BigDecimal deskHeight = BigDecimal.valueOf(40);

    @Builder.Default
    private BigDecimal spacingX = BigDecimal.valueOf(10);

    @Builder.Default
    private BigDecimal spacingY = BigDecimal.valueOf(10);

    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO;

    private List<String> amenities;
}
