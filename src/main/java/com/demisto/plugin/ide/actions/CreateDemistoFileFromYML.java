package com.demisto.plugin.ide.actions;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Map;
import java.util.Objects;


import static com.demisto.plugin.ide.DemistoUtils.*;

public class CreateDemistoFileFromYML extends AnAction {
    public static final Logger LOG = Logger.getInstance(CreateDemistoFileFromYML.class);
    private Project project;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // getting the current file that we are working on
        this.project = e.getProject();
        assert project != null;
        ApplicationManager.getApplication().invokeLater(() -> prepareFileAndOpen(project));
    }

    private void prepareFileAndOpen(@Nullable Project project) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            Messages.showInfoMessage(this.project, "You can only create a Demisto Python file from the YML file of a Demisto Python Automation or Integration. " +
                    "Open a Demisto YML file and then click the 'Create Demisto Python' button.", IdeBundle.message("title.cannot.open.project"));
        } else {
            Document currentDoc = editor.getDocument();
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
            assert currentFile != null;
            VirtualFile scriptFile;
            VirtualFile imageFile;
            VirtualFile detailedDescriptionFile;
            String newFilePath = "";
            String prefix = "";
            String newYmlString = "";
            String newDirPath;
            VirtualFile dirPath = currentFile.getParent();
            assert dirPath != null;

            LOG.info("In CreateDemistoFileFromYML, preparing to open file " + currentFile.getPath());
            if (Objects.equals(currentFile.getExtension(), "yml")) {

                // create demisto yml object
                Yaml yaml = new Yaml();
                String ymlString = DemistoUtils.readFile(currentFile.getPath());
                Map<String, Object> ymlMap = yaml.load(ymlString);
                String ymlType = ymlIsIntegrationOrAutomation(ymlMap);
                String script;
                String image = "";
                String detailedDescription = "";


                if (ymlType.equals("py-automation")) {
                    prefix = SCRIPT_PREFIX;
                } else if (ymlType.equals("py-integration")) {
                    prefix = INTEGRATION_PREFIX;
                } else {
                    Messages.showInfoMessage(this.project, "You can only open Demisto Python Automation and Integrations", IdeBundle.message("title.cannot.open.project"));
                    return;
                }


                String dirName = DemistoUtils.removePrefix(DemistoUtils.getFileNameWithoutExtension(currentFile.getName()), prefix);
                newDirPath = dirPath.getPath() + "/" + dirName;
                boolean dirCreated = DemistoUtils.createDirectory(newDirPath);

                if (!dirCreated) {
                    Messages.showInfoMessage(this.project, "Could not create directory for the project", IdeBundle.message("title.cannot.create.file"));
                    return;
                }

                // get script from current yml
                if (ymlType.equals("py-automation")) {
                    LOG.info("Created directory, updating automation files.");
                    DemistoAutomationYML automationYML = new DemistoAutomationYML(ymlMap);
                    script = ensureTextEndsInNewLine(getDemistoScriptWithImports(automationYML.getScript()));

                    automationYML.setScript(SCRIPT_DEFAULT);

                    newYmlString = DemistoUtils.getYMLStringFromMap(automationYML.getDemistoYMLMapWithUnsupportedFields(automationYML));

                } else {
                    LOG.info("Created directory, updating integration files.");
                    DemistoIntegrationYML integrationYML = new DemistoIntegrationYML(ymlMap);
                    Map integrationMap = integrationYML.getDemistoYMLMapWithUnsupportedFields(integrationYML);
                    image = (String) integrationMap.get("image");
                    detailedDescription = integrationYML.getDetaileddescription();
                    script = ensureTextEndsInNewLine(getDemistoScriptWithImports(integrationYML.getScript().getScript()));


                    integrationYML.setDetaileddescription(DETAILED_DESCRIPTION_DEFAULT);
                    integrationYML.getScript().setScript(SCRIPT_DEFAULT);
                    Map unsupportedFields = integrationYML.getUnsupportedFields();
                    if (unsupportedFields.containsKey("image")) {
                        unsupportedFields.remove("image");
                    }
                    integrationYML.setUnsupportedFields(unsupportedFields);

                    newYmlString = DemistoUtils.getYMLStringFromMap(integrationYML.getDemistoYMLMapWithUnsupportedFields(integrationYML));
                }

                newFilePath = newDirPath + "/" + currentFile.getName();
                newFilePath = DemistoUtils.removePrefix(newFilePath, prefix);

                // Split and open image and description files
                if (ymlType.equals("py-integration")) {
                    LOG.info("Opening image and description files.");
                    // Split and open image and description files

                    createImageFile(newFilePath, image);

                    // Write the detailed description to a file
                    if (!DemistoUtils.stringIsNotEmptyOrNull(detailedDescription)) {
                        detailedDescription = " ";
                    }

                    String detailedDescriptionFilePath = DemistoUtils.renameFileExtension(
                            DemistoUtils.addFilePostfix(newFilePath, DESCRIPTION_POSTFIX), "md");
                    DemistoUtils.writeStringToFile(detailedDescriptionFilePath, detailedDescription);

                }

                LOG.info("Creating python file.");
                // write Python script to file
                String scriptFilePath = DemistoUtils.renameFileExtension(newFilePath, "py");
                DemistoUtils.writeStringToFile(scriptFilePath, script);
                // open it as a virtual file
                scriptFile = VfsUtil.findFileByIoFile(new File(scriptFilePath), true);
                if (scriptFile != null) {
                    // If successful, open the script file and delete the old yaml
                    DemistoUtils.openFile(scriptFile, project);
                    DemistoUtils.writeStringToFile(DemistoUtils.removePrefix(newFilePath, prefix), newYmlString);
                    DemistoUtils.deleteFile(currentFile.getPath());
                }
                LOG.info("Successfully created Demisto file.");
            } else {
                Messages.showInfoMessage(this.project, "You can only create a Demisto Python file from the YML file of a Demisto Python Automation or Integration. " +
                        "Open a Demisto YML file and then click the 'Create Demisto Python' button.", IdeBundle.message("title.cannot.open.project"));
            }
        }
    }

}

