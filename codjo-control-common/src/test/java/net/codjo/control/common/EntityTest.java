/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import net.codjo.control.common.util.EntityHelper;
import junit.framework.TestCase;
/**
 * Classe de test de Entity.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.3 $
 */
public class EntityTest extends TestCase {
    private Entity entity;

    public EntityTest(String testName) {
        super(testName);
    }

    public void test_getEntityHelperX_NoBatchClass() {
        entity.setBeanClassName(Query.class.getName());

        EntityHelper helper = entity.getEntityHelper();
        EntityHelper batchHelper = entity.getEntityHelperForBatch();
        assertEquals("helper identique", helper, batchHelper);
        assertEquals("helper sur la bonne classe.", Query.class.getName(),
            helper.getBeanClassName());
    }


    public void test_getEntityHelperX_helperBuildOnlyOnce() {
        entity.setBeanClassName(Query.class.getName());

        assertNotNull(entity.getEntityHelper());
        assertEquals("helper construit une seul fois", entity.getEntityHelper(),
            entity.getEntityHelper());
    }


    public void test_getEntityHelperX_WithBatchClass() {
        entity.setBeanClassName(Query.class.getName());
        entity.setBatchClassName(Parameter.class.getName());

        EntityHelper helper = entity.getEntityHelper();
        EntityHelper batchHelper = entity.getEntityHelperForBatch();

        assertTrue("helper ne sont pas identique", helper != batchHelper);
        assertEquals("helper sur la bonne classe.", Query.class.getName(),
            helper.getBeanClassName());
        assertEquals("Batchhelper sur la bonne classe.", Parameter.class.getName(),
            batchHelper.getBeanClassName());
    }


    @Override
    protected void setUp() throws Exception {
        entity = new Entity();
    }
}
