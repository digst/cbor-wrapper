package dk.gov.dktb.mdoc.model;

import com.authlete.cose.COSESign1;
import com.authlete.mdoc.DeviceAuth;
import lombok.Getter;
import lombok.val;

import java.util.List;
import java.util.Map;

@Getter
public class DeviceAuthExt extends DeviceAuth {
    private COSESign1 signature;

    public DeviceAuthExt(COSESign1 deviceSignature) {
        super(deviceSignature);
        this.signature = deviceSignature;
    }

    public static DeviceAuthExt from(Map<String, Object> deviceAuth) {
        val signature = COSESign1Ext.from((List<Object>) deviceAuth.get("deviceSignature"));
        return new DeviceAuthExt(signature);
    }

}
