package net.codjo.control.server.plugin;
import java.text.MessageFormat;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobAudit;

import static net.codjo.i18n.common.plugin.InternationalizationUtil.translate;
/**
 *
 */
public class PostControlAudit {
    private static final String VALID_LINE_COUNT = "valid-line-count";
    private static final String BAD_LINE_COUNT = "bad-line-count";
    private int validLineCount = -1;
    private int badLineCount = -1;


    public PostControlAudit() {
    }


    public PostControlAudit(int validLineCount, int badLineCount) {
        this.validLineCount = validLineCount;
        this.badLineCount = badLineCount;
    }


    public PostControlAudit(JobAudit jobAudit) {
        Arguments arguments = jobAudit.getArguments();

        validLineCount = Integer.parseInt(arguments.get(VALID_LINE_COUNT));
        badLineCount = Integer.parseInt(arguments.get(BAD_LINE_COUNT));
    }


    public int getValidLineCount() {
        return validLineCount;
    }


    public void setValidLineCount(int validLineCount) {
        this.validLineCount = validLineCount;
    }


    public int getBadLineCount() {
        return badLineCount;
    }


    public void setBadLineCount(int badLineCount) {
        this.badLineCount = badLineCount;
    }


    public void fill(JobAudit jobAudit) {
        Arguments arguments = new Arguments();

        arguments.put(VALID_LINE_COUNT, Integer.toString(validLineCount));
        arguments.put(BAD_LINE_COUNT, Integer.toString(badLineCount));

        jobAudit.setArguments(arguments);

        if (getBadLineCount() > 0) {
            jobAudit.setWarningMessage(MessageFormat.format(translate("PostControlAudit.message"), getBadLineCount()));
        }
    }
}
