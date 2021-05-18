package com.demisto.plugin.ide;

import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.automations.ui.DemistoAutomationPanel;
import com.demisto.plugin.ide.generalUIComponents.MessagePanel;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.demisto.plugin.ide.integrations.ui.DemistoIntegrationPanel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import org.apache.commons.io.FilenameUtils;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.demisto.plugin.ide.DemistoUtils.*;
import static com.demisto.plugin.ide.DemistoYML.getRandomID;

public class DemistoSettingsToolWindowFactory implements ToolWindowFactory {
    private Content content;
    private DemistoAutomationPanel automationPanel;
    public static final Logger LOG = Logger.getInstance(DemistoSettingsToolWindowFactory.class);
    public static final String INTEGRATION_TEMPLATE_PATH = "/META-INF/integration-template.yml";
    private ToolWindow toolWindow;

    public DemistoSettingsToolWindowFactory() {
    }

    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        // initializing the tool window for the first time
        LOG.info("Creating Demisto tool settings window");
        this.toolWindow = toolWindow;
        refreshToolWindow(project, toolWindow);
        MessageBusConnection msgBus = project.getMessageBus().connect(project);
        // monitors selected file changes - when we switch to another file we want to refresh the settings window
        msgBus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                new FileEditorManagerListener() {
                    @Override
                    public void selectionChanged(FileEditorManagerEvent event) {
                        refreshToolWindow(project, toolWindow);

                    }
                });
    }

    // refreshing the tool window content
    private void refreshToolWindow(Project project, ToolWindow toolWindow) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            Document currentDoc = editor.getDocument();
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            // remove old content
            toolWindow.getContentManager().removeAllContents(true);

            // create new content
            assert currentFile != null;
            String fileExtension = FilenameUtils.getExtension(currentFile.getPath());
            if (fileExtension.equals("py")) {
                // relevant Demisto file
                String ymlFilePath = DemistoUtils.renameFileExtension(currentFile.getPath(), "yml");
                String ymlString = DemistoUtils.readFile(ymlFilePath);

                if (DemistoUtils.stringIsNotEmptyOrNull(ymlString)) {
                    Yaml yaml = new Yaml();
                    Map ymlMap = yaml.load(ymlString);
                    String ymlType = ymlIsIntegrationOrAutomation(ymlMap);

                    if (ymlType.equals("py-automation")) {
                        DemistoAutomationYML automationYML = new DemistoAutomationYML(ymlMap);
                        automationPanel = new DemistoAutomationPanel(automationYML, ymlFilePath, project);
                        content = contentFactory.createContent(automationPanel, "", false);
                    } else if (ymlType.equals("py-integration")) {
                        com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML integrationYML = new com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML(ymlMap);
                        DemistoIntegrationPanel integrationPanel = new DemistoIntegrationPanel(integrationYML, ymlFilePath, project);
                        content = contentFactory.createContent(integrationPanel, "", false);
                    } else {
                        MessagePanel messagePanel = new MessagePanel("This plugin can be used only for Demisto Python integrations or Automations");
                        content = contentFactory.createContent(messagePanel, "", false);
                    }
                } else {
                    // python file without matching configuration
                    JPanel fatherPanel = new JPanel();
                    JPanel innerPanel = new JPanel();
                    innerPanel.setLayout(new GridLayoutManager(3, 1, JBUI.emptyInsets(), -1, -1));
                    fatherPanel.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
                    JPanel messagePanel = new MessagePanel("Demisto settings and actions are only available for Python files that have a corresponding Demisto YML file." +
                            " Select the appropriate configuration (Automation/Integration) to auto-create a YML file");
                    innerPanel.add(messagePanel, new GridConstraints(0, 0, 1, 1,
                            GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.FILL_HORIZONTAL,
                            null, null, null));
                    JButton createAutomationButton = new JButton("Create Demisto Automation Configuration");
                    JButton createIntegrationButton = new JButton("Create Demisto Integration Configuration");
                    createAutomationButton.addActionListener(e ->
                    {
                        // create new yml map and set current script
                        DemistoAutomationYML automationYML = new DemistoAutomationYML(new HashMap());
                        String script = currentDoc.getText();
                        automationYML.setScript(script);
                        automationYML.commonfields.setId(getRandomID());
                        script = getDemistoScriptWithImports(script);
                        String finalScript = script;
                        // IntelliJ uses single write lock for files, therefore write actions should be performed only in the EDT
                        Runnable r = () -> currentDoc.setText(finalScript);
                        DemistoUtils.runActionInEDT(r);
                        // get string representation of the updated automation yml
                        Map demistoMap = automationYML.getDemistoYMLMapWithUnsupportedFields(automationYML);
                        String demistoStringYML = DemistoUtils.getYMLStringFromMap(demistoMap);
                        // write yml to string
                        DemistoUtils.writeStringToFile(ymlFilePath, demistoStringYML);
                        // refresh
                        this.refreshToolWindow(project, toolWindow);
                    });
                    createIntegrationButton.addActionListener(e ->
                    {
                        // copy integration template to intended path
                        try {
                            copyResource(INTEGRATION_TEMPLATE_PATH, ymlFilePath, this.getClass());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        // create new yml map and set current script
                        String integrationString = DemistoUtils.readFile(ymlFilePath);
                        Yaml yaml = new Yaml();
                        Map ymlMap = yaml.load(integrationString);
                        DemistoIntegrationYML integrationYML = new DemistoIntegrationYML(ymlMap);
                        String script = currentDoc.getText();
                        integrationYML.getScript().setScript(script);
                        String uniqueID = getRandomID();
                        integrationYML.setName(uniqueID);
                        integrationYML.commonfields.setId(uniqueID);
                        script = getDemistoScriptWithImports(script);
                        String finalScript = script;
                        // IntelliJ uses single write lock for files, therefore write actions should be performed only in the EDT
                        Runnable r = () -> currentDoc.setText(finalScript);
                        DemistoUtils.runActionInEDT(r);
                        // get string representation of the updated automation yml
                        Map demistoMap = integrationYML.getDemistoYMLMapWithUnsupportedFields(integrationYML);
                        String demistoStringYML = DemistoUtils.getYMLStringFromMap(demistoMap);
                        // write yml to string
                        DemistoUtils.writeStringToFile(ymlFilePath, demistoStringYML);
                        // refresh
                        this.refreshToolWindow(project, toolWindow);
                    });
                    innerPanel.add(createAutomationButton, new GridConstraints(1, 0, 1, 1,
                            GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.FILL_HORIZONTAL,
                            null, null, null));
                    innerPanel.add(createIntegrationButton, new GridConstraints(2, 0, 1, 1,
                            GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.FILL_HORIZONTAL,
                            null, null, null));

                    fatherPanel.add(innerPanel, new GridConstraints(0, 0, 1, 1,
                            GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.FILL_HORIZONTAL,
                            null, null, null));
                    content = contentFactory.createContent(fatherPanel, "", false);
                }
            } else {
                // not a python file, might be a configuration
                String message;
                if (fileExtension.equals("yml")) {
                    message = "If this is a Demisto Automation/Integration YML file and you want to edit the code, click the \"Create Demisto Python\" button above.\n" +
                              "- The code from the Demisto YML will be copied to a new Python file.\n" +
                              "- Imports for using general Demisto functions will be automatically added to the new Demisto Python file.\n " +
                              "- You'll be able to edit the code and the Demisto Settings for this Automation/Integration";
                } else {
                    message = "Select a Demisto YML or Demisto Python file to use Demisto's Add-on";
                }
                JPanel fatherPanel = new JPanel();
                fatherPanel.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
                JPanel innerPanel = new JPanel();
                innerPanel.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
                JPanel messagePanel = new MessagePanel(message);
                innerPanel.add(messagePanel, new GridConstraints(0, 0, 1, 1,
                        GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.FILL_HORIZONTAL,
                        null, null, null));
                fatherPanel.add(innerPanel, new GridConstraints(0, 0, 1, 1,
                        GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.FILL_HORIZONTAL,
                        null, null, null));
                content = contentFactory.createContent(fatherPanel, "", false);
            }

            // add new content
            toolWindow.getContentManager().addContent(content);
        }
    }
}

