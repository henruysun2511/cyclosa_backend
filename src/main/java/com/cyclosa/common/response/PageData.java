package com.cyclosa.common.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder
public class PageData<T> {

    private List<T>    items;
    private Pagination pagination;

    public static <S, T> PageData<T> of(Page<S> page, Function<S, T> mapper) {
        return PageData.<T>builder()
                .items(page.getContent().stream().map(mapper).toList())
                .pagination(Pagination.from(page))
                .build();
    }

    @Getter
    @Builder
    public static class Pagination {
        private int     page;
        private int     size;
        private long    total;
        private int     pages;
        private boolean hasNext;
        private boolean hasPrev;

        static Pagination from(Page<?> page) {
            return Pagination.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .total(page.getTotalElements())
                    .pages(page.getTotalPages())
                    .hasNext(!page.isLast())
                    .hasPrev(!page.isFirst())
                    .build();
        }
    }
}
