package dk.gov.dktb.mdoc.model;

import com.authlete.cbor.CBORParser;
import com.authlete.mdoc.DeviceResponse;
import com.authlete.mdoc.Document;
import com.authlete.mdoc.DocumentError;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.util.List;
import java.util.Map;

@Getter
public class DeviceResponseExt extends DeviceResponse {
    private String version;
    private List<Document> documents;
    private List<DocumentError> errors;
    private int status;

    public DeviceResponseExt(List<Document> documents) {
        super(documents);
        this.documents = documents;
    }

    public DeviceResponseExt(String version, List<Document> documents, List<DocumentError> documentErrors, int status) {
        super(version, documents, documentErrors, status);
        this.documents = documents;
        this.errors = documentErrors;
        this.version = version;
        this.status = status;
    }

    public DocumentExt getDocument(int index) {
        return (DocumentExt) documents.get(index);
    }

    public static DeviceResponseExt from(Map<String, Object> cbor) {
        val version = cbor.get("version");
        val status = cbor.get("status");
        val documents = (List) cbor.get("documents");
        val docs = documents.stream().map(d -> DocumentExt.from((Map<String, Object>) d)).toList();
        return new DeviceResponseExt((String) version, docs, null, (Integer) status);
    }

    @SneakyThrows
    public static DeviceResponseExt from(byte[] cbor) {
        val parser = new CBORParser(cbor);
        val list = parser.all();
        return DeviceResponseExt.from((Map<String, Object>) list.get(0));
    }

}
