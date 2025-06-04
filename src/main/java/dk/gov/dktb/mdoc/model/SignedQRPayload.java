package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORParser;
import dk.gov.dktb.mdoc.utilities.Base64Url;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/// Signed QR payload:
///
/// ```
/// QRPayload = {
///   "m": tstr         ; mdocGeneratedNonce cf. SignedQRHandover
///   "f": uint,        ; validFrom cf. SignedQRHandover
///   "t": uint,        ; validTo cf. SignedQRHandover
///   "d": bstr         ; CBOR encoding of Document
///}
///```
public class SignedQRPayload {
    private final String mdocGeneratedNonce;
    private final Instant validFrom;
    private final Instant validTo;
    private final DocumentExt document;

    @Setter
    private Clock clock = Clock.systemUTC();

    /**
     * Signed QR temporal validity period is not allowed to exceed this value
     */
    @Setter
    private Duration maxAllowedTimeToLive = Duration.ofSeconds(190);

    /**
     * Temporal validation allows for a clock skew up to this value
     */
    @Setter
    private Duration allowedClockSkew = Duration.ofSeconds(60);

    @SneakyThrows
    public SignedQRPayload(String qrData) {
        final byte[] decoded = Base64Url.decode(qrData);
        final var map = asMap(decoded);

        mdocGeneratedNonce = (String) map.get("m");
        validFrom = asEpochSecond(map.get("f"));
        validTo = asEpochSecond(map.get("t"));
        document = DocumentExt.from(asMap((byte[]) map.get("d")));

        if (!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("ValidFrom is after validTo");
        }
    }

    private static Map<String, Object> asMap(byte[] decoded) throws IOException {
        var parser = new CBORParser(decoded);
        final Object next = parser.next();
        if (!(next instanceof Map)) {
            throw new IllegalArgumentException("Expected a Map, but got " + next.getClass().getSimpleName());
        }
        //noinspection unchecked
        return (Map<String, Object>) next;
    }

    private static Instant asEpochSecond(Object value) {
        if (value instanceof Integer) {
            return Instant.ofEpochSecond((Integer) value);
        } else if (value instanceof Long) {
            return Instant.ofEpochSecond((Long) value);
        } else {
            throw new IllegalArgumentException("Cannot convert " + value + " to a valid epoch second");
        }
    }

    public void assertNotExpiredOrNotYetValid() {
        if (allowedClockSkew.isNegative()) throw new IllegalArgumentException("The clock skew cannot be negative");
        var skewSeconds = allowedClockSkew.getSeconds();
        var now = clock.instant();

        var validToWithSkew = validTo.plusSeconds(skewSeconds);
        if (now.isAfter(validToWithSkew)) {
            throw new SecurityException("QR code has expired at " + validTo);
        }

        var validFromWithSkew = validFrom.minusSeconds(skewSeconds);
        if (now.isBefore(validFromWithSkew)) {
            throw new SecurityException("QR code is not valid until " + validFrom);
        }
    }

    public void assertTimeToLiveValid() {
        if (validTo.minus(maxAllowedTimeToLive).isAfter(validFrom)) {
            throw new SecurityException("QR code lifetime is longer than " + maxAllowedTimeToLive.getSeconds() + " seconds");
        }
    }

    public void assertValid() {
        assertTimeToLiveValid();
        assertNotExpiredOrNotYetValid();
        assertDeviceSignatureValid();
    }

    private void assertDeviceSignatureValid() {
        var transcript = SessionTranscript.forSignedQR(validFrom, validTo, mdocGeneratedNonce);
        document.assertSignatureValid(transcript);
    }
}
