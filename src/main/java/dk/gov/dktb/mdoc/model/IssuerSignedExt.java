package dk.gov.dktb.mdoc.model;

import com.authlete.cose.COSESign1;
import com.authlete.cose.COSEVerifier;
import com.authlete.mdoc.IssuerNameSpaces;
import com.authlete.mdoc.IssuerSigned;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.security.Key;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class IssuerSignedExt extends IssuerSigned {
    private IssuerNameSpaces nameSpaces;
    private COSESign1 issuerAuth;
    private MobileSecurityObjectExt mobileSecurityObject;

    public IssuerSignedExt(IssuerNameSpaces nameSpaces, COSESign1 issuerAuth) {
        super(nameSpaces, issuerAuth);
        this.nameSpaces = nameSpaces;
        this.issuerAuth = issuerAuth;
        this.mobileSecurityObject = MobileSecurityObjectExt.fromCbor(issuerAuth.getPayload());
    }

    public static IssuerSignedExt from(Map<String, Object> cbor) {
        var issuerNameSpaces = IssuerNameSpacesExt.from((Map<String, Object>) cbor.get("nameSpaces"));
        var issuerAuth = COSESign1Ext.from((List<Object>) cbor.get("issuerAuth"));
        return new IssuerSignedExt(issuerNameSpaces, issuerAuth);
    }

    @SneakyThrows
    public void assertSignatureValid() {
        var verifier = new COSEVerifier(getSigningKey());

        if (issuerAuth.getUnprotectedHeader().getAlg() == null && issuerAuth.getProtectedHeader().getAlg() == null) {
        }
        if(!verifier.verify(issuerAuth)) {
            throw new SecurityException("Issuer signature is invalid");
        }
    }

    private Key getSigningKey() {
        return ((COSESign1Ext) issuerAuth).getSigningCertificate().getPublicKey();
    }

}
