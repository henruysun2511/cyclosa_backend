package com.cyclosa.workflow.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalActionRequest {

    @Size(max = 1000, message = "Nhận xét tối đa 1000 ký tự")
    private String comment;

    @Builder.Default
    private Map<String, Object> contextVariables = new HashMap<>();
}
