package net.codjo.control.common.util;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.codjo.control.common.util.SqlNameCodec.decodeList;
import static net.codjo.control.common.util.SqlNameCodec.encodeList;
import static net.codjo.test.common.matcher.JUnitMatchers.*;
/**
 *
 */
public class SqlNameCodecTest {
    @Test
    public void test_encodeOneItem() throws Exception {
        String result = encodeList(singletonList("bobo"));
        List<String> decoded = decodeList(result);
        assertThat(decoded, is(singletonList("bobo")));
    }


    @Test
    public void test_encodeItems() throws Exception {
        String result = encodeList(asList("a", "b"));
        List<String> decoded = decodeList(result);
        assertThat(decoded, is(asList("a", "b")));
    }


    @Test
    public void test_encodeDecodeNull() throws Exception {
        String result = encodeList(null);
        List<String> decoded = decodeList(result);
        assertThat(decoded, is(Collections.<String>emptyList()));
    }
}
