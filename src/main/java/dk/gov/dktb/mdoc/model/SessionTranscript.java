package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORByteArray;
import com.authlete.cbor.CBORItem;
import com.authlete.cbor.CBORItemList;
import com.authlete.cbor.CBORLong;
import com.authlete.cbor.CBORNull;
import com.authlete.cbor.CBORString;
import dk.gov.dktb.mdoc.utilities.SHA256;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
public class SessionTranscript {
    private CBORItem handOver;

    public SessionTranscript(CBORItem handOver) {
        this.handOver = handOver;
    }


    /// Build session transcript for OID4VP with the following syntax:
    ///
    /// ```
    /// SessionTranscript = [
    ///     DeviceEngagementBytes,
    ///     EReaderKeyBytes,
    ///     Handover
    ///]
    ///
    /// DeviceEngagementBytes = nil,
    /// EReaderKeyBytes = nil
    ///
    /// Handover = OID4VPHandover
    ///
    /// OID4VPHandover = [
    ///     clientIdHash
    ///     responseUriHash
    ///     nonce
    ///]
    ///
    /// clientIdHash = bstr
    /// responseUriHash = bstr
    ///```
    ///
    /// where clientIdHash is the SHA-256 hash of clientIdToHash and responseUriHash is the SHA-256 hash of the responseUriToHash:
    ///
    ///
    /// ```
    /// clientIdToHash = [clientId, mdocGeneratedNonce]
    /// responseUriToHash = [responseUri, mdocGeneratedNonce]
    ///
    /// mdocGeneratedNonce = tstr - available as apu header.
    /// clientId = tstr
    /// responseUri = tstr
    /// nonce = tstr
    ///```
    ///
    ///
    /// @param clientId           the client id of the verifier as defined in authentication request
    /// @param responseUri        the response uri as defined in the authentication request
    /// @param nonce              client nonce, as defined in the authentication request
    /// @param mdocGeneratedNonce Wallet generated nonce, passed in response as "apu" header.
    /// @return session transcript object used for validating device signature.
    public static SessionTranscript forOid4VP(String clientId, String responseUri, String nonce,
                                              String mdocGeneratedNonce) {

        var clientIdToHash = new CBORItemList(
                new CBORString(clientId),
                new CBORString(mdocGeneratedNonce)
        );
        var clientIdHash = SHA256.digest(clientIdToHash.encode());

        var responseUriToHash = new CBORItemList(
                new CBORString(responseUri),
                new CBORString(mdocGeneratedNonce)
        );
        var responseUriHash = SHA256.digest(responseUriToHash.encode());

        var handover = new CBORItemList(
                new CBORByteArray(clientIdHash),
                new CBORByteArray(responseUriHash),
                new CBORString(nonce)
        );

        return new SessionTranscript(handover);
    }

    public CBORItem asCBOR() {
        return new CBORItemList(
                CBORNull.INSTANCE,
                CBORNull.INSTANCE,
                handOver
        );
    }

    /// ```
    /// SessionTranscript = [
    ///    DeviceEngagementBytes : bstr,  // always nil for signed QR presentations
    ///    EReaderKeyBytes       : bstr,  // always nil for signed QR presentations
    ///    Handover              : SignedQRHandover
    ///]
    ///
    /// SignedQRHandover = [
    ///    mdocGeneratedNonce   : tstr // 16 random bytes, Base64URL-encoded
    ///    validFrom            : uint // Unix timestamp
    ///    validTo              : uint // Unix timestamp
    ///]
    ///```
    public static SessionTranscript forSignedQR(Instant validFrom, Instant validTo, String mdocGeneratedNonce) {
        var handover = new CBORItemList(
                new CBORString(mdocGeneratedNonce),
                new CBORLong(validFrom.getEpochSecond()),
                new CBORLong(validTo.getEpochSecond())
        );

        return new SessionTranscript(handover);
    }
}
