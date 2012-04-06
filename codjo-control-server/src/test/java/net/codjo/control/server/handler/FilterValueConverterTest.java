package net.codjo.control.server.handler;
import java.math.BigDecimal;
import java.sql.Timestamp;
import net.codjo.control.common.util.FilterConstants;
import org.junit.Test;

import static net.codjo.test.common.matcher.JUnitMatchers.*;
/**
 *
 */
public class FilterValueConverterTest {

    @Test
    public void test_convertString_AllCase() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(String.class, FilterConstants.ALL),
                   is(FilterConstants.ALL));
    }


    @Test
    public void test_convertInteger() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(Integer.class, "5"),
                   is(5));
        assertThat(FilterValueConverter.convertFromStringValue(int.class, "5"),
                   is(5));
    }


    @Test
    public void test_convertInteger_AllCase() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(Integer.class, FilterConstants.ALL),
                   is(-1));
        assertThat(FilterValueConverter.convertFromStringValue(int.class, FilterConstants.ALL),
                   is(-1));
    }


    @Test
    public void test_convertDate() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(java.sql.Date.class, "2012-01-30"),
                   is(java.sql.Date.valueOf("2012-01-30")));
        assertThat(FilterValueConverter.convertFromStringValue(java.util.Date.class, "2012-01-30").getTime(),
                   is(java.sql.Date.valueOf("2012-01-30").getTime()));
    }


    @Test
    public void test_convertDate_AllCase() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(java.sql.Date.class, FilterConstants.ALL),
                   is(java.sql.Date.valueOf("9999-12-31")));

        assertThat(FilterValueConverter.convertFromStringValue(java.util.Date.class, FilterConstants.ALL).getTime(),
                   is(java.sql.Date.valueOf("9999-12-31").getTime()));
    }


    @Test
    public void test_convertBigDecimal() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(BigDecimal.class, "5"),
                   is(new BigDecimal("5")));
    }


    @Test
    public void test_convertBigDecimal_AllCase() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(BigDecimal.class, FilterConstants.ALL),
                   is(new BigDecimal("-1")));
    }


    @Test
    public void test_convertTimestamp() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(Timestamp.class, "1998-12-30 10:00:00"),
                   is(Timestamp.valueOf("1998-12-30 10:00:00")));
    }


    @Test
    public void test_convertTimestamp_AllCase() throws Exception {
        assertThat(FilterValueConverter.convertFromStringValue(Timestamp.class, FilterConstants.ALL),
                   is(Timestamp.valueOf("9999-12-31 00:00:00")));
    }
}
