package com.cyclosa.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectStepRequest {

    @NotBlank(message = "Vui lòng nhập lý do từ chối yêu cầu phê duyệt")
    @Size(min = 5, message = "Lý do từ chối phải có tối thiểu 5 ký tự")
    private String comment;
}

