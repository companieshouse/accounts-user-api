package uk.gov.companieshouse.accounts.user.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class ParsingUtils {

    public static String reduceTimestampResolution( final String timestamp ) {
        return timestamp.substring( 0, timestamp.indexOf( ":" ) );
    }

    public static String localDateTimeToNormalisedString( final LocalDateTime localDateTime ) {
        final var timestamp = localDateTime.toString();
        return reduceTimestampResolution( timestamp );
    }

    public static OffsetDateTime localDateTimeToOffsetDateTime( final LocalDateTime localDateTime ) {
        return Objects.isNull( localDateTime ) ? null : OffsetDateTime.of( localDateTime, ZoneOffset.UTC );
    }

}
