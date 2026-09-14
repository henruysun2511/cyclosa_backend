package com.cyclosa.common.util;

import com.cyclosa.common.response.PageData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public final class PageableUtils {

    private PageableUtils() {}

    private static final int MAX_SIZE = 100;

    public static String normalizeKeyword(String keyword) {
        return (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
    }

    public static <T> PageData<T> of(List<T> items, int page, int size, long total) {
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return PageData.<T>builder()
                .items(items)
                .pagination(PageData.Pagination.builder()
                        .page(page).size(size).total(total).pages(totalPages)
                        .hasNext(page < totalPages - 1).hasPrev(page > 0)
                        .build())
                .build();
    }

    public static Pageable of(int page, int size,
                              String sortBy, String direction,
                              String defaultSort, Set<String> allowed) {
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        int safePage = Math.max(page, 0);

        String safeSort = (sortBy != null && allowed.contains(sortBy)) ? sortBy : defaultSort;
        Sort.Direction dir = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize, Sort.by(dir, safeSort));
    }
}
