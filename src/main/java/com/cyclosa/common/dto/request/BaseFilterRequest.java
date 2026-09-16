package com.cyclosa.common.dto.request;

import com.cyclosa.common.util.PageableUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.util.Set;

@Getter
@Setter
public abstract class BaseFilterRequest {

    @Parameter(description = "Từ khóa tìm kiếm")
    private String keyword;

    @Parameter(description = "Trang hiện tại (bắt đầu từ 0)", schema = @Schema(defaultValue = "0"))
    @Min(value = 0, message = "Số trang không được âm")
    private int page = 0;

    @Parameter(description = "Số phần tử mỗi trang (tối đa 100)", schema = @Schema(defaultValue = "20"))
    @Min(value = 1, message = "Kích thước trang tối thiểu là 1")
    @Max(value = 100, message = "Kích thước trang tối đa là 100")
    private int size = 20;

    @Parameter(description = "Trường sắp xếp", schema = @Schema(defaultValue = "createdAt"))
    private String sortBy = "createdAt";

    @Parameter(description = "Chiều sắp xếp: asc hoặc desc", schema = @Schema(defaultValue = "desc", allowableValues = {"asc", "desc"}))
    @Pattern(regexp = "(?i)^(asc|desc)$", message = "Chiều sắp xếp chỉ nhận giá trị 'asc' hoặc 'desc'")
    private String direction = "desc";

    /**
     * Chuyển đổi sang Spring Data Pageable với trường sắp xếp mặc định và danh sách trường hợp lệ
     */
    public Pageable toPageable(String defaultSortBy, Set<String> allowedSortFields) {
        String safeSort = (sortBy != null && !sortBy.isBlank()) ? sortBy : defaultSortBy;
        return PageableUtils.of(this.page, this.size, safeSort, this.direction, defaultSortBy, allowedSortFields);
    }
}
