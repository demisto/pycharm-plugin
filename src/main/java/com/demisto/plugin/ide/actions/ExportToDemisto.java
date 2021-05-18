package com.demisto.plugin.ide.actions;

import com.demisto.plugin.ide.DemistoRESTClient;
import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FilenameUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Objects;

import static com.demisto.plugin.ide.DemistoUtils.*;

public class ExportToDemisto extends AnAction {
    public static String AUTOMATION_UPLOAD_PATH = "/automation/import";
    public static String INTEGRATION_UPLOAD_PATH = "/settings/integration-conf/upload";
    public static final Logger LOG = Logger.getInstance(ExportToDemisto.class);
    public static final String DEMISTO_EXPORT_TITLE = "Demisto Export";

    @Override
    public void actionPerformed(AnActionEvent e) {
        // getting the current file that we are working on
        Project project = e.getProject();
        assert project != null;
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            Messages.showInfoMessage(project, "Please open a YML or Python of Demisto Python Automation or Integration," +
                    " and then click on the 'Export to Demisto' button", DEMISTO_EXPORT_TITLE);
            return;
        }
        Document currentDoc = Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getDocument();
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        assert currentFile != null;
        Boolean res = updateCurrentYmlScript(project);
        if (res){
            Messages.showMessageDialog(project, "File was exported successfully!", DEMISTO_EXPORT_TITLE, IconLoader.getIcon(DEMISTO_EXPORT_ICON_PATH));
        }
    }

    public static Boolean updateCurrentYmlScript(Project project){
        Document currentDoc = Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getDocument();
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        assert currentFile != null;
        String fileExtension = FilenameUtils.getExtension(currentFile.getPath());
        String ymlFilePath;
        String script;
        if (fileExtension.equals("yml")) {
            ymlFilePath = currentFile.getPath();
            String pythonFilePath = DemistoUtils.renameFileExtension(ymlFilePath, "py");
            script = ensureTextEndsInNewLine(DemistoUtils.removeImportsFromPythonFile(DemistoUtils.readFile(pythonFilePath)));
        }
        else if (fileExtension.equals("py")) {
            // removing the imports of demistomock, demistocommonserverpython, demistocommonuserpython from the Python script
            script = ensureTextEndsInNewLine(DemistoUtils.removeImportsFromPythonFile(currentDoc.getText()));
            ymlFilePath = DemistoUtils.renameFileExtension(currentFile.getPath(), "yml");
        }
        else {
            Messages.showInfoMessage(project, "Please open a YML or Python of Demisto Python Automation or Integration," +
                    " and then click on the 'Export to Demisto' button", DEMISTO_EXPORT_TITLE);
            return false;
        }

        return updateCurrentYmlInternal(project, ymlFilePath, script);
    }

    private static Boolean updateCurrentYmlInternal(Project project, String ymlFilePath, String script) {
        // adding the updated script to the yml
        LOG.info("Exporting file " + ymlFilePath);
        String ymlString = DemistoUtils.readFile(ymlFilePath);
        if (!DemistoUtils.stringIsNotEmptyOrNull(ymlString)) {
            Messages.showMessageDialog(project, "Got an empty file for path: " + ymlFilePath + "Please check that this Python file has a Demisto YML configuration",
                    DEMISTO_EXPORT_TITLE, IconLoader.getIcon(DEMISTO_EXPORT_ICON_PATH));
            return false;
        }
        Yaml yaml = new Yaml();
        Map ymlMap = yaml.load(ymlString);
        String ymlType = ymlIsIntegrationOrAutomation(ymlMap);
        Map demistoMap;
        String uploadPath = "";
        if (ymlType.equals("py-automation")){
            LOG.info("Updating automation for export.");
            DemistoAutomationYML automationYML = new DemistoAutomationYML(ymlMap);
            automationYML.setScript(script);
            automationYML.validateArguments();
            automationYML.validateOutputs();
            demistoMap = automationYML.getDemistoYMLMapWithUnsupportedFields(automationYML);
            uploadPath = AUTOMATION_UPLOAD_PATH;
        } else if (ymlType.equals("py-integration")){
            LOG.info("Updating integration for export.");
            // Insert image and description
            String detailedDescriptionFilePath = DemistoUtils.renameFileExtension(
                    DemistoUtils.addFilePostfix(ymlFilePath, DESCRIPTION_POSTFIX), "md");
            String imageFilePath = DemistoUtils.renameFileExtension(
                    DemistoUtils.addFilePostfix(ymlFilePath, IMAGE_POSTFIX), "png");

            String imageBase64 = DemistoUtils.readFile(imageFilePath, true);
            String imagePrefix;
            if (DemistoUtils.stringIsNotEmptyOrNull(imageBase64)) {
                imagePrefix = BASE64_PNG_PREFIX;
            }
            else {
                imageFilePath = DemistoUtils.renameFileExtension(
                        DemistoUtils.addFilePostfix(ymlFilePath, IMAGE_POSTFIX), "jpg");
                imageBase64 = DemistoUtils.readFile(imageFilePath, true);
                imagePrefix = BASE64_JPEG_PREFIX;
            }

            if (DemistoUtils.stringIsNotEmptyOrNull(imageBase64)) {
                imageBase64 = imagePrefix + imageBase64;
                if (ymlMap.containsKey("image")) {
                    ymlMap.replace("image", imageBase64);
                } else {
                    ymlMap.put("image", imageBase64);
                }
            }
            DemistoIntegrationYML integrationYML = new DemistoIntegrationYML(ymlMap);


            String description =  DemistoUtils.readFile(detailedDescriptionFilePath);
            if(DemistoUtils.stringIsNotEmptyOrNull(description)) {
                integrationYML.setDetaileddescription(description);
            }

            // validating that we don't have empty params or commands

            integrationYML.validateParams();
            integrationYML.validateYMLCommands();
            integrationYML.getScript().setScript(script);

            demistoMap = integrationYML.getDemistoYMLMapWithUnsupportedFields(integrationYML);
            uploadPath = INTEGRATION_UPLOAD_PATH;
        } else {
            Messages.showMessageDialog(project, "File is not a Demisto Automation or Integration",
                    DEMISTO_EXPORT_TITLE, IconLoader.getIcon(DEMISTO_EXPORT_ICON_PATH));
            return false;
        }
        // get string representation of the updated yml
        String demistoStringYML = DemistoUtils.getYMLStringFromMap(demistoMap);
        // write yml to unified file
        String unifiedFilePath = DemistoUtils.addFilePostfix(ymlFilePath, UNIFIED_POSTFIX);
        DemistoUtils.writeStringToFile(unifiedFilePath, demistoStringYML);

        LOG.info("Exporting file " + ymlFilePath + " to Demisto");
        DemistoRESTClient demistoRESTClient = new DemistoRESTClient(project);
        String res = demistoRESTClient.sendPostRequest(null, uploadPath, unifiedFilePath);
        LOG.info("Export response from Demisto is: " + res);


        return stringIsNotEmptyOrNull(res);

    }
}
