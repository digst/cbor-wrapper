package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORByteArray;
import com.authlete.cbor.CBORItemList;
import com.authlete.cbor.CBORPairList;
import com.authlete.cbor.CBORString;
import com.authlete.cbor.CBORTaggedItem;
import com.authlete.mdoc.Document;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 *  Implements the intermediate structure for DeviceAuthentication.
 *  This structure is not transferred, only computed.
 */
@RequiredArgsConstructor
public class DeviceAuthentication
{
    private final SessionTranscript sessionTranscript;
    private final Document document;

    public byte[] encode() {

        val docType = document.findByKey("docType").getValue();
        val deviceSigned = (CBORPairList)document.findByKey("deviceSigned").getValue();
        val nameSpaces = deviceSigned.findByKey("nameSpaces").getValue();

        val cbor = new CBORItemList(
                new CBORString("DeviceAuthentication"),
                sessionTranscript.asCBOR(),
                docType,
                nameSpaces
        );
        return new CBORTaggedItem(24, new CBORByteArray(cbor.encode())).encode();
    }
}
