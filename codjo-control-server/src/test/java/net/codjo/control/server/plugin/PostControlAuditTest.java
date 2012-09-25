package net.codjo.control.server.plugin;
import junit.framework.TestCase;
import net.codjo.control.server.i18n.InternationalizationFixture;
import net.codjo.i18n.common.Language;
import net.codjo.workflow.common.message.JobAudit;
/**
 * Classe de test de {@link PostControlAudit}.
 */
public class PostControlAuditTest extends TestCase {
    private PostControlAudit audit;
    private InternationalizationFixture i18nFixture = new InternationalizationFixture();


    public void test_constructor() throws Exception {
        assertEquals(0, new PostControlAudit(0, 1).getValidLineCount());
        assertEquals(1, new PostControlAudit(0, 1).getBadLineCount());
    }


    public void test_default() throws Exception {
        assertEquals(-1, audit.getValidLineCount());
        assertEquals(-1, audit.getBadLineCount());
    }


    public void test_setters() throws Exception {
        audit.setValidLineCount(5);
        assertEquals(5, audit.getValidLineCount());

        audit.setBadLineCount(15);
        assertEquals(15, audit.getBadLineCount());
    }


    public void test_fillJobAuditVariables() throws Exception {
        audit.setValidLineCount(5);
        audit.setBadLineCount(15);

        JobAudit jobAudit = new JobAudit();
        audit.fill(jobAudit);

        assertEquals(5, new PostControlAudit(jobAudit).getValidLineCount());
        assertEquals(15, new PostControlAudit(jobAudit).getBadLineCount());
    }


    public void test_noBadLines() throws Exception {
        audit.setValidLineCount(5);
        audit.setBadLineCount(0);

        JobAudit jobAudit = new JobAudit();
        audit.fill(jobAudit);

        assertEquals(JobAudit.Status.OK, jobAudit.getStatus());
    }


    public void test_warning() throws Exception {
        audit.setValidLineCount(5);
        audit.setBadLineCount(10);

        JobAudit jobAudit = new JobAudit();
        audit.fill(jobAudit);

        assertEquals(JobAudit.Status.WARNING, jobAudit.getStatus());
        assertEquals("Il y a 10 ligne(s) placées en quarantaine.", jobAudit.getWarningMessage());

        i18nFixture.setLanguage(Language.EN);

        jobAudit = new JobAudit();
        audit.fill(jobAudit);

        assertEquals(JobAudit.Status.WARNING, jobAudit.getStatus());
        assertEquals("There is(are) 10 line(s) in quarantine.", jobAudit.getWarningMessage());
    }


    public void test_warning_defaultValue() throws Exception {
        audit.setValidLineCount(5);
        audit.setBadLineCount(-1);

        JobAudit jobAudit = new JobAudit();
        audit.fill(jobAudit);

        assertEquals(JobAudit.Status.OK, jobAudit.getStatus());
    }


    @Override
    protected void setUp() throws Exception {
        i18nFixture.doSetUp();
        audit = new PostControlAudit();
    }


    @Override
    protected void tearDown() throws Exception {
        i18nFixture.doTearDown();
    }
}
