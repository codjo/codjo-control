/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import junit.framework.TestCase;
import org.easymock.MockControl;
/**
 * Description of the Class
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.2 $
 */
public class EntityIteratorImplTest extends TestCase {
    private MockEntityHelper helper;
    private EntityIteratorImpl iterator;
    private ResultSet rs;
    private MockControl rsControler;
    private Statement stmt;
    private MockControl stmtControler;


    public EntityIteratorImplTest(String testCaseName) {
        super(testCaseName);
    }


    public void test_close() throws Exception {
        helper = new MockEntityHelper();

        stmt.close();
        stmtControler.setVoidCallable(1);

        rsControler.replay();
        stmtControler.replay();

        iterator = new EntityIteratorImpl(helper, rs, stmt);
        iterator.close();

        rsControler.verify();
        stmtControler.verify();
    }


    public void test_hasNext() throws Exception {
        helper = new MockEntityHelper();

        rs.next();
        rsControler.setReturnValue(true, 2);
        rs.next();
        rsControler.setReturnValue(false, 1);

        rsControler.replay();

        iterator = new EntityIteratorImpl(helper, rs, stmt);

        assertTrue("Premiere ligne", iterator.hasNext());
        assertTrue("Toujours la premiere ligne", iterator.hasNext());

        iterator.next();

        assertTrue("Deuxieme ligne", iterator.hasNext());
        assertTrue("Toujours une Deuxieme ligne", iterator.hasNext());

        iterator.next();

        assertTrue("Plus de ligne", !iterator.hasNext());
        assertTrue("Vraiment plus aucune ligne ", !iterator.hasNext());

        rsControler.verify();

        assertTrue("new object ", helper.newObjectFromCalled);
    }


    public void test_update() throws Exception {
        helper = new MockEntityHelper();

        rs.next();
        rsControler.setReturnValue(true, 1);

        // Cet appel rs.next() est fait par l'update pour s'assurer
        // que l'ordre d'appel est correcte
        // cf. http://4ddev02/vqwiki/jsp/Wiki?EasyMock
        rs.next();
        rsControler.setThrowable(new RuntimeException("Normalement capturé par l'update"),
                                 1);
        rs.updateRow();
        rsControler.setVoidCallable(1);

        rs.next();
        rsControler.setReturnValue(false, 1);

        rsControler.replay();

        iterator = new EntityIteratorImpl(helper, rs, stmt);
        assertTrue(iterator.hasNext());
        Object obj = iterator.next();

        // Maj de obj par des contrôles
        iterator.update(obj, null);

        assertTrue("Plus de ligne", !iterator.hasNext());

        rsControler.verify();
    }


    @Override
    protected void setUp() {
        rsControler = MockControl.createControl(ResultSet.class);
        rs = (ResultSet)rsControler.getMock();
        stmtControler = MockControl.createControl(Statement.class);
        stmt = (Statement)stmtControler.getMock();
    }


    @Override
    protected void tearDown() {
    }


    private class MockEntityHelper extends EntityHelper {
        boolean newObjectFromCalled = false;
        int newObjectFromCalledNb = 0;


        @Override
        Object newObjectFrom(ResultSet rs) throws SQLException {
            newObjectFromCalled = true;
            newObjectFromCalledNb++;
            return new Object();
        }


        @Override
        void updateResultSet(ResultSet rs, Object obj, EntityResultState ctrl)
              throws SQLException {
//            void updateResultSet(ResultSet rs, Object obj) throws SQLException {
            try {
                rs.next();
            }
            catch (RuntimeException ex) {
                // Exception normal - Pour verifier l'ordre d'appel
            }
        }
    }
}
