package com.elearning.common.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestParamsTest {

    private static final Map<String, String> SORTABLE = Map.of("full_name", "fullName", "created_at", "createdAt");

    @Test
    void toPageable_mapsSnakeCaseSortFieldAndConvertsPageToZeroBased() {
        Pageable pageable = PageRequestParams.of(3, 10, "full_name", "asc").toPageable(SORTABLE, "createdAt");

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("fullName")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("fullName").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void toPageable_unknownSortFieldFallsBackToDefaultDescending() {
        Pageable pageable = PageRequestParams.of(null, null, "password_hash", null).toPageable(SORTABLE, "createdAt");

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(PageRequestParams.DEFAULT_PAGE_SIZE);
        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageable.getSort().getOrderFor("passwordHash")).isNull();
    }

    @Test
    void of_capsPageSizeAtMax() {
        assertThat(PageRequestParams.of(1, 100000, null, null).pageSize()).isEqualTo(PageRequestParams.MAX_PAGE_SIZE);
    }
}
