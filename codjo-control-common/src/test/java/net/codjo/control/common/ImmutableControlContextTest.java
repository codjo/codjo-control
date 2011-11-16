/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.lang.reflect.Method;
import junit.framework.TestCase;
import net.codjo.test.common.mock.ConnectionMock;
/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.3 $
 */
public class ImmutableControlContextTest extends TestCase {
    private ImmutableControlContext immutable;
    private ControlContext context;

    public void test_delegateAll() throws Exception {
        Method[] imMeth = ImmutableControlContext.class.getMethods();
        for (Method method : imMeth) {
            if (method.getDeclaringClass() != Object.class
                && method.getDeclaringClass() != ImmutableControlContext.class) {
                fail("La méthode doit être déclaré dans ImmutableControlContext "
                     + "pour s'assurer de l'aspect immutable et de la délégation! "
                     + method);
            }
        }
    }


    public void test_delegate() throws Exception {
        context.setCurrentRequestId("id");
        context.setUser("bobo");

        assertEquals("id", immutable.getCurrentRequestId());
        assertEquals("bobo", immutable.getUser());
    }


    public void test_immutable() throws Exception {
        try {
            immutable.setCurrentRequestId("id");
            fail("Methode immutable");
        }
        catch (UnsupportedOperationException ex) {
            ;
        }

        try {
            immutable.setUser("bobo");
            fail("Methode immutable");
        }
        catch (UnsupportedOperationException ex) {
            ;
        }

        try {
            immutable.setConnection(new ConnectionMock());
            fail("Methode immutable");
        }
        catch (UnsupportedOperationException ex) {
            ;
        }
    }


    @Override
    protected void setUp() throws Exception {
        context = new ControlContext();
        immutable = new ImmutableControlContext(context);
    }
}
