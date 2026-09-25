package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.PostingChannel;
import com.cyclosa.recruitment.enums.PostingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobPostingRequest {
    @NotBlank(message = "Tiêu đề tin tuyển dụng không được để trống")
    private String title;
    @NotNull(message = "Kênh đăng tuyển không được để trống")
    private PostingChannel channel;
    private String postingUrl;
    private PostingStatus status;
    private LocalDateTime expiresAt;
}
