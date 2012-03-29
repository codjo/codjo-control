package net.codjo.control.server.util;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import net.codjo.control.common.ControlException;
import net.codjo.control.common.i18n.InternationalizationFixture;
import net.codjo.mad.server.handler.HandlerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MARCH;
import static java.util.Calendar.getInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
/**
 *
 */
public class ControlUtilsTest {
    private InternationalizationFixture i18nFixture = new InternationalizationFixture();


    @Before
    public void setUp() throws Exception {
        i18nFixture.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        i18nFixture.doTearDown();
    }


    @Test
    public void checkFieldNotNullOK() throws Exception {
        ControlUtils.checkFieldNotNull(new Object(), 1, "object");
    }


    @Test
    public void checkFieldNotNullKO() {
        try {
            ControlUtils.checkFieldNotNull(null, 1, "object");
            fail();
        }
        catch (ControlException e) {
            assertEquals("Erreur 1 - Le champ 'object' est obligatoire.", e.getMessage());
        }
    }


    @Test
    public void checkFieldNotBlankOK() throws ControlException {
        ControlUtils.checkFieldNotBlank("  12   \t", 1, "object");
    }


    @Test
    public void checkFieldNotBlankKO() {
        try {
            ControlUtils.checkFieldNotBlank("  ", 1, "object");
            fail();
        }
        catch (ControlException e) {
            assertEquals("Erreur 1 - Le champ 'object' est obligatoire.", e.getMessage());
        }
    }


    @Test
    public void checkFieldStrictlyGreaterThanThresholdOK() throws ControlException {
        ControlUtils.checkFieldStrictlyGreaterThanThreshold(BigDecimal.valueOf(12.0),
                                                            10.0,
                                                            1,
                                                            "my error message");
        ControlUtils.checkFieldStrictlyGreaterThanThreshold(null, 10.0, 1, "my error message");
    }


    @Test
    public void checkFieldStrictlyGreaterThanThresholdKO() {
        try {
            ControlUtils.checkFieldStrictlyGreaterThanThreshold(BigDecimal.valueOf(12.0),
                                                                26.0,
                                                                1,
                                                                "my error message");
            fail();
        }
        catch (ControlException e) {
            assertEquals("Erreur 1 - my error message", e.getMessage());
        }
    }


    @Test
    public void checkFieldStrictlyInsideBoundsOK() throws ControlException {
        ControlUtils.checkFieldStrictlyInsideBounds(BigDecimal.valueOf(12.0),
                                                    10.0,
                                                    200.0,
                                                    1,
                                                    "my error message");
        ControlUtils.checkFieldStrictlyInsideBounds(null, 10.0, 200.0, 1, "my error message");
    }


    @Test
    public void checkFieldStrictlyInsideBoundsKOTooSmall() {
        try {
            ControlUtils.checkFieldStrictlyInsideBounds(BigDecimal.valueOf(12.0),
                                                        26.0,
                                                        200.0,
                                                        1,
                                                        "my error message");
            fail();
        }
        catch (ControlException e) {
            assertEquals("Erreur 1 - my error message", e.getMessage());
        }
    }


    @Test
    public void checkFieldStrictlyInsideBoundsKOTooBig() {
        try {
            ControlUtils.checkFieldStrictlyInsideBounds(BigDecimal.valueOf(300.0),
                                                        26.0,
                                                        200.0,
                                                        1,
                                                        "my error message");
            fail();
        }
        catch (ControlException e) {
            assertEquals("Erreur 1 - my error message", e.getMessage());
        }
    }


    @Test
    public void checkFirstValueLowerThanSecondValueOK() throws ControlException {
        ControlUtils.checkFirstValueLowerThanSecondValue(BigDecimal.valueOf(200.0), BigDecimal.valueOf(200.0),
                                                         1, "my error message");
        ControlUtils.checkFirstValueLowerThanSecondValue(BigDecimal.valueOf(200.0), null,
                                                         1, "my error message");
        ControlUtils.checkFirstValueLowerThanSecondValue(null, BigDecimal.valueOf(200.0),
                                                         1, "my error message");
        ControlUtils.checkFirstValueLowerThanSecondValue(BigDecimal.valueOf(200.0), BigDecimal.valueOf(300.0),
                                                         1, "my error message");
    }


    @Test
    public void checkFirstValueLowerThanSecondValueKO() {
        try {
            ControlUtils.checkFirstValueLowerThanSecondValue(BigDecimal.valueOf(200.0),
                                                             BigDecimal.valueOf(100.0),
                                                             1,
                                                             "my error message");
            fail();
        }
        catch (ControlException e) {
            assertEquals("Erreur 1 - my error message", e.getMessage());
        }
    }


    @Test
    public void checkFirstDateNotNullIfSecondDateNotNullOK() throws ControlException {
        Calendar calendar = getInstance();
        calendar.set(2000, JANUARY, 1);
        Date firstDate = calendar.getTime();

        calendar.set(2002, MARCH, 5);
        Date secondDate = calendar.getTime();

        ControlUtils.checkFirstDateNotNullIfSecondDateNotNull(firstDate, secondDate, "<première date>",
                                                              "<seconde date>", 1);

        ControlUtils.checkFirstDateNotNullIfSecondDateNotNull(firstDate, null, "<première date>",
                                                              "<seconde date>", 1);

        ControlUtils.checkFirstDateNotNullIfSecondDateNotNull(null, null, "<première date>",
                                                              "<seconde date>", 1);
    }


    @Test
    public void checkFirstDateNotNullIfSecondDateNotNullKO() {
        Calendar calendar = getInstance();
        calendar.set(2002, MARCH, 5);

        Date secondDate = calendar.getTime();

        try {
            ControlUtils.checkFirstDateNotNullIfSecondDateNotNull(null, secondDate, "<première date>",
                                                                  "<seconde date>", 1);
            fail();
        }
        catch (ControlException e) {
            assertEquals("Erreur 1 - La <seconde date> ne peut être remplie si la <première date> est nulle.",
                         e.getMessage());
        }
    }


    @Test
    public void checkSecondDateAfterFirstDateOK() throws ControlException {
        Calendar calendar = getInstance();
        calendar.set(2000, JANUARY, 1);
        Date firstDate = calendar.getTime();

        calendar.set(2002, MARCH, 5);
        Date secondDate = calendar.getTime();

        ControlUtils.checkSecondDateAfterFirstDate(firstDate,
                                                   secondDate,
                                                   "<première date>",
                                                   "<seconde date>",
                                                   1);
        ControlUtils.checkSecondDateAfterFirstDate(firstDate, null, "<première date>", "<seconde date>", 1);
        ControlUtils.checkSecondDateAfterFirstDate(null, secondDate, "<première date>", "<seconde date>", 1);
        ControlUtils.checkSecondDateAfterFirstDate(null, null, "<première date>", "<seconde date>", 1);
    }


    @Test
    public void checkSecondDateAfterFirstDateKO() {
        Calendar calendar = getInstance();
        calendar.set(2008, JANUARY, 1);
        Date firstDate = calendar.getTime();

        calendar.set(2002, MARCH, 5);
        Date secondDate = calendar.getTime();

        try {
            ControlUtils.checkSecondDateAfterFirstDate(firstDate,
                                                       secondDate,
                                                       "<première date>",
                                                       "<seconde date>",
                                                       1);
            fail();
        }
        catch (ControlException e) {
            assertEquals("Erreur 1 - Veuillez saisir une <seconde date> supérieure à la <première date>.",
                         e.getMessage());
        }
    }


    @Test
    public void checkDateInsideBoundsOK() throws HandlerException {
        Calendar calendar = getInstance();
        calendar.set(2000, JANUARY, 1);
        Date beginDate = calendar.getTime();

        calendar.set(2001, DECEMBER, 31);
        Date shiftDate = calendar.getTime();

        calendar.set(2002, JANUARY, 1);
        Date endDate = calendar.getTime();

        ControlUtils.checkDateInsideBounds(beginDate, shiftDate, endDate);
    }


    @Test
    public void checkDateInsideBoundsKOShiftDateTooLate() {
        Calendar calendar = getInstance();
        calendar.set(2000, JANUARY, 1);
        Date beginDate = calendar.getTime();

        calendar.set(2002, JANUARY, 1);
        Date shiftDate = calendar.getTime();

        calendar.set(2002, JANUARY, 1);
        Date endDate = calendar.getTime();

        try {
            ControlUtils.checkDateInsideBounds(beginDate, shiftDate, endDate);
            fail();
        }
        catch (HandlerException exception) {
            assertEquals(
                  "La date de recalage doit être comprise entre le 02/01/2000 et le 31/12/2001.",
                  exception.getMessage());
        }
    }


    @Test
    public void checkDateInsideBoundsKOShiftDateTooEarly() {
        Calendar calendar = getInstance();
        calendar.set(2000, JANUARY, 1);
        Date beginDate = calendar.getTime();

        calendar.set(1999, DECEMBER, 31);
        Date shiftDate = calendar.getTime();

        calendar.set(2002, JANUARY, 1);
        Date endDate = calendar.getTime();

        try {
            ControlUtils.checkDateInsideBounds(beginDate, shiftDate, endDate);
            fail();
        }
        catch (HandlerException exception) {
            assertEquals(
                  "La date de recalage doit être comprise entre le 02/01/2000 et le 31/12/2001.",
                  exception.getMessage());
        }
    }


    @Test
    public void checkDateInsideBoundsKOShiftDateTooLateNoBeginDate() {
        Calendar calendar = getInstance();
        calendar.set(2002, JANUARY, 1);
        Date shiftDate = calendar.getTime();

        calendar.set(2002, JANUARY, 1);
        Date endDate = calendar.getTime();

        try {
            ControlUtils.checkDateInsideBounds(null, shiftDate, endDate);
            fail();
        }
        catch (HandlerException exception) {
            assertEquals(
                  "La date de recalage doit être inférieure ou égale au 31/12/2001.", exception.getMessage());
        }
    }
}
