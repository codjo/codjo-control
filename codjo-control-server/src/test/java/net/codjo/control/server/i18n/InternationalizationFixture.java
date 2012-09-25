package net.codjo.control.server.i18n;
import net.codjo.i18n.common.Language;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.i18n.common.plugin.InternationalizationPlugin;
import net.codjo.test.common.fixture.Fixture;
/**
 *
 */
public class InternationalizationFixture implements Fixture {
    private InternationalizationPlugin i18nPlugin = new InternationalizationPlugin();

    public void doSetUp() throws Exception {
        i18nPlugin = new InternationalizationPlugin();
        TranslationManager translationManager = i18nPlugin.getConfiguration().getTranslationManager();
        translationManager.addBundle("net.codjo.control.server.i18n", Language.FR);
        translationManager.addBundle("net.codjo.control.server.i18n", Language.EN);
    }


    public void doTearDown() throws Exception {
    }


    public void setLanguage(Language language) {
        i18nPlugin.getConfiguration().setLanguage(language);
    }
}
