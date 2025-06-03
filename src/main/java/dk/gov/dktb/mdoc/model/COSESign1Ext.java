package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORByteArray;
import com.authlete.cbor.CBORItem;
import com.authlete.cbor.CBORPair;
import com.authlete.cbor.CBORParser;
import com.authlete.cbor.CBORizer;
import com.authlete.cose.COSEProtectedHeader;
import com.authlete.cose.COSESign1;
import com.authlete.cose.COSEUnprotectedHeader;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

@Getter
public class COSESign1Ext extends COSESign1 {
    private static final int PROTECTED_HEADER_INDEX = 0;
    private static final int UNPROTECTED_HEADER_INDEX = 1;
    private static final int PAYLOAD_INDEX = 2;
    private static final int SIGNATURE_INDEX = 3;

    private COSEProtectedHeader protectedHeader;
    private COSEUnprotectedHeader unprotectedHeader;
    private CBORItem payload;
    private CBORByteArray signature;

    /**
     * A constructor with a protected header, an unprotected header, a
     * payload and a signature.
     *
     * @param protectedHeader   A protected header. Must not be null.
     * @param unprotectedHeader An unprotected header. Must not be null.
     * @param payload           A payload. Must be either {@link CBORByteArray} or
     *                          {@link CBORNull}.
     * @param signature         A signature. Must not be null.
     */
    public COSESign1Ext(COSEProtectedHeader protectedHeader, COSEUnprotectedHeader unprotectedHeader, CBORItem payload, CBORByteArray signature) {
        super(protectedHeader, unprotectedHeader, payload, signature);
        this.protectedHeader = protectedHeader;
        this.unprotectedHeader = unprotectedHeader;
        this.payload = payload;
        this.signature = signature;
    }

    @SneakyThrows
    public static COSESign1Ext from(List<Object> parsed) {
        final var protectedHeader = getProtectedHeader(parsed);
        final var unprotectedHeader = getUnprotectedHeader(parsed);
        final var payload = new CBORByteArray((byte[]) parsed.get(PAYLOAD_INDEX));
        final var signature = new CBORByteArray((byte[]) parsed.get(SIGNATURE_INDEX));

        return new COSESign1Ext(protectedHeader, unprotectedHeader, payload, signature);
    }

    private static COSEUnprotectedHeader getUnprotectedHeader(List<Object> parsed) {
        final Map<Object, Object> map = (Map<Object, Object>) parsed.get(UNPROTECTED_HEADER_INDEX);
        return new COSEUnprotectedHeader(asPairs(map));
    }

    private static COSEProtectedHeader getProtectedHeader(List<Object> parsed) throws IOException {
        var parser = new CBORParser((byte[]) parsed.get(PROTECTED_HEADER_INDEX));
        var map = asPairs((Map<Object, Object>) parser.next());
        return new COSEProtectedHeader((byte[]) parsed.getFirst(), map);
    }

    private static List<CBORPair> asPairs(Map<Object, Object> map) {
        return map.entrySet().stream().map(e -> {
            final var izer = new CBORizer();
            return new CBORPair(izer.cborize(e.getKey()), izer.cborize(e.getValue()));
        }).toList();
    }

    public X509Certificate getSigningCertificate() {
        return unprotectedHeader.getX5Chain().getFirst();
    }

    public void setPayload(byte[] payload) {
        this.payload = new CBORByteArray(payload);
    }
}
