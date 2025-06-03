package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORByteArray;
import com.authlete.cbor.CBORItem;
import com.authlete.cbor.CBORItemList;
import com.authlete.cbor.CBORNull;
import com.authlete.cbor.CBORString;
import dk.gov.dktb.mdoc.utilities.SHA256;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Utility class to generate the session transcript for the OpenID4VP protocol.
 */
@NoArgsConstructor
@Getter
@Setter
public class SessionTranscript {
    //Simple version, that does not support deviceEngagement and/or eReaderData.
    // device engagement bytes (NOT tagged)
    //private byte[] deviceEngagementRawData;

    /// reader key bytes ( NOT tagged)
    //private byte[] eReaderRawData;

    // handover object
    private CBORItem handOver;

    public SessionTranscript(CBORItem handOver) {
        this.handOver = handOver;
    }


    /**
     *  * <p>
     *  *   SessionTranscript = [
     *  *   DeviceEngagementBytes,
     *  *   EReaderKeyBytes,
     *  *   Handover
     *  * ]
     *  * <p>
     *  * DeviceEngagementBytes = nil,
     *  * EReaderKeyBytes = nil
     *  * <p>
     *  * Handover = OID4VPHandover
     *  * OID4VPHandover = [
     *  * clientIdHash
     *  * responseUriHash
     *  * nonce
     *  * ]
     *  * <p>
     *  * clientIdHash = Data
     *  * responseUriHash = Data
     *  * <p>
     *  * where clientIdHash is the SHA-256 hash of clientIdToHash and responseUriHash is the SHA-256 hash of the responseUriToHash.
     *  * <p>
     *  * <p>
     *  * clientIdToHash = [clientId, mdocGeneratedNonce]
     *  * responseUriToHash = [responseUri, mdocGeneratedNonce]
     *  * <p>
     *  * <p>
     *  * mdocGeneratedNonce = String - available as apu header.
     *  * clientId = String
     *  * responseUri = String
     *  * nonce = String
     *
     * @param clientId              the client id of the verifier as defined in authentication request
     * @param responseUri           the response uri as defined in the authentication request
     * @param nonce                 client nonce, as defined in the authentication request
     * @param mdocGeneratedNonce    Wallet generated nonce, passed in response as "apu" header.
     * @return session transcript object used for validating device signature.
     */
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

}
