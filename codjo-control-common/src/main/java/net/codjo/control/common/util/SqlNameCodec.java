package net.codjo.control.common.util;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 *
 */
public class SqlNameCodec {
    private SqlNameCodec() {
    }


    public static String encodeList(List<String> names) {
        if (names == null) {
            return null;
        }
        return names.toString();
    }


    public static List<String> decodeList(String encodedValue) {
        if (encodedValue == null) {
            return Collections.emptyList();
        }

        String contentWithoutBracket = encodedValue.substring(1, encodedValue.length() - 1);
        if (contentWithoutBracket.trim().length() == 0) {
            return Collections.emptyList();
        }

        String[] split = contentWithoutBracket.split(", ");
        return Arrays.asList(split);
    }
}
