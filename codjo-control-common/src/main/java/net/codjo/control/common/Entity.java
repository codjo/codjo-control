/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import net.codjo.control.common.util.EntityHelper;
/**
 * Represente la balise entity dans le fichier xml du plan d'intégration.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.1.1.1 $
 */
public class Entity {
    private String beanClassName;
    private String batchClassName;
    private EntityHelper helper;
    private EntityHelper helperForBatch;

    public Entity() {}

    public void setBatchClassName(String batchClassName) {
        this.batchClassName = batchClassName;
    }


    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }


    public String getBatchClassName() {
        return batchClassName;
    }


    public String getBeanClassName() {
        return beanClassName;
    }


    public EntityHelper getEntityHelper() {
        if (helper == null) {
            helper = new EntityHelper();
            helper.setBeanClassName(getBeanClassName());
        }
        return helper;
    }


    public EntityHelper getEntityHelperForBatch() {
        if (helperForBatch == null) {
            if (getBatchClassName() == null) {
                helperForBatch = getEntityHelper();
            }
            else {
                helperForBatch = new EntityHelper();
                helperForBatch.setBeanClassName(getBatchClassName());
            }
        }
        return helperForBatch;
    }
}
