package net.codjo.control.common.i18n;
import net.codjo.i18n.common.Language;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.i18n.common.plugin.InternationalizationPlugin;
import net.codjo.test.common.fixture.Fixture;
/**
 *
 */
public class InternationalizationFixture implements Fixture {
    public void doSetUp() throws Exception {
        InternationalizationPlugin i18nPlugin = new InternationalizationPlugin();
        TranslationManager translationManager = i18nPlugin.getConfiguration().getTranslationManager();
        translationManager.addBundle("net.codjo.control.common.i18n", Language.FR);
        translationManager.addBundle("net.codjo.control.common.i18n", Language.EN);
    }


    public void doTearDown() throws Exception {
    }
}
