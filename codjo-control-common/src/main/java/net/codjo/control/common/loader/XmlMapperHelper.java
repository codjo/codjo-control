/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.xml.XmlException;
import net.codjo.xml.easyxml.EasyXMLMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
/**
 * Classe de type helper pour encapsuler un appel a EasyXMLMapper.
 *
 * @author $Author: blazart $
 * @version $Revision: 1.2 $
 */
public final class XmlMapperHelper {
    private static ApplicationIP appIp;
    private static final Logger APP = Logger.getLogger(XmlMapperHelper.class);
    private static File rootFolder = null;
    private static String appIpRessourceName = null;

    private XmlMapperHelper() {}

    public static synchronized void initToLoadFromRessource(String ressourceName) {
        XmlMapperHelper.rootFolder = null;
        XmlMapperHelper.appIpRessourceName = ressourceName;
        XmlMapperHelper.appIp = null;
    }


    public static synchronized void initToLoadFromFile(File root, File relativeAppIpFile) {
        File appIPFile = new File(root, relativeAppIpFile.toString());

        if (!appIPFile.exists() || !appIPFile.canRead()) {
            throw new IllegalArgumentException("Fichier introuvable (ou illisible) : "
                + appIPFile + " depuis " + new File(".").getAbsolutePath());
        }
        XmlMapperHelper.rootFolder = root;
        XmlMapperHelper.appIpRessourceName = relativeAppIpFile.toString();
        XmlMapperHelper.appIp = null;
    }


    /**
     * Retourne l'objet ApplicationIP.
     *
     * @return l'application IP
     *
     * @throws IOException impossible de trouver ou de lire les plans d'intégrations.
     * @throws XmlException Erreur de lecture Xml.
     */
    public static synchronized ApplicationIP getApplicationIP()
            throws IOException, XmlException {
        if (appIp == null) {
            checkHelperHasBeenIntialized();
            appIp =
                (ApplicationIP)loadObject(appIpRessourceName, ApplicationIP.class,
                    "ApplicationIPRules.xml");
            appIp.loadAllPlans();
        }
        return appIp;
    }


    public static IntegrationPlan loadPlan(String planFileName, Class clazz)
            throws IOException, XmlException {
        APP.info("(loadPlan) Chargement du fichier " + planFileName);

        //URL xmlFileAsUrl = buildURL(planFileName, IntegrationPlan.class);
        //System.out.println("** Host :  "+xmlFileAsUrl.getHost());
        //System.out.println("** url  :  "+xmlFileAsUrl.getPath());
        //System.out.println("** file  :  "+xmlFileAsUrl.getFile());
        InputStream xmlFileAsStream = buildStream(planFileName, clazz);
        URL xmlRulesAsUrl = XmlMapperHelper.class.getResource("IntegrationPlanRules.xml");

        EasyXMLMapper easyXMLMapper = new EasyXMLMapper(xmlFileAsStream, xmlRulesAsUrl);

        if (rootFolder != null) {
            easyXMLMapper.setEntityResolver(new FileResolver(rootFolder));
        }
        else {
            easyXMLMapper.setEntityResolver(new UriResolver());
        }

        return (IntegrationPlan)easyXMLMapper.load();
    }

    public static IntegrationPlan loadPlan(String planFileName)
            throws IOException, XmlException {
        return loadPlan(planFileName, IntegrationPlan.class);
    }


    static Object loadObject(String xmlFile, Class objClass, String rulesFile)
            throws IOException, XmlException {
        URL xmlRulesAsUrl = XmlMapperHelper.class.getResource(rulesFile);
        InputStream xmlFileAsStream = buildStream(xmlFile, objClass);

        EasyXMLMapper easyXMLMapper = new EasyXMLMapper(xmlFileAsStream, xmlRulesAsUrl);

        if (rootFolder != null) {
            easyXMLMapper.setEntityResolver(new FileResolver(rootFolder));
        }
        else {
            easyXMLMapper.setEntityResolver(new UriResolver());
        }

        return easyXMLMapper.load();
    }


    private static InputStream buildStream(String xmlFile, Class objClass)
            throws FileNotFoundException {
        InputStream xmlFileAsStream;
        if (rootFolder != null) {
            xmlFileAsStream = new FileInputStream(new File(rootFolder, xmlFile));
        }
        else {
            xmlFileAsStream = objClass.getResourceAsStream(xmlFile);
        }

        if (xmlFileAsStream == null) {
            APP.error("Le fichier XML " + xmlFile + " est introuvable !");
            throw new IllegalArgumentException("Le fichier XML " + xmlFile
                + " est introuvable !");
        }

        return xmlFileAsStream;
    }


    private static void checkHelperHasBeenIntialized() {
        if (appIpRessourceName == null) {
            throw new IllegalArgumentException(
                "Il faut appeler une méthode init avant d'appeler cette methode !");
        }
    }

    /**
     * Entity Resolver a partir d'un fichier.
     */
    private static final class FileResolver implements EntityResolver {
        private File root;

        FileResolver(File root) {
            this.root = root;
        }

        public InputSource resolveEntity(String publicId, String systemId)
                throws IOException {
            File entityFile =
                new File(root, systemId.substring("file://".length(), systemId.length()));

            APP.info("Chargement de l'entité de systemeId=" + systemId
                + " à partir du fichier " + entityFile + " ");

            if (!entityFile.exists()) {
                throw new IllegalArgumentException("Fichier introuvable : " + entityFile);
            }

            return new InputSource(new FileInputStream(entityFile));
        }
    }


    /**
     * Resolver utilisée pour les imports de fichier mapping a partir du jar.
     */
    private static class UriResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            if (systemId.startsWith("file://")) {
                systemId = systemId.substring("file://".length(), systemId.length());
            }

            InputStream systemRessource =
                XmlMapperHelper.class.getResourceAsStream(systemId);

            if (systemRessource == null) {
                String errorMsg =
                    "Impossible de charger la ressource systeme=" + systemId
                    + " (publicId=" + publicId + ") - Ressource introuvable";
                APP.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            return new InputSource(systemRessource);
        }
    }
}
