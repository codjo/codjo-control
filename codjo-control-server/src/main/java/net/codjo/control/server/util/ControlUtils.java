package net.codjo.control.server.util;
import net.codjo.control.common.ControlException;
import net.codjo.mad.server.handler.HandlerException;
import net.codjo.util.date.DateUtil;
import java.text.MessageFormat;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
/**
 *
 */
public class ControlUtils {

    private final static String MISSING_REQUIRED_FIELD = "Le champ ''{0}'' est obligatoire.";


    private ControlUtils() {
    }


    public static void checkFieldNotNull(Object fieldValue, int errorNumber, String field)
          throws ControlException {
        if (fieldValue == null) {
            throw new ControlException(errorNumber, MessageFormat.format(MISSING_REQUIRED_FIELD, field));
        }
    }


    public static void checkFieldNotBlank(String fieldValue, int errorNumber, String field)
          throws ControlException {
        if (StringUtils.isBlank(fieldValue)) {
            throw new ControlException(errorNumber, MessageFormat.format(MISSING_REQUIRED_FIELD, field));
        }
    }


    public static void checkFieldStrictlyGreaterThanThreshold(BigDecimal fieldValue,
                                                              double threshold,
                                                              int errorNumber,
                                                              String errorMessage) throws ControlException {
        if (fieldValue != null && fieldValue.doubleValue() <= threshold) {
            throw new ControlException(errorNumber, errorMessage);
        }
    }


    public static void checkFieldStrictlyInsideBounds(BigDecimal fieldValue,
                                                      double minThreshold,
                                                      double maxThreshold,
                                                      int errorNumber,
                                                      String errorMessage) throws ControlException {
        if (fieldValue != null && (fieldValue.doubleValue() < minThreshold
                                   || fieldValue.doubleValue() > maxThreshold)) {
            throw new ControlException(errorNumber, errorMessage);
        }
    }


    public static void checkFirstValueLowerThanSecondValue(BigDecimal firstValue, BigDecimal secondValue,
                                                           int errorNumber,
                                                           String errorMessage) throws ControlException {
        if (firstValue != null && secondValue != null &&
            (firstValue.doubleValue() > secondValue.doubleValue())) {
            throw new ControlException(errorNumber, errorMessage);
        }
    }


    public static void checkFirstDateNotNullIfSecondDateNotNull(Date firstDate,
                                                                Date secondDate,
                                                                String firstDateName,
                                                                String secondDateName,
                                                                int errorCode) throws ControlException {
        if (firstDate == null && secondDate != null) {
            throw new ControlException(errorCode,
                                       "La " + secondDateName + " ne peut être remplie si la "
                                       + firstDateName + " est nulle.");
        }
    }


    public static void checkSecondDateAfterFirstDate(Date firstDate,
                                                     Date secondDate,
                                                     String firstDateName,
                                                     String secondDateName,
                                                     int errorCode) throws ControlException {

        if (firstDate != null && secondDate != null && firstDate.after(secondDate)) {
            throw new ControlException(errorCode,
                                       "Veuillez saisir une " + secondDateName +
                                       " supérieure à la " + firstDateName + ".");
        }
    }


    public static void checkDateInsideBounds(Date beginDate, Date shiftDate, Date endDate)
          throws HandlerException {
        java.util.Date shiftedEndDate = DateUtil.shiftDate(endDate, -1);
        if (beginDate == null) {
            if (shiftDate.after(shiftedEndDate)) {
                throw new HandlerException("La date de recalage doit être inférieure ou égale au "
                                           + DateUtil.getFrenchDate(shiftedEndDate) + ".");
            }
        }
        else {
            Date shiftedBeginDate = DateUtil.shiftDate(beginDate, 1);
            if (shiftDate.after(shiftedEndDate) || shiftDate.before(shiftedBeginDate)) {
                throw new HandlerException("La date de recalage doit être comprise entre le "
                                           + DateUtil.getFrenchDate(shiftedBeginDate)
                                           + " et le " + DateUtil.getFrenchDate(shiftedEndDate) + ".");
            }
        }
    }
}
