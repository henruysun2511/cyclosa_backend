package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.PostingChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobPostingRequest {
    private UUID companyId;
    @NotNull(message = "Vị trí tuyển dụng không được để trống")
    private UUID jobPositionId;
    @NotBlank(message = "Tiêu đề tin tuyển dụng không được để trống")
    private String title;
    @NotNull(message = "Kênh đăng tuyển không được để trống")
    private PostingChannel channel;
    private String postingUrl;
    private LocalDateTime expiresAt;
}
