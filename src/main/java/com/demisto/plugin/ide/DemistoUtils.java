package com.demisto.plugin.ide;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


public class DemistoUtils {
    final public static String DEMISTO_EXPORT_ICON_PATH = "/icons/export-to-demisto.png";

    public static final Logger LOG = Logger.getInstance(DemistoUtils.class);
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final int TEXT_DEBOUNCE = 700;
    public static final String DESCRIPTION_POSTFIX = "_description";
    public static final String IMAGE_POSTFIX = "_image";
    public static final String SCRIPT_PREFIX = "script-";
    public static final String INTEGRATION_PREFIX = "integration-";
    public static final String UNIFIED_POSTFIX = "_unified";
    public static final String BASE64_PNG_PREFIX = "data:image/png;base64,";
    public static final String BASE64_JPEG_PREFIX = "data:image/jpeg;base64,";
    public static final String DETAILED_DESCRIPTION_DEFAULT = "";
    public static final String SCRIPT_DEFAULT = "-";
    public static final String DOT = ".";
    public static final String DEMISTO_PLACEHOLDER_IMAGE = "/icons/placeholder.png";


    public static void writeImageToFile(String path, @NotNull String image) {
        try {
            byte[] decodedImg = Base64.getDecoder().decode(image.getBytes(StandardCharsets.UTF_8));
            Path destinationFile = Paths.get(path);
            Files.write(destinationFile, decodedImg);
        } catch (IOException e) {
            LOG.error("Error in writeImageToFile for path " + path + " Error was: " + e.getMessage());
        }
    }

    public static void createImageFile(String newFilePath, String image) {
        String imageFilePath = "";
        String extension = "";
        boolean createNewImage = false;
        if (stringIsNotEmptyOrNull(image)) {
            // Write the image to a file
            if (image.contains("png")) {
                image = image.replaceFirst(BASE64_PNG_PREFIX, "");
                extension = "png";
            } else if (image.contains("jpeg")) {
                image = image.replaceFirst(BASE64_JPEG_PREFIX, "");
                extension = "jpg";
            } else {
                LOG.info("Found an unknown image in the file for the path: " + newFilePath + ", using the default image.");
                createNewImage = true;
            }
        } else {
            createNewImage = true;
        }

        try {
            if (createNewImage) {
                LOG.info("Found no image in the file for the path: " + newFilePath + ", using the default image.");
                imageFilePath = DemistoUtils.renameFileExtension(
                        DemistoUtils.addFilePostfix(newFilePath, IMAGE_POSTFIX), "png");
                copyResource(DEMISTO_PLACEHOLDER_IMAGE, imageFilePath, DemistoUtils.class);
            } else {
                imageFilePath = DemistoUtils.renameFileExtension(
                        DemistoUtils.addFilePostfix(newFilePath, IMAGE_POSTFIX), extension);
                DemistoUtils.writeImageToFile(imageFilePath, image);
            }
        } catch (IOException e) {
            LOG.error("Could not create the default image in path" + imageFilePath + ", Error was: " + e.getMessage());
        }
    }

    public static Boolean writeStringToFile(String path, String stringToWrite) {
        // building a new yaml or overwriting the existing one
        File file = new File(path);
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(stringToWrite);
            writer.close();
        } catch (IOException e) {
            LOG.error("Error in writeStringToFile for path " + path + " Error was: " + e.getMessage());
            return false;
        }

        return true;
    }

    @NotNull
    public static Boolean createDirectory(String dirPath) {
        File directory = new File(dirPath);
        boolean created = false;
        if (!directory.exists()) {
            created = directory.mkdir();
        }

        if (!created) {
            LOG.warn("Failed to create directory: " + dirPath);
        }

        return created;
    }

    public static Yaml getDefaultYML() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Representer representer = new Representer() {
            @Override
            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
                // if value of property is null, ignore it.
                if (propertyValue == null || propertyValue == "" || (propertyValue instanceof ArrayList && ((ArrayList) propertyValue).size() == 0)) {
                    return null;
                } else {
                    return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                }
            }
        };
        representer.getPropertyUtils().setSkipMissingProperties(true);
        return new Yaml(representer, options);
    }

    public static Map writeDemistoObjectToMap(DemistoYML ymlObject) {
        Yaml yaml = getDefaultYML();
        String res = yaml.dumpAs(ymlObject, Tag.MAP, null);
        return yaml.load(res);
    }

    public static String renameFileExtension(String source, String newExtension) {
        String baseName = FilenameUtils.removeExtension(source);
        return baseName + "." + newExtension;
    }

    public static String addFilePostfix(String source, String postfix) {
        String extension = FilenameUtils.getExtension(source);
        String baseName = FilenameUtils.removeExtension(source);
        String newName = baseName + postfix;

        if (extension != null && !extension.isEmpty()) {
            newName += "." + extension;
        }

        return newName;
    }

    public static String removePrefix(String source, String prefix) {
        if (!prefix.isEmpty() &&
                !(prefix.equals(source)) &&
                !(prefix.equals(FilenameUtils.removeExtension(source)))) {
            return source.replaceAll(prefix, "");
        }

        return source;
    }

    public static String getFileNameWithoutExtension(String source) {
        return FilenameUtils.removeExtension(source);
    }

    @NotNull
    public static Boolean stringIsNotEmptyOrNull(String str) {
        return str != null && !str.isEmpty() && !str.equals("null");
    }

    public static Boolean booleanIsNotEmptyOrNull(Boolean bool) {
        return bool != null;
    }

    public static void deleteFile(String path) {
        File file = new File(path);

        boolean deleted = file.delete();

        if (!deleted) {
            LOG.error("Could not delete file in path " + path);
        }
    }

    @NotNull
    public static String readFile(String path) {
        return readFile(path, false);
    }

    @NotNull
    public static String readFile(String path, Boolean encode) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            LOG.info("Error in readFile for path: " + path + " Error was: " + e.getMessage());
            return "";
        }

        if (encode) {
            encoded = Base64.getEncoder().encode(encoded);
        }

        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void openFile(VirtualFile file, @NotNull Project project) {
        // open a virtual file in the editor
        ArrayList<VirtualFile> file_list = new ArrayList<>();
        file_list.add(file);
        NonProjectFileWritingAccessProvider.allowWriting(file_list);
        PsiNavigationSupport.getInstance().createNavigatable(project, file, -1).navigate(true);
    }

    public static void copyResource(String origin, String dest, Class c) throws IOException {
        InputStream src = c.getResourceAsStream(origin);
        Files.copy(src, Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
    }

    public static String getYMLStringFromMap(Map demistoMap) {
        Yaml yaml = getDefaultYML();
        // write yml to string
        return yaml.dumpAs(demistoMap, Tag.MAP, null);
    }

    public static JLabel underlineJLabel(String JLabelText) {
        JLabel label = new JLabel(JLabelText);
        Font font = label.getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        label.setFont(font.deriveFont(attributes));
        return label;
    }

    public static String markdownToHtml(String markdown) {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse(markdown);
        String htmlFromMarkdown = renderer.render(document);
        return "<html>" + htmlFromMarkdown + "</html>";
    }

    public static String removeImportsFromPythonFile(String script) {
        script = script.replaceAll("import demistomock as demisto", "");
        script = script.replaceAll("from CommonServerPython import \\*", "");
        script = script.replaceAll("from CommonServerUserPython import \\*", "");
        script = script.replaceAll("(from __future__ import .*)", "");
        return script;
    }

    public static String getDemistoScriptWithImports(String script) {
        if (!script.contains("import demistomock as demisto") ||
                !script.contains("from CommonServerPython import *") ||
                !script.contains("from CommonServerUserPython import *")) {
            script = DemistoUtils.removeImportsFromPythonFile(script);
            script = String.format("import demistomock as demisto\n" +
                    "from CommonServerPython import *\n" +
                    "from CommonServerUserPython import *\n%s", script);
        }
        return script;
    }

    public static String getDemistoScriptWithDemistoMockImport(String script) {
        if (!script.contains("import demistomock as demisto")) {
            script = String.format("import demistomock as demisto\n%s", script);
        }
        return script;
    }

    public static void runActionInEDT(Runnable action) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            application.runWriteAction(action);
        } else {
            application.invokeLater(() -> application.runWriteAction(action));
        }
    }

    public static String validateStringParameter(String param) {
        if (stringIsNotEmptyOrNull(param)) {
            return param.trim();
        }
        return "";
    }

    public static Boolean validateBooleanParameter(Boolean param) {
        if (booleanIsNotEmptyOrNull(param)) {
            return param;
        }
        return false;
    }

    public static String ymlIsIntegrationOrAutomation(Map yml) {
        if (stringIsNotEmptyOrNull(String.valueOf(yml.get("type"))) && String.valueOf(yml.get("type")).equals("python")) {
            return "py-automation";
        } else if (stringIsNotEmptyOrNull(String.valueOf(yml.get("configuration")))) { // integration
            LinkedHashMap integrationScriptMap = (LinkedHashMap) yml.get("script");
            if (String.valueOf(integrationScriptMap.get("type")).equals("python")) {
                return "py-integration";
            } else {
                return "js-integration";
            }
        }
        return "";
    }

    public static String addNewLineToString(String text) {
        return text + LINE_SEPARATOR;
    }

    public static String ensureTextEndsInNewLine(String text) {
        return addNewLineToString(text.trim());
    }

    public static String getSingleUserPreference(String key) {
        DemistoUserPreferences preferences = DemistoUserPreferences.getInstance();
        assert preferences != null;
        return validateStringParameter(preferences.getUserPreference(key));
    }

    public static Map<String, String> getUserPreferencesCache() {
        DemistoUserPreferences preferences = DemistoUserPreferences.getInstance();
        return preferences.getUserPreferences();
    }

    public static void setUserPreferencesCache(Map<String, String> cache) {
        DemistoUserPreferences preferences = DemistoUserPreferences.getInstance();
        assert preferences != null;
        preferences.setUserPreferences(cache);
    }

    public static void setSingleUserPreference(String key, String value) {
        DemistoUserPreferences preferences = DemistoUserPreferences.getInstance();
        preferences.setUserPreference(key, value);
    }

    public static String createUserPreferenceKey(String ymlName, String commandName, String argument) {
        StringBuilder key = new StringBuilder();
        key.append(ymlName);
        key.append(DOT);
        if (stringIsNotEmptyOrNull(commandName)) {
            // integrations have commands, automations do not
            key.append(commandName);
        }
        key.append(DOT);
        key.append(argument);
        return key.toString();
    }

    public static String safeGetKeyFromHashmap(Map cache, String key) {
        try {
            Object val = cache.get(key);
            return validateStringParameter(String.valueOf(val));
        } catch (Error e) {
            LOG.error("Unexpected error in safeGetKeyFromHashmap for key " + key + " error was: " + e.getMessage());
            return "";
        }
    }

    public static class DeferredDocumentListener implements DocumentListener {

        private final Timer timer;

        public DeferredDocumentListener(int timeOut, ActionListener listener, boolean repeats) {
            timer = new Timer(timeOut, listener);
            timer.setRepeats(repeats);
        }

        public void start() {
            timer.start();
        }

        public void stop() {
            timer.stop();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            timer.restart();
        }

    }

    public static String getValueOfStringOrNull(@Nullable Object str){
        String newStr = String.valueOf(str);
        if (newStr.equals("null")){
            return null;
        }
        return newStr;
    }
}