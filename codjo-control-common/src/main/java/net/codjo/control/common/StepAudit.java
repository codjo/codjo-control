package net.codjo.control.common;
import java.util.Date;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
public class StepAudit {
    private static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
          .appendHours()
          .appendSuffix(" h ")
          .appendMinutes()
          .appendSuffix(" min ")
          .appendSecondsWithOptionalMillis()
          .appendSuffix(" s")
          .toFormatter();

    private Date startDate = new Date();
    private Date endDate = new Date();
    private int okRunningCount = 0;
    private int notOkRunningCount = 0;


    public int getOkRunningCount() {
        return okRunningCount;
    }


    public void incrementOkRunningCount() {
        endDate = new Date();
        okRunningCount++;
    }


    public int getNotOkRunningCount() {
        return notOkRunningCount;
    }


    public void incrementNotOkRunningCount() {
        notOkRunningCount++;
    }


    public String getOkRunningDuration() {
        return new Duration(startDate.getTime(), endDate.getTime()).toPeriod().toString(PERIOD_FORMATTER);
    }
}
