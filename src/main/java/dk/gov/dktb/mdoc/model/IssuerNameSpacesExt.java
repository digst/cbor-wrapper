package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORParser;
import com.authlete.mdoc.IssuerNameSpaces;
import com.authlete.mdoc.IssuerNameSpacesEntry;
import com.authlete.mdoc.IssuerSignedItem;
import com.authlete.mdoc.IssuerSignedItemBytes;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IssuerNameSpacesExt extends IssuerNameSpaces {
    public IssuerNameSpacesExt(List<? extends IssuerNameSpacesEntry> entries) {
        super(entries);
    }

    @SneakyThrows
    public static IssuerNameSpacesExt from(Map<String, Object> nameSpaces) {
        val result = new ArrayList<IssuerNameSpacesEntry>();
        for (Map.Entry<String, Object> entry : nameSpaces.entrySet()) {
            val vals = new ArrayList<IssuerSignedItemBytes>();
            val values = (List<byte[]>) entry.getValue();
            for (val value : values) {
                try {
                    val parser = new CBORParser(value);
                    val next = (Map<String, Object>) parser.next();
                    val item = new IssuerSignedItem((Integer) next.get("digestID"), (byte[]) next.get("random"), (String) next.get("elementIdentifier"), next.get("elementValue"));
                    vals.add(new IssuerSignedItemBytes(item));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            result.add(new IssuerNameSpacesEntry(entry.getKey(), vals));
        }
        return new IssuerNameSpacesExt(result);
    }

}
