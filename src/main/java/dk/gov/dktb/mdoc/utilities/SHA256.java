package dk.gov.dktb.mdoc.utilities;

import lombok.SneakyThrows;
import lombok.val;

import java.security.MessageDigest;

public class SHA256 {
    @SneakyThrows
    public static byte[] digest(byte[] input) {
        val digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input);
    }
}
