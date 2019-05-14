package dev.helderlee.cb2xmltoffpojo;

import dev.helderlee.cb2xmltoffpojo.generator.Cb2XmlToJava;
import dev.helderlee.cb2xmltoffpojo.util.IoUtil;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.xml.sax.SAXException;

public class App {

    public static void main(String[] args) {
        IoUtil.checkDirectory("source");
        IoUtil.checkDirectory("target");
        File sourceDirectory = IoUtil.getDirectory("source");
        File targetDirectory = IoUtil.getDirectory("target");
        File[] xmlFiles = sourceDirectory.listFiles();
        for (File xmlFile : xmlFiles) {
            Cb2XmlToJava cb2XmlToJava = new Cb2XmlToJava((xmlFile), targetDirectory);
            try {
                cb2XmlToJava.generate();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
