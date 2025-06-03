package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORByteArray;
import com.authlete.cbor.CBORInteger;
import com.authlete.cbor.CBORPair;
import com.authlete.cose.COSEEC2Key;
import com.authlete.cose.COSEKey;
import com.authlete.mdoc.DeviceKeyInfo;
import com.authlete.mdoc.KeyAuthorizations;
import com.authlete.mdoc.KeyInfo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Map;

@Getter
public class DeviceKeyInfoExt extends DeviceKeyInfo {
    private COSEKey deviceKey;
    public DeviceKeyInfoExt(COSEKey deviceKey, KeyAuthorizations keyAuthorizations, KeyInfo keyInfo) {
        super(deviceKey, keyAuthorizations, keyInfo);
        this.deviceKey = deviceKey;
    }

    public static DeviceKeyInfoExt from(Map<String, Object> cbor) {
        var map = (Map<Integer, Object>)cbor.get("deviceKey");
        var pairs = new ArrayList<CBORPair>();
        for (var entry : map.entrySet()) {
            var item = entry.getValue() instanceof Integer ?
                    new CBORInteger((Integer)entry.getValue()) : new CBORByteArray((byte[]) entry.getValue());
            pairs.add(new CBORPair(new CBORInteger(entry.getKey()), item));
        }
        return new DeviceKeyInfoExt(new COSEEC2Key(pairs), null, null);
    }
}
