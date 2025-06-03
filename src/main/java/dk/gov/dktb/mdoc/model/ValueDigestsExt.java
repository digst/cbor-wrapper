package dk.gov.dktb.mdoc.model;

import com.authlete.mdoc.DigestIDs;
import com.authlete.mdoc.DigestIDsEntry;
import com.authlete.mdoc.ValueDigests;
import com.authlete.mdoc.ValueDigestsEntry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class ValueDigestsExt extends ValueDigests {
    private List<? extends ValueDigestsEntry> entries;
    public ValueDigestsExt(List<? extends ValueDigestsEntry> entries) {
        super(entries);
        this.entries = entries;
    }

    public static ValueDigestsExt from(Map<String, Object> cbor) {
        var entries = new ArrayList<ValueDigestsEntry>();
        for (Map.Entry<String, Object> entry : cbor.entrySet()) {
            var digestIds = new ArrayList<DigestIDsEntry>();
            var values = (Map<Integer, byte[]>)entry.getValue();
            for(var id : values.keySet()) {
                digestIds.add(new DigestIDsEntry(id, values.get(id)));
            }
            entries.add(new ValueDigestsEntry(entry.getKey(), new DigestIDs(digestIds)));
        }
        return new ValueDigestsExt(entries);
    }
}
