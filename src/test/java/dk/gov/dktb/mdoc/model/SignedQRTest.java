package dk.gov.dktb.mdoc.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SignedQRTest {
    private static final String validPayload = "pGFkWQQ1o2dkb2NUeXBldGV1LmV1cm9wYS5lYy5hZ2V2MTBubGRldmljZVNpZ25lZKJqZGV2aWNlQXV0aKFvZGV2aWNlU2lnbmF0dXJl0oRDoQEmoFhY2BhYVIR0RGV2aWNlQXV0aGVudGljYXRpb26D9vaDdjZMbGhfZldPYmhySmswaGVlaHhaWUEaaD7hpxpoPuJbdGV1LmV1cm9wYS5lYy5hZ2V2MTBu2BhBoFhAYj83CiLjxOKHhcfGMdCTWV00WjE37MxVkRZYeKqDwAyJ49lqxhZImsWzNC37VMWfWRqURRNu8swNYSEMf4xS9WpuYW1lU3BhY2Vz2BhBoGxpc3N1ZXJTaWduZWSiamlzc3VlckF1dGiEQ6EBJqEYIoIvWCDbADAtKRN0jp8aVItb6fVPI6hQ7hsm8adeEHVRQp_LTlkCJ9gYWQIipmdkb2NUeXBldGV1LmV1cm9wYS5lYy5hZ2V2MTBuZ3ZlcnNpb25jMS4wbHZhbGlkaXR5SW5mb6Nmc2lnbmVkwHQyMDI1LTA2LTAzVDExOjUxOjAzWml2YWxpZEZyb23AdDIwMjUtMDYtMDNUMTE6NTE6MDNaanZhbGlkVW50aWzAdDIwMjUtMDktMDFUMTE6NTE6MDNabHZhbHVlRGlnZXN0c6F0ZXUuZXVyb3BhLmVjLmFnZXYxMG6mGgx3UbxYIJDcOPkM98Jddg51m4XZpFZu4w067PDcnbD5zRyWetjPGkzLHqdYIGsQrYC37a-RHkaRMqQlLFCL89lRBRq7On1pqiF1Rx10GlVOVtJYIBflZ-1FrYrV34ppI8x6yfYYEvP-5iNoysf3eSARsMvoGmowERdYIIDCIxmafW2i7ec8XMT19SrtTtWXSCrafu7zHk21BSdnGmyjww1YIEvBePAdeYWjuPDW1iUPkbROflpsWS9ABzb1Chc6iFUyGn1Chr1YIDzMfWFzxVxk4qCKtm_tqUgwFGRMlXfSX6qO0dYUsE54bWRldmljZUtleUluZm-haWRldmljZUtleaUBAgMmIAEhWCB7-wJX23K7WpofKWS0LBFNFKv6txKVDgtGqBGaKbeYXyJYIL4KhLBz09K9aiarvPNWHbuT384Nu2vCI-iDaECgUqryb2RpZ2VzdEFsZ29yaXRobWdTSEEtMjU2WEDU_XuZ46TZWV6HkzR74x7kUBdrr1-HjPNebTJDz_Iromyfksb0F4e9PcNJzJRLx-8mKd0Z7NnMsePhjNzuXKfHam5hbWVTcGFjZXOhdGV1LmV1cm9wYS5lYy5hZ2V2MTBugdgYWGSkZnJhbmRvbVggVb07h66Lhbwq1uC0Q3HsEEVyn5idk9es9jd9mcIZLZ5oZGlnZXN0SUQabKPDDWxlbGVtZW50VmFsdWX1cWVsZW1lbnRJZGVudGlmaWVya2FnZV9vdmVyXzE4YWYaaD7hp2FtdjZMbGhfZldPYmhySmswaGVlaHhaWUFhdBpoPuJb";
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

    private void setupValidSignedQRAt(final String validationTime) {
        signedQR = new SignedQRPayload(validPayload);
        signedQR.setClock(Clock.fixed(Instant.parse(validationTime), ZoneOffset.UTC));
        signedQR.setAllowedClockSkew(Duration.from(Duration.ofSeconds(60)));
        signedQR.setMaxAllowedTimeToLive(Duration.ofSeconds(180));
    }
}
