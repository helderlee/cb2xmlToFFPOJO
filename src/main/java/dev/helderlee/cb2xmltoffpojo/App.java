package dev.helderlee.cb2xmltoffpojo;

import dev.helderlee.cb2xmltoffpojo.generator.Cb2XmlToJava;
import dev.helderlee.cb2xmltoffpojo.util.IoUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Arg 1: source path to xml files");
            System.out.println("Arg 2: target path for generated files");
            System.exit(1);
        }
        String sourceArg = args[0];
        String targetArg = args[1];
        IoUtil.checkDirectory(sourceArg);
        IoUtil.checkDirectory(targetArg);
        File sourceDirectory = IoUtil.getDirectory(sourceArg);
        File targetDirectory = IoUtil.getDirectory(targetArg);
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
