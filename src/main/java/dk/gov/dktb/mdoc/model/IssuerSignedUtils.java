package dk.gov.dktb.mdoc.model;

import com.authlete.cose.COSESign1;
import com.authlete.mdoc.IssuerSigned;
import com.authlete.mdoc.MobileSecurityObject;
import lombok.val;

public class IssuerSignedUtils {
    public static MobileSecurityObject getMso(IssuerSigned signed) {
        val signature = (COSESign1)signed.getPairs().get(1).getValue();

        val payload = signature.getPayload();

        return null;
    }
}
