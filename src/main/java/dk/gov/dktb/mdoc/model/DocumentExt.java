package dk.gov.dktb.mdoc.model;

import com.authlete.cose.COSEVerifier;
import com.authlete.mdoc.DeviceSigned;
import com.authlete.mdoc.Document;
import com.authlete.mdoc.Errors;
import com.authlete.mdoc.IssuerSigned;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import java.util.Map;

@Setter
public class DocumentExt extends Document {
    private String docType;
    private IssuerSigned issuerSigned;
    private DeviceSigned deviceSigned;
    private Errors errors;

    public DocumentExt(String docType, IssuerSigned issuerSigned) {
        super(docType, issuerSigned);
        this.docType = docType;
        this.issuerSigned = issuerSigned;
    }

    public DocumentExt(String docType, IssuerSigned issuerSigned, DeviceSigned deviceSigned, Errors errors) {
        super(docType, issuerSigned, deviceSigned, errors);
        this.docType = docType;
        this.issuerSigned = issuerSigned;
        this.deviceSigned = deviceSigned;
        this.errors = errors;
    }

    public static DocumentExt from(Map<String, Object> cbor) {
        val docType = cbor.get("docType");
        val issuerSigned = IssuerSignedExt.from((Map<String, Object>) cbor.get("issuerSigned"));
        val deviceSigned = DeviceSignedExt.from((Map<String, Object>) cbor.get("deviceSigned"));
        return new DocumentExt((String) docType, issuerSigned, deviceSigned, null);
    }

    public IssuerSignedExt getIssuerSigned() {
        return (IssuerSignedExt) issuerSigned;
    }

    public DeviceSignedExt getDeviceSigned() {
        return (DeviceSignedExt) deviceSigned;
    }

    @SneakyThrows
    public void assertSignatureValid(SessionTranscript sessionTranscript) {
        var signature = getDeviceSigned().getDeviceSignature();
        var deviceKeyInfo = (DeviceKeyInfoExt) getIssuerSigned().getMobileSecurityObject().getDeviceKeyInfo();
        var verifier = new COSEVerifier(deviceKeyInfo.getDeviceKey().createPublicKey());

        var deviceAuthentication = new DeviceAuthentication(sessionTranscript, this);

        final byte[] encode = deviceAuthentication.encode();
        signature.setPayload(encode);
        var result = verifier.verify(signature);

        if (!result) {
            throw new SecurityException("Device signature invalid");
        }
    }


}
