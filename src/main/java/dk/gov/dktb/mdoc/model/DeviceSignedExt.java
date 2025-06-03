package dk.gov.dktb.mdoc.model;

import com.authlete.mdoc.DeviceAuth;
import com.authlete.mdoc.DeviceNameSpaces;
import com.authlete.mdoc.DeviceNameSpacesBytes;
import com.authlete.mdoc.DeviceSigned;
import lombok.Getter;
import lombok.val;

import java.util.List;
import java.util.Map;

@Getter
public class DeviceSignedExt extends DeviceSigned {
    private DeviceNameSpacesBytes nameSpaces;
    private DeviceAuth deviceAuth;

    public DeviceSignedExt(DeviceNameSpacesBytes nameSpaces, DeviceAuth deviceAuth) {
        super(nameSpaces, deviceAuth);
        this.nameSpaces = nameSpaces;
        this.deviceAuth = deviceAuth;
    }

    public static DeviceSignedExt from(Map<String, Object> cbor) {
        //note: we don't support deviceNameSpaces parsing
        val deviceNameSpaces = new DeviceNameSpaces(List.of());
        val deviceAuth = DeviceAuthExt.from((Map<String, Object>) cbor.get("deviceAuth"));
        return new DeviceSignedExt(new DeviceNameSpacesBytes(deviceNameSpaces), deviceAuth);
    }

    public COSESign1Ext getDeviceSignature() {
        return (COSESign1Ext) ((DeviceAuthExt) deviceAuth).getSignature();
    }
}
