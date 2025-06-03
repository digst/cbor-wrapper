package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORByteArray;
import com.authlete.cbor.CBORItem;
import com.authlete.cbor.CBORParser;
import com.authlete.mdoc.DeviceKeyInfo;
import com.authlete.mdoc.MobileSecurityObject;
import com.authlete.mdoc.ValidityInfo;
import com.authlete.mdoc.ValueDigests;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.util.Map;

@Getter
public class MobileSecurityObjectExt extends MobileSecurityObject {
    private static final String VALUE_DIGESTS = "valueDigests";
    private static final String DEVICE_KEY_INFO = "deviceKeyInfo";
    private static final String VALIDITY_INFO = "validityInfo";

    private ValidityInfo validityInfo;
    private DeviceKeyInfo deviceKeyInfo;
    private String docType;
    private ValueDigests valueDigests;
    private String version;
    private String digestAlgorithm;

    public MobileSecurityObjectExt(ValueDigests valueDigests, DeviceKeyInfo deviceKeyInfo, String docType, ValidityInfo validityInfo) {
        super(valueDigests, deviceKeyInfo, docType, validityInfo);
        this.docType = docType;
        this.validityInfo = validityInfo;
        this.deviceKeyInfo = deviceKeyInfo;
        this.valueDigests = valueDigests;
    }

    public MobileSecurityObjectExt(String version, String digestAlgorithm, ValueDigests valueDigests, DeviceKeyInfo deviceKeyInfo, String docType, ValidityInfo validityInfo) {
        super(version, digestAlgorithm, valueDigests, deviceKeyInfo, docType, validityInfo);
        this.docType = docType;
        this.validityInfo = validityInfo;
        this.deviceKeyInfo = deviceKeyInfo;
        this.valueDigests = valueDigests;
        this.version = version;
        this.digestAlgorithm = digestAlgorithm;
    }

    @SneakyThrows
    public static MobileSecurityObjectExt fromCbor(CBORItem payload) {
        val parser = new CBORParser(((CBORByteArray) payload).getValue());
        val map = (Map<String, Object>) (new CBORParser((byte[]) parser.next())).next();

        return new MobileSecurityObjectExt(
                (String) map.get("version"),
                (String) map.get("digestAlgorithm"),
                ValueDigestsExt.from((Map<String, Object>) map.get(VALUE_DIGESTS)),
                DeviceKeyInfoExt.from((Map<String, Object>) map.get(DEVICE_KEY_INFO)),
                (String) map.get("docType"),
                ValidityInfoExt.from((Map<String, Object>) map.get(VALIDITY_INFO)));
    }
}
