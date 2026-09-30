
package com.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum CacheStrategy {
    NONE("NONE"),
    CACHE_ASIDE("CACHE_ASIDE")
    ;

    private final String cacheStrategy;
}
