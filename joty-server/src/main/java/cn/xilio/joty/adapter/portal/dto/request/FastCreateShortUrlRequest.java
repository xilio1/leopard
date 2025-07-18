package cn.xilio.joty.adapter.portal.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record FastCreateShortUrlRequest(
        /** 原始URL */
        @NotEmpty String url
) {
}
