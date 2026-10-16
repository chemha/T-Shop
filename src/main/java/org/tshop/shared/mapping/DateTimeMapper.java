package org.tshop.shared.mapping;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Conversions MapStruct can't do on its own. Mappers list it in {@code uses}. */
@Component
public class DateTimeMapper {

    /** The API returns timestamps in UTC, e.g. 2026-10-16T14:05:00Z. */
    public OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
