package io.github.nct682000.authservice.dto.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequests {

    private PageRequests() {}

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt")
            .and(Sort.by(Sort.Direction.DESC, "id"));

    public static Pageable of(int page, int size) {
        return PageRequest.of(page, size, DEFAULT_SORT);
    }
}
