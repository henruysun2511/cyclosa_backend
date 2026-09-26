package com.cyclosa.discipline.dto.request;

import com.cyclosa.discipline.enums.GrievanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveGrievanceRequest {

    @NotNull(message = "Trạng thái xử lý không được để trống")
    private GrievanceStatus status;

    @NotBlank(message = "Nội dung giải quyết / phản hồi không được để trống")
    private String resolutionNote;
}
