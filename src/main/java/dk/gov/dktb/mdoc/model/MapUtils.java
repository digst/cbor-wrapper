package dk.gov.dktb.mdoc.model;

import java.util.Map;

public class MapUtils {
    public static Map<String, Object> getMap(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }
}
