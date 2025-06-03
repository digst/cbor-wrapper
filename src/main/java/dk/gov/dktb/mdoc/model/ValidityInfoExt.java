package dk.gov.dktb.mdoc.model;

import com.authlete.mdoc.ValidityInfo;
import lombok.Getter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Getter
public class ValidityInfoExt extends ValidityInfo {
    private ZonedDateTime signed;
    private ZonedDateTime validFrom;
    private ZonedDateTime validUntil;
    private ZonedDateTime expectedUpdate;

    public ValidityInfoExt(ZonedDateTime signed, ZonedDateTime validFrom, ZonedDateTime validUntil) {
        super(signed, validFrom, validUntil);
        this.signed = signed;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public ValidityInfoExt(ZonedDateTime signed, ZonedDateTime validFrom, ZonedDateTime validUntil, ZonedDateTime expectedUpdate) {
        super(signed, validFrom, validUntil, expectedUpdate);
        this.signed = signed;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.expectedUpdate = expectedUpdate;
    }

    public static ValidityInfoExt from(Map<String, Object> cbor) {
        return new ValidityInfoExt(
                asZonedDateTime(cbor.get("signed")),
                asZonedDateTime(cbor.get("validFrom")),
                asZonedDateTime(cbor.get("validUntil")),
                asZonedDateTime(cbor.get("expectedUpdate"))
        );
    }

    private static ZonedDateTime asZonedDateTime(Object date) {
        if (date == null) return null;
        Instant instant = Instant.parse((String) date);
        return instant.atZone(ZoneId.of("UTC")); // or any other zone

        //  return ZonedDateTime.parse((String) date, DateTimeFormatter.ISO_INSTANT);
    }
}
