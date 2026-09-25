package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.PostingChannel;
import com.cyclosa.recruitment.enums.PostingStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingResponse {
    private UUID id;
    private UUID companyId;
    private UUID jobPositionId;
    private String jobPositionTitle;
    private String title;
    private PostingChannel channel;
    private String postingUrl;
    private PostingStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private Integer viewCount;
    private Integer applyCount;
    private LocalDateTime createdAt;
}
