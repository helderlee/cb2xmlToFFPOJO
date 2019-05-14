package dev.helderlee.cb2xmltoffpojo.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import static com.google.common.base.CaseFormat.*;
import java.io.FileWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.text.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class Cb2XmlToJava {

    private static int position;

    private final File xmlFile;
    private final File targetDirectory;

    private String packageName;
    private FileWriter copybookClassFileWriter;
    private FileWriter plainClassFileWriter;
    private FileWriter decoratorClassFileWriter;

    public Cb2XmlToJava(File xmlFile, File targetDirectory) {
        this.xmlFile = xmlFile;
        this.targetDirectory = targetDirectory;
    }

    public void generate() throws ParserConfigurationException, SAXException, IOException {
        Files.createDirectories(Paths.get(targetDirectory.getAbsolutePath()));

        final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        final Document doc = docBuilder.parse(this.xmlFile);
        final List<String> list = new ArrayList<>();
        parse(doc, list, doc.getDocumentElement());
        if (decoratorClassFileWriter != null) {
            decoratorClassFileWriter.close();
            decoratorClassFileWriter = null;
        }
        if (plainClassFileWriter != null) {
            plainClassFileWriter.write("}\n");
            plainClassFileWriter.close();
            plainClassFileWriter = null;
        }
        if (copybookClassFileWriter != null) {
            copybookClassFileWriter.write("}\n");
            copybookClassFileWriter.close();
            copybookClassFileWriter = null;
        }
    }

    private void parse(final Document doc, final List<String> list, final Element e) throws IOException {
        Node parent = e.getParentNode();
        Element parentElement = null;
        String picture = e.getAttribute("picture");
        String occurs = e.getAttribute("occurs");

        if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
            parentElement = (Element) parent;
        }

        String parentOccurs = "";
        if (parentElement != null && !StringUtils.isBlank(parentElement.getAttribute("occurs"))) {
            parentOccurs = parentElement.getAttribute("occurs");
        }

        if (!StringUtils.isBlank(e.getAttribute("filename"))) {
            String filename = e.getAttribute("filename");
            packageName = filename.toLowerCase().split("\\.")[0];
            this.writeCopybookClassFile();
        }
        if (!StringUtils.isBlank(occurs)) {
            position = 1;

            copybookClassFileWriter.write(this.renderCollectionField(parentElement, e));

            this.writePlainClassFile(e);
        }

        if (!StringUtils.isBlank(picture)) {
            if (!StringUtils.isBlank(parentOccurs)
                    && plainClassFileWriter != null) {
                plainClassFileWriter.write(this.renderPlainField(parentElement, e));
            } else {
                copybookClassFileWriter.write(this.renderPlainField(parentElement, e));
            }
        }

        final NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                list.add(n.getNodeName());
                parse(doc, list, (Element) n);
            }
        }
        if (!StringUtils.isBlank(occurs)) {
            this.writeDecoratorClassFile(e);
        }
    }

    private void writeCopybookClassFile() throws IOException {
        String className = WordUtils.capitalize(packageName);
        String classFilename = className.concat(".java");

        Files.createDirectories(Paths.get(
                targetDirectory.getAbsolutePath(),
                packageName));

        copybookClassFileWriter = new FileWriter(Paths.get(
                targetDirectory.getAbsolutePath(),
                packageName,
                classFilename).toString());

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n");
        sb.append("\n");
        sb.append("import ").append(packageName).append(".decorator.*;\n");
        sb.append("import com.github.ffpojo.metadata.positional.*;\n");
        sb.append("import com.github.ffpojo.metadata.positional.annotation.*;\n");
        sb.append("import lombok.Data;\n");
        sb.append("import java.io.*;\n");
        sb.append("import java.util.*;\n");
        sb.append("\n");
        sb.append("@PositionalRecord\n");
        sb.append("@Data\n");
        sb.append("public class ").append(className).append(" implements Serializable {\n\n");

        copybookClassFileWriter.write(sb.toString());

        writeListDecorator();
    }

    private void writeListDecorator() throws IOException {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("resource.loader", "class");
        velocityEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();

        Template t = velocityEngine.getTemplate("ListDecorator.vm");

        VelocityContext context = new VelocityContext();
        context.put("packageName", packageName.concat(".decorator"));

        StringWriter writer = new StringWriter();
        t.merge(context, writer);

        try (FileWriter listDecoratorWriter = new FileWriter(Paths.get(
                targetDirectory.getAbsolutePath(),
                packageName, "decorator",
                "ListDecorator.java").toString())) {
            listDecoratorWriter.write(writer.toString());
        }
    }

    private void writePlainClassFile(final Element e) throws IOException {
        String className = WordUtils.capitalize(LOWER_HYPHEN.to(UPPER_CAMEL, e.getAttribute("name").toLowerCase()));
        String classFilename = className.concat(".java");

        if (plainClassFileWriter != null) {
            plainClassFileWriter.write("}\n");
            plainClassFileWriter.close();
            plainClassFileWriter = null;
        }

        plainClassFileWriter = new FileWriter(Paths.get(
                targetDirectory.getAbsolutePath(),
                packageName,
                classFilename).toString());

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n");
        sb.append("\n");
        sb.append("import ").append(packageName).append(".decorator.*;\n");
        sb.append("import com.github.ffpojo.metadata.positional.*;\n");
        sb.append("import com.github.ffpojo.metadata.positional.annotation.*;\n");
        sb.append("import lombok.Data;\n");
        sb.append("import java.io.*;\n");
        sb.append("import java.util.*;\n");
        sb.append("\n");
        sb.append("@PositionalRecord\n");
        sb.append("@Data\n");
        sb.append("public class ").append(className).append(" implements Serializable {\n\n");

        plainClassFileWriter.write(sb.toString());
    }

    private void writeDecoratorClassFile(final Element e) throws IOException {
        String className = WordUtils.capitalize(LOWER_HYPHEN.to(UPPER_CAMEL, e.getAttribute("name").toLowerCase()));
        String classFilename = className.concat("CollectionDecorator.java");

        Files.createDirectories(Paths.get(
                targetDirectory.getAbsolutePath(),
                packageName, "decorator"));

        if (decoratorClassFileWriter != null) {
            decoratorClassFileWriter.close();
            decoratorClassFileWriter = null;
        }

        decoratorClassFileWriter = new FileWriter(Paths.get(
                targetDirectory.getAbsolutePath(),
                packageName, "decorator",
                classFilename).toString());

        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(packageName).append(".decorator;\n\n");
        sb.append("import ").append(packageName).append(".*;\n\n");

        sb.append("public class ");
        sb.append(className);
        sb.append("CollectionDecorator extends ListDecorator<");
        sb.append(className);
        sb.append("> {\n\n");

        sb.append("   public ");
        sb.append(className);
        sb.append("CollectionDecorator() { this(");
        sb.append(className);
        sb.append(".class, ");
        sb.append(e.getAttribute("occurs"));
        sb.append("); }\n");

        sb.append("   public ");
        sb.append(className);
        sb.append("CollectionDecorator(Class clazz, Integer occurrences) { super(clazz, occurrences); }\n\n");

        sb.append("}\n");

        decoratorClassFileWriter.write(sb.toString());
    }

    private String renderPlainField(Element parentElement, Element e) {
        StringBuilder sb = new StringBuilder();

        String parentOccurs = "";
        if (parentElement != null && !StringUtils.isBlank(parentElement.getAttribute("occurs"))) {
            parentOccurs = parentElement.getAttribute("occurs");
        }
        String picture = e.getAttribute("picture");

        sb.append("   ");

        sb.append("@PositionalField(initialPosition = ");
        if (!StringUtils.isBlank(parentOccurs)) {
            sb.append(position);
        } else {
            sb.append(e.getAttribute("position"));
        }
        sb.append(", finalPosition = ");
        if (!StringUtils.isBlank(parentOccurs)) {
            sb.append(position + Integer.parseInt(e.getAttribute("storage-length")) - 1);
            position += Integer.parseInt(e.getAttribute("storage-length"));
        } else {
            sb.append(Integer.parseInt(e.getAttribute("position"))
                    + Integer.parseInt(e.getAttribute("storage-length")) - 1);
        }
        if (picture.contains("V")) {
            sb.append(", paddingCharacter = '0', paddingAlign = PaddingAlign.LEFT, decorator = SignedDoubleDecorator.class");
        } else if (picture.contains("9")) {
            sb.append(", paddingCharacter = '0', paddingAlign = PaddingAlign.LEFT, decorator = SignedLongDecorator.class");
        }
        sb.append(")\n");

        sb.append("   ");

        sb.append("private ");
        if (picture.contains("X")) {
            sb.append("String ");
        } else if (picture.contains("V")) {
            sb.append("Double ");
        } else if (picture.contains("9")) {
            sb.append("Long ");
        }
        sb.append(LOWER_HYPHEN.to(LOWER_CAMEL, e.getAttribute("name")
                .toLowerCase()).concat(";\n\n"));

        return sb.toString();
    }

    private String renderCollectionField(Element parentElement, Element e) {
        StringBuilder sb = new StringBuilder();

        String parentOccurs = "";
        if (parentElement != null && !StringUtils.isBlank(parentElement.getAttribute("occurs"))) {
            parentOccurs = parentElement.getAttribute("occurs");
        }
        String occurs = e.getAttribute("occurs");

        sb.append("   ");
        sb.append("@PositionalField(initialPosition = ");
        if (!StringUtils.isBlank(parentOccurs)) {
            sb.append(position);
        } else {
            sb.append(e.getAttribute("position"));
        }
        sb.append(", finalPosition = ");
        if (!StringUtils.isBlank(parentOccurs)) {
            sb.append(position + Integer.parseInt(e.getAttribute("storage-length")) - 1);
            position += Integer.parseInt(e.getAttribute("storage-length"));
        } else {
            if (!StringUtils.isBlank(occurs)) {
                sb.append(Integer.parseInt(e.getAttribute("position"))
                        + (Integer.parseInt(e.getAttribute("storage-length")) * Integer.parseInt(e.getAttribute("occurs")))
                        - 1);
                sb.append(", decorator = ");
                sb.append(LOWER_HYPHEN.to(UPPER_CAMEL, e.getAttribute("name").toLowerCase()));
                sb.append("CollectionDecorator.class");
            }
        }
        sb.append(")\n");
        sb.append("   ");
        sb.append("private ");
        if (!StringUtils.isBlank(occurs)) {
            sb.append("List<");
            sb.append(LOWER_HYPHEN.to(UPPER_CAMEL, e.getAttribute("name")
                    .toLowerCase()));
            sb.append("> ");
        }
        sb.append(LOWER_HYPHEN.to(LOWER_CAMEL, e.getAttribute("name")
                .toLowerCase()).concat("List;").concat("\n"));
        sb.append("\n");

        return sb.toString();
    }

}
