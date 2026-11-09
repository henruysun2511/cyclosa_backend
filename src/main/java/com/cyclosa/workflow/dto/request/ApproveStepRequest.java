package com.cyclosa.workflow.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveStepRequest {
    private String comment;
}

