package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORParser;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
///
/// Multiple parts:
///
/// ```
/// PartialQRPayload = {
///   "i": uint,          ; Number of this part
///   "n": uint,          ; Number of parts in total
///   "p": bstr           ; Part p_i
///}
///```
public class SignedQRPayload {
    public static final String MDOC_GENERATED_NONCE_LABEL = "m";
    public static final String VALID_FROM_LABEL = "f";
    public static final String VALID_TO_LABEL = "t";
    public static final String DOCUMENT_LABEL = "d";
    public static final String CURRENT_PART_LABEL = "i";
    public static final String NUMBER_OF_PARTS_LABEL = "n";
    public static final String PART_LABEL = "p";


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

    /**
     * Construct from single QR payload
     *
     * @param payload data from QR code
     */
    @SneakyThrows
    public SignedQRPayload(byte[] payload) {
        final var map = asMap(payload);

        mdocGeneratedNonce = (String) map.get(MDOC_GENERATED_NONCE_LABEL);
        validFrom = asEpochSecond(map.get(VALID_FROM_LABEL));
        validTo = asEpochSecond(map.get(VALID_TO_LABEL));
        document = DocumentExt.from(asMap((byte[]) map.get(DOCUMENT_LABEL)));

        if (!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("ValidFrom is after validTo");
        }
    }

    @SneakyThrows
    public static SignedQRPayload fromMultipleParts(List<byte[]> payloads) {
        var count = payloads.size();
        var parts = new ArrayList<byte[]>(Collections.nCopies(count, null));
        for (byte[] payload : payloads) {
            final var map = asMap(payload);
            var i = (Integer) map.get(CURRENT_PART_LABEL);
            if (i < 0 || i >= count) {
                throw new IllegalArgumentException("Invalid part number: " + i);
            }
            var n = (Integer) map.get(NUMBER_OF_PARTS_LABEL);
            if (n != count) {
                throw new IllegalArgumentException("Number of parts (n) in CBOR structure must be the same as the number of parts passed to constructor.");
            }
            var p = (byte[]) map.get(PART_LABEL);
            parts.set(i, p);
        }
        return new SignedQRPayload(concatenate(parts));
    }

    public static byte[] concatenate(List<byte[]> arrays) {
        var totalLength = arrays.stream().mapToInt(array -> array.length).sum();

        var result = new byte[totalLength];
        int currentPos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentPos, array.length);
            currentPos += array.length;
        }
        return result;
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
