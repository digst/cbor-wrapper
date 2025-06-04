package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORParser;
import dk.gov.dktb.mdoc.utilities.Base64Url;
import lombok.SneakyThrows;

import java.io.IOException;
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

    @SneakyThrows
    public SignedQRPayload(String qrData) {
        final byte[] decoded = Base64Url.decode(qrData);
        final var map = asMap(decoded);

        mdocGeneratedNonce = (String) map.get("m");
        validFrom = asEpochSecond(map.get("f"));
        validTo = asEpochSecond(map.get("t"));
        document = DocumentExt.from(asMap((byte[]) map.get("d")));

        if(!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("ValidFrom is after validTo");
        }
    }

    private static Map<String, Object> asMap(byte[] decoded) throws IOException {
        var parser = new CBORParser(decoded);
        return (Map<String, Object>) parser.next();
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

    public void assertNotExpiredOrNotYetValid(Duration allowedClockSkew) {
        if (allowedClockSkew.isNegative()) throw new IllegalArgumentException("The clock skew cannot be negative");
        var skewSeconds = allowedClockSkew.getSeconds();
        var now = Instant.now();
        var expiresAt = validTo.plusSeconds(skewSeconds);
        if (now.isAfter(expiresAt)) {
            throw new SecurityException("QR code has expired at " + validTo);
        }
        var validAt = validFrom.minusSeconds(skewSeconds);
        if (now.isBefore(validAt)) {
            throw new SecurityException("QR code is not valid until " + validFrom);
        }
    }

    public void assertTimeToLiveValid(Duration timeToLive) {
        if (validTo.minus(timeToLive).isAfter(validFrom)) {
            throw new SecurityException("QR code lifetime is longer than " + timeToLive.getSeconds() + " seconds");
        }
    }

    public void assertValid() {
        assertTimeToLiveValid(Duration.ofSeconds(190));
        assertNotExpiredOrNotYetValid(Duration.ofSeconds(60));
        assertDeviceSignatureValid();
    }

    private void assertDeviceSignatureValid() {
        var transcript = SessionTranscript.forSignedQR(validFrom, validTo, mdocGeneratedNonce);
        document.assertSignatureValid(transcript);
    }
}
