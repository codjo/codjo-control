package net.codjo.control.gui.plugin;
import net.codjo.agent.UserId;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.control.gui.data.QuarantineGuiDataList;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.common.structure.TableStructure;
import net.codjo.mad.common.structure.DefaultStructureReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.TestCase;
/**
 */
public abstract class QuarantineComplianceTestCase extends TestCase {
    private String quarantineResource = "/conf/quarantine.xml";
    private String structureResource = "/conf/structure.xml";
    protected List<String> hiddenFields = new ArrayList<String>();


    protected QuarantineComplianceTestCase() {
    }


    protected QuarantineComplianceTestCase(String quarantineResource, String structureResource) {
        this.quarantineResource = quarantineResource;
        this.structureResource = structureResource;
    }


    public void test_integrity() throws Exception {
        QuarantineManager manager = new QuarantineManager(getClass().getResource(quarantineResource),
                                                          UserId.createId("user_dev", "samsung"));
        QuarantineGuiDataList list = manager.getList();

        File structure = new File(getClass().getResource(structureResource).getFile());

        StructureReader structureReader = new DefaultStructureReader(new FileReader(structure));

        for (QuarantineGuiData guiData : list.getDataList()) {
            TableStructure table = structureReader.getTableBySqlName(guiData.getQuarantine());
            compare(table, guiData);
        }
    }


    private void compare(TableStructure table, QuarantineGuiData guiData)
          throws Exception {
        Collection fieldsInGui = guiData.getDetail().getFields();
        if (fieldsInGui != null) {
            for (Object field : table.getFieldsByJavaKey().keySet()) {
                if ("quarantineId".equals(field)) {
                    continue;
                }
                else if (hiddenFields.contains(field.toString())) {
                    continue;
                }
                assertTrue("Le champ " + field
                           + " de la Quarantaine devrait etre affiché dans l'écran détail >"
                           + guiData.getName() + "<", fieldsInGui.contains(field));
            }
        }
    }
}
