package dk.gov.dktb.mdoc.model;

import dk.gov.dktb.mdoc.utilities.Base64Url;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SignedQRTest {
    private static final byte[] validPayload = Base64Url.decode("pGFkWQQ1o2dkb2NUeXBldGV1LmV1cm9wYS5lYy5hZ2V2MTBubGRldmljZVNpZ25lZKJqZGV2aWNlQXV0aKFvZGV2aWNlU2lnbmF0dXJl0oRDoQEmoFhY2BhYVIR0RGV2aWNlQXV0aGVudGljYXRpb26D9vaDdjZMbGhfZldPYmhySmswaGVlaHhaWUEaaD7hpxpoPuJbdGV1LmV1cm9wYS5lYy5hZ2V2MTBu2BhBoFhAYj83CiLjxOKHhcfGMdCTWV00WjE37MxVkRZYeKqDwAyJ49lqxhZImsWzNC37VMWfWRqURRNu8swNYSEMf4xS9WpuYW1lU3BhY2Vz2BhBoGxpc3N1ZXJTaWduZWSiamlzc3VlckF1dGiEQ6EBJqEYIoIvWCDbADAtKRN0jp8aVItb6fVPI6hQ7hsm8adeEHVRQp_LTlkCJ9gYWQIipmdkb2NUeXBldGV1LmV1cm9wYS5lYy5hZ2V2MTBuZ3ZlcnNpb25jMS4wbHZhbGlkaXR5SW5mb6Nmc2lnbmVkwHQyMDI1LTA2LTAzVDExOjUxOjAzWml2YWxpZEZyb23AdDIwMjUtMDYtMDNUMTE6NTE6MDNaanZhbGlkVW50aWzAdDIwMjUtMDktMDFUMTE6NTE6MDNabHZhbHVlRGlnZXN0c6F0ZXUuZXVyb3BhLmVjLmFnZXYxMG6mGgx3UbxYIJDcOPkM98Jddg51m4XZpFZu4w067PDcnbD5zRyWetjPGkzLHqdYIGsQrYC37a-RHkaRMqQlLFCL89lRBRq7On1pqiF1Rx10GlVOVtJYIBflZ-1FrYrV34ppI8x6yfYYEvP-5iNoysf3eSARsMvoGmowERdYIIDCIxmafW2i7ec8XMT19SrtTtWXSCrafu7zHk21BSdnGmyjww1YIEvBePAdeYWjuPDW1iUPkbROflpsWS9ABzb1Chc6iFUyGn1Chr1YIDzMfWFzxVxk4qCKtm_tqUgwFGRMlXfSX6qO0dYUsE54bWRldmljZUtleUluZm-haWRldmljZUtleaUBAgMmIAEhWCB7-wJX23K7WpofKWS0LBFNFKv6txKVDgtGqBGaKbeYXyJYIL4KhLBz09K9aiarvPNWHbuT384Nu2vCI-iDaECgUqryb2RpZ2VzdEFsZ29yaXRobWdTSEEtMjU2WEDU_XuZ46TZWV6HkzR74x7kUBdrr1-HjPNebTJDz_Iromyfksb0F4e9PcNJzJRLx-8mKd0Z7NnMsePhjNzuXKfHam5hbWVTcGFjZXOhdGV1LmV1cm9wYS5lYy5hZ2V2MTBugdgYWGSkZnJhbmRvbVggVb07h66Lhbwq1uC0Q3HsEEVyn5idk9es9jd9mcIZLZ5oZGlnZXN0SUQabKPDDWxlbGVtZW50VmFsdWX1cWVsZW1lbnRJZGVudGlmaWVya2FnZV9vdmVyXzE4YWYaaD7hp2FtdjZMbGhfZldPYmhySmswaGVlaHhaWUFhdBpoPuJb");

    private static final String[] validMultiplePayloads = new String[]{
            "o2FpAGFuBGFwWQEYpGFkWQQ1o2dkb2NUeXBldGV1LmV1cm9wYS5lYy5hZ2V2MTBubGRldmljZVNpZ25lZKJqZGV2aWNlQXV0aKFvZGV2aWNlU2lnbmF0dXJl0oRDoQEmoFhY2BhYVIR0RGV2aWNlQXV0aGVudGljYXRpb26D9vaDdmdqVTc3Qnp3TkpfaEE2UmxFWEtvMEEaaEBs5xpoQG2bdGV1LmV1cm9wYS5lYy5hZ2V2MTBu2BhBoFhAeNQkjTyw_tWj7GTNnxT-UwHzBPfw4G1-VkKlPRBdE1zNwHL5XW0K29PtTkduR6B0audFN34sIqDUItd7VjXgsmpuYW1lU3BhY2Vz2BhBoGxpc3N1ZXJTaWduZWSiamlzc3VlckF1dA",
            "o2FpAWFuBGFwWQEYaIRDoQEmoRgigi9YINsAMC0pE3SOnxpUi1vp9U8jqFDuGybxp14QdVFCn8tOWQIn2BhZAiKmZ2RvY1R5cGV0ZXUuZXVyb3BhLmVjLmFnZXYxMG5ndmVyc2lvbmMxLjBsdmFsaWRpdHlJbmZvo2ZzaWduZWTAdDIwMjUtMDYtMDRUMTU6NTc6MjdaaXZhbGlkRnJvbcB0MjAyNS0wNi0wNFQxNTo1NzoyN1pqdmFsaWRVbnRpbMB0MjAyNS0wOS0wMlQxNTo1NzoyN1psdmFsdWVEaWdlc3RzoXRldS5ldXJvcGEuZWMuYWdldjEwbqYaFoC_vVggOxBqcRTVqcfRTNpKCht_DRD9EJ8BCgXXocN2k9cibg8aHQ",
            "o2FpAmFuBGFwWQEY9IeDWCChQvELwA8oAwrFEZbQJt57NXfHPT10COQxCJMYz4qKABoohT9ZWCDg5BpL29irLPUXWhKkq0hBsyhyXWFnP9liBAWRp1ph5horGWMbWCBD0YYVcmLMLBmJ2hzOs-WYIbu29xJDu4x2v2YeT0rURhpMBrrKWCAU9TV71SbPMuzcx4Z4_VlVV00FdHqWcjIVga49OcNvexpscx4_WCDi68D0NL6SArQ7VcFtHtbnGYzcmc0_oLDn5tPHM59Qgm1kZXZpY2VLZXlJbmZvoWlkZXZpY2VLZXmlAQIDJiABIVggriFPfyA1-VmyvtTg2Wp_q6oxwav5DvAZHbw9Nv2S9z4iWCCTKd3iBLnT9sSpd1cZxaN68g",
            "o2FpA2FuBGFwWQEahHsbRjB02nHpymurFv5Gb2RpZ2VzdEFsZ29yaXRobWdTSEEtMjU2WEAKmz_rbRVyQcyVxzI-99OtwWYxTtGH4bi5hy3OG-_dJN_KvpdrOutzaIQFL9UMn71aNNlj7kBycgd7iqwyL7n_am5hbWVTcGFjZXOhdGV1LmV1cm9wYS5lYy5hZ2V2MTBugdgYWGSkZnJhbmRvbVggms9eJ2458pYojbjvTxnLVQ7JnabkZIxuMUdCmxkmOYloZGlnZXN0SUQaTAa6ymxlbGVtZW50VmFsdWX1cWVsZW1lbnRJZGVudGlmaWVya2FnZV9vdmVyXzE4YWYaaEBs52FtdmdqVTc3Qnp3TkpfaEE2UmxFWEtvMEFhdBpoQG2b"
    };

    private SignedQRPayload signedQR;

    @Test
    @SneakyThrows
    public void signedQrIsValid() {
        setupValidSignedQRAt("2025-06-03T11:53:00Z");

        signedQR.assertValid();
    }

    @Test
    @SneakyThrows
    public void signedQrIsExpired() {
        setupValidSignedQRAt("2025-06-03T11:58:00Z");

        var exception = assertThrows(SecurityException.class, () -> signedQR.assertValid());

        assertEquals("QR code has expired at 2025-06-03T11:54:03Z", exception.getMessage());
    }

    @Test
    @SneakyThrows
    public void signedQrIsNotYetValid() {
        setupValidSignedQRAt("2025-06-03T11:30:00Z");

        var exception = assertThrows(SecurityException.class, () -> signedQR.assertValid());

        assertEquals("QR code is not valid until 2025-06-03T11:51:03Z", exception.getMessage());
    }

    @Test
    @SneakyThrows
    public void signedQrIsNotExpiredWithinClockSkew() {
        setupValidSignedQRAt("2025-06-03T11:54:33Z");

        signedQR.assertValid();
    }

    @Test
    @SneakyThrows
    public void signedQrIsValidBeforeValidFromWithinClockSkew() {
        setupValidSignedQRAt("2025-06-03T11:50:33Z");

        signedQR.assertValid();
    }

    @Test
    @SneakyThrows
    public void multipleIsValid() {
        setupValidMultipleQRAt("2025-06-04T15:58:00Z");

        signedQR.assertValid();
    }

    private void setupValidSignedQRAt(final String validationTime) {
        signedQR = new SignedQRPayload(validPayload);
        signedQR.setClock(Clock.fixed(Instant.parse(validationTime), ZoneOffset.UTC));
        signedQR.setAllowedClockSkew(Duration.from(Duration.ofSeconds(60)));
        signedQR.setMaxAllowedTimeToLive(Duration.ofSeconds(180));
    }

    private void setupValidMultipleQRAt(final String validationTime) {
        signedQR = SignedQRPayload.fromMultipleParts(
                Arrays.stream(validMultiplePayloads).sequential().map(Base64Url::decode).toList()
        );
        signedQR.setClock(Clock.fixed(Instant.parse(validationTime), ZoneOffset.UTC));
        signedQR.setAllowedClockSkew(Duration.from(Duration.ofSeconds(60)));
        signedQR.setMaxAllowedTimeToLive(Duration.ofSeconds(180));
    }
}
