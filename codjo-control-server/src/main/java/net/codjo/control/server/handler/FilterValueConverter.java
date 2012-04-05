package net.codjo.control.server.handler;
import java.math.BigDecimal;
import java.sql.Timestamp;
import net.codjo.control.common.util.FilterConstants;
import net.codjo.mad.server.handler.XMLUtils;
/**
 *
 */
public class FilterValueConverter {

    private FilterValueConverter() {
    }


    public static <T> T convertFromStringValue(Class<T> propertyClass, String value) {
        if (isNumeric(propertyClass) && FilterConstants.ALL.equals(value)) {
            value = "-1";
        }
        if (isDate(propertyClass) && FilterConstants.ALL.equals(value)) {
            value = "9999-12-31";
        }
        if (propertyClass == Timestamp.class && FilterConstants.ALL.equals(value)) {
            value = "9999-12-31 23:59:59";
        }
        return XMLUtils.convertFromStringValue(propertyClass, value);
    }


    private static <T> boolean isDate(Class<T> propertyClass) {
        return propertyClass == java.sql.Date.class
               || propertyClass == java.util.Date.class;
    }


    private static <T> boolean isNumeric(Class<T> propertyClass) {
        return propertyClass == Integer.class
               || propertyClass == int.class
               || propertyClass == Double.class
               || propertyClass == double.class
               || propertyClass == BigDecimal.class;
    }
}
