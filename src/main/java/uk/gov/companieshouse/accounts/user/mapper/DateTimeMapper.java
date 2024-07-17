package uk.gov.companieshouse.accounts.user.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DateTimeMapper {

    @Named("localDateTimeToOffsetDateTime")
    default  OffsetDateTime localDateTimeToOffsetDateTime( LocalDateTime localDateTime){
        return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
    }

    @Named("offsetDateTimeToDateTime")
    default  LocalDateTime offsetDateTimeToDateTime( OffsetDateTime offsetDateTime){
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}
