package dk.gov.dktb.mdoc.utilities;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Url {
    public static String encode(byte [] bytes) {
        return new String(Base64.getUrlEncoder().encode(bytes), StandardCharsets.UTF_8);
    }

    public static byte[] decode(String base64) {
        return Base64.getUrlDecoder().decode(base64);
    }
}
