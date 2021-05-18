package com.demisto.plugin.ide.integrations.ui;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.Events;
import com.demisto.plugin.ide.generalUIComponents.CollapsiblePanel;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;
import java.util.Objects;


import static com.demisto.plugin.ide.Events.DEMISTO_ARGUMENT_CHANGE;
import static com.demisto.plugin.ide.Events.DEMISTO_DELETE;
import static com.demisto.plugin.ide.integrations.ui.DemistoIntegrationPanel.LOG;
import static com.demisto.plugin.ide.integrations.ui.DemistoIntegrationPanel.YML_CHANGE_INTEGRATION_PANEL;
import static com.demisto.plugin.ide.integrations.ui.IntegrationAdvanceSettingsPanel.YML_CHANGE_INTEGRATION_ADVANCE_SETTINGS_FIELD;
import static com.demisto.plugin.ide.integrations.ui.IntegrationBasicSettingsPanel.YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD;
import static com.demisto.plugin.ide.integrations.ui.IntegrationCommandPanel.YML_CHANGE_INTEGRATION_COMMAND_FIELD;
import static com.demisto.plugin.ide.integrations.ui.IntegrationParameterPanel.YML_CHANGE_INTEGRATION_PARAMETERS;
import static com.demisto.plugin.ide.integrations.ui.IntegrationParameterPanel.YML_CHANGE_INTEGRATION_PARAMETER_FIELD;

/**
 * @author Shachar Hirshberg
 * @since December 22, 2018
 */

public class IntegrationSettingsPanel extends JPanel {
    private JPanel parametersPanel;
    private JPanel advanceSettingsPanel;
    private CollapsiblePanel collapsibleAdvanceSettingsPanel;
    private JPanel commandsPanel;
    private JPanel wrapPanel;
    private GridBagLayout parametersPanelLayout;
    private JButton newParamButton;
    private JButton saveButton;
    private String ymlFilePath;
    private DemistoIntegrationYML integrationYML;
    private Project project;
    private GridBagConstraints parametersConstraints;
    private MessageBus msgBus;
    public static String PLUS_ICON = "/icons/plus-button.png";
    public static String MINUS_ICON = "/icons/minus-button.svg";
    private CollapsiblePanel collapsibleParametersPanel;
    private CollapsiblePanel collapsibleCommandsPanel;
    private GridBagConstraints commandsConstraints;
    private GridBagLayout commandsPanelLayout;
    private JButton newCommandButton;

    public IntegrationSettingsPanel(DemistoIntegrationYML integrationYML, Project project, String ymlFilePath) {
        //======== settingsPanel ========
        this.integrationYML = integrationYML;
        this.project = project;
        this.ymlFilePath = ymlFilePath;
        try {
            this.msgBus = project.getMessageBus();
            this.msgBus.connect(project).subscribe(DEMISTO_DELETE,
                    new Events() {
                        @Override
                        public void updateDemistoEvent() {
                        }

                        @Override
                        public void deleteDemistoCommand(@NotNull String commandName) {
                            removeDemistoCommand(commandName);
                        }

                        @Override
                        public void deleteDemistoArgument(@NotNull String argumentName, @NotNull String commandName) {
                        }

                        @Override
                        public void deleteDemistoParameter(@NotNull String parameterName) {
                            removeDemistoParam(parameterName);
                        }

                        @Override
                        public void deleteDemistoOutput(@NotNull String contextPath, @NotNull String commandName) {
                        }
                    });
        } catch (NullPointerException e) {
            // this is used to be able to test this panel
        }

        this.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        wrapPanel = new JPanel();
        wrapPanel.setLayout(new GridLayoutManager(5, 1, JBUI.emptyInsets(), -1, -1));

        //======== basicSettingsPanel ========
        IntegrationBasicSettingsPanel basicSettingsPanel = new IntegrationBasicSettingsPanel(this.integrationYML);
        basicSettingsPanel.addPropertyChangeListener(YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD, evt -> firePropertyChange(YML_CHANGE_INTEGRATION_PANEL, false, true));
        wrapPanel.add(basicSettingsPanel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));

        //======== parametersPanel ========

        collapsibleParametersPanel = createCollapsibleParametersPanel(true);
        wrapPanel.add(collapsibleParametersPanel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));

        //======== commandsPanel ========

        collapsibleCommandsPanel = createCollapsibleCommandsPanel(true);
        wrapPanel.add(collapsibleCommandsPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));

        //======== advanceSettingsPanel ========

        collapsibleAdvanceSettingsPanel = createCollapsibleIntegrationAdvanceSettingsPanel(true);
        wrapPanel.add(collapsibleAdvanceSettingsPanel, new GridConstraints(3, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));

        //======== save Button ========

        saveButton = new JButton("Save");
        saveButton.setName("saveButton");
        saveButton.addActionListener(e -> {
            saveYML();
            JOptionPane.showMessageDialog(this, "Saved successfully!");
        });
        wrapPanel.add(saveButton, new GridConstraints(4, 0, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.ANCHOR_EAST,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //======== Add wrap panel to main panel ========

        this.addPropertyChangeListener(YML_CHANGE_INTEGRATION_PANEL, evt -> saveYML());
        this.add(wrapPanel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
    }

    private JPanel createParametersPanel() {
        // Integration Parameters
        parametersPanel = new JPanel();
        parametersPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        parametersConstraints = new GridBagConstraints();
        parametersConstraints.fill = GridBagConstraints.HORIZONTAL;
        parametersPanelLayout = new GridBagLayout();
        parametersPanel.setLayout(parametersPanelLayout);
        for (int i = 0; i < integrationYML.getConfiguration().size(); i++) {
            {
                // we create a panel for each of the parameters in the integrationYML object
                IntegrationParameterPanel param = new IntegrationParameterPanel(integrationYML.getConfiguration().get(i), project);
                parametersConstraints.gridy = i;
                parametersConstraints.weightx = 1.0;
                parametersConstraints.weighty = 1.0;
                parametersConstraints.fill = GridBagConstraints.HORIZONTAL;
                parametersConstraints.insets = JBUI.insets(3, 0);
                parametersPanelLayout.setConstraints(param, parametersConstraints);
                parametersPanel.add(param);
            }
        }

        // ---- Add Parameter button ----
        newParamButton = new JButton(" Add Parameter", IconLoader.getIcon(PLUS_ICON));
        newParamButton.setName("newParamButton");
        newParamButton.addActionListener(e -> {
            addNewParam();
        });
        parametersConstraints.gridy = GridBagConstraints.LAST_LINE_START;
        parametersConstraints.fill = GridConstraints.FILL_NONE;
        parametersConstraints.anchor = GridBagConstraints.CENTER;
        parametersConstraints.insets = JBUI.insetsTop(10);
        parametersPanelLayout.setConstraints(newParamButton, parametersConstraints);
        parametersPanel.add(newParamButton);
        parametersPanel.addPropertyChangeListener(YML_CHANGE_INTEGRATION_PARAMETERS, evt -> firePropertyChange(YML_CHANGE_INTEGRATION_PANEL, false, true));

        return parametersPanel;
    }

    private void addNewParam() {
        parametersConstraints.gridy = this.integrationYML.getConfiguration().size();
        parametersConstraints.weightx = 1;
        parametersConstraints.weighty = 1;
        parametersConstraints.fill = GridBagConstraints.BOTH;
        parametersConstraints.insets = JBUI.insets(3, 0);

        DemistoIntegrationYML.DemistoParameter emptyParam = this.integrationYML.addEmptyParameter();
        IntegrationParameterPanel newParam = new IntegrationParameterPanel(emptyParam, project);

        parametersPanelLayout.setConstraints(newParam, parametersConstraints);
        newParam.addPropertyChangeListener(YML_CHANGE_INTEGRATION_PARAMETER_FIELD,
                evt -> firePropertyChange(YML_CHANGE_INTEGRATION_PANEL, false, true));
        parametersPanel.add(newParam);
        parametersPanel.revalidate();
    }

    public void removeDemistoParam(String name) {
        // removing param from the param list
        int index = this.integrationYML.findParamInArray(name);
        this.integrationYML.removeParam(index);
        // saving the yml with the updated changes
        saveYML();
        // updating the panel

        repaintSettingsPanel(false, collapsibleCommandsPanel.isCollapsed());
    }

    public CollapsiblePanel createCollapsibleParametersPanel(Boolean panelIsCollapsed) {
        parametersPanel = createParametersPanel();
        collapsibleParametersPanel = new CollapsiblePanel(parametersPanel, true, panelIsCollapsed,
                IconLoader.getIcon(MINUS_ICON), IconLoader.getIcon(PLUS_ICON), "Parameters", true, true);
        TitledBorder titledBorder = BorderFactory.createTitledBorder("");
        collapsibleParametersPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        return collapsibleParametersPanel;
    }

    public CollapsiblePanel createCollapsibleCommandsPanel(Boolean panelIsCollapsed) {
        commandsPanel = createCommandsPanel();
        collapsibleCommandsPanel = new CollapsiblePanel(commandsPanel, true, panelIsCollapsed,
                IconLoader.getIcon(MINUS_ICON), IconLoader.getIcon(PLUS_ICON), "Commands", true, true);
        TitledBorder titledBorder = BorderFactory.createTitledBorder("");
        collapsibleCommandsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        return collapsibleCommandsPanel;
    }

    public CollapsiblePanel createCollapsibleIntegrationAdvanceSettingsPanel(Boolean panelIsCollapsed) {
        advanceSettingsPanel = new IntegrationAdvanceSettingsPanel(integrationYML, project, ymlFilePath);
        advanceSettingsPanel.addPropertyChangeListener(YML_CHANGE_INTEGRATION_ADVANCE_SETTINGS_FIELD, evt -> firePropertyChange(YML_CHANGE_INTEGRATION_PANEL, false, true));
        collapsibleAdvanceSettingsPanel = new CollapsiblePanel(advanceSettingsPanel, true, panelIsCollapsed,
                IconLoader.getIcon(MINUS_ICON), IconLoader.getIcon(PLUS_ICON), "Advanced", true, true);
        TitledBorder titledBorder = BorderFactory.createTitledBorder("");
        collapsibleAdvanceSettingsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        return collapsibleAdvanceSettingsPanel;
    }

    public void removeDemistoCommand(String name) {
        // removing param from the param list
        int index = this.integrationYML.getScript().findCommandInArray(name);
        this.integrationYML.getScript().removeCommand(index);
        // saving the yml with the updated changes
        saveYML();
        // updating the panel
        repaintSettingsPanel(collapsibleParametersPanel.isCollapsed(), false);
        msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
    }

    private void addNewCommand() {
        commandsConstraints.gridy = this.integrationYML.getScript().getCommands().size();
        commandsConstraints.weightx = 1;
        commandsConstraints.weighty = 1;
        commandsConstraints.fill = GridBagConstraints.BOTH;
        commandsConstraints.insets = JBUI.insets(3, 0);

        DemistoIntegrationYML.DemistoCommand emptyCommand = this.integrationYML.getScript().addEmptyCommand();
        IntegrationCommandPanel newCommand = new IntegrationCommandPanel(emptyCommand, project);

        CollapsiblePanel collapsiblePanel = new CollapsiblePanel(newCommand, true, false,
                IconLoader.getIcon(MINUS_ICON), IconLoader.getIcon(PLUS_ICON), emptyCommand.getName(), true, false);
        newCommand.addPropertyChangeListener(YML_CHANGE_INTEGRATION_COMMAND_FIELD, evt -> {
            firePropertyChange(YML_CHANGE_INTEGRATION_PANEL, false, true);
            if (!(collapsiblePanel.getTitle().equals(emptyCommand.getName()))) {
                collapsiblePanel.setTitle(emptyCommand.getName());
                msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
            }
        });
        TitledBorder titledBorder = BorderFactory.createTitledBorder("");
        collapsiblePanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        commandsPanelLayout.setConstraints(collapsiblePanel, commandsConstraints);
        commandsPanel.add(collapsiblePanel);
        commandsPanel.revalidate();
        msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
    }

    private JPanel createCommandsPanel() {
        commandsPanel = new JPanel();
        // integration commands
        TitledBorder titledBorder = BorderFactory.createTitledBorder("");
        commandsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        commandsConstraints = new GridBagConstraints();
        commandsConstraints.fill = GridBagConstraints.HORIZONTAL;
        commandsPanelLayout = new GridBagLayout();
        commandsPanel.setLayout(commandsPanelLayout);
        for (int i = 0; i < integrationYML.getScript().getCommands().size(); i++) {
            {
                // we create a panel for each of the commands in the integrationYML object
                DemistoIntegrationYML.DemistoCommand currentCommand = integrationYML.getScript().getCommands().get(i);
                IntegrationCommandPanel command = new IntegrationCommandPanel(currentCommand, project);
                CollapsiblePanel collapsiblePanel = new CollapsiblePanel(command, true, true, IconLoader.getIcon(MINUS_ICON),
                        IconLoader.getIcon(PLUS_ICON), currentCommand.getName(), true, false);
                collapsiblePanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
                commandsConstraints.gridy = i;
                commandsConstraints.weightx = 1.0;
                commandsConstraints.weighty = 1.0;
                commandsConstraints.fill = GridBagConstraints.HORIZONTAL;
                commandsPanelLayout.setConstraints(collapsiblePanel, commandsConstraints);
                command.addPropertyChangeListener(YML_CHANGE_INTEGRATION_COMMAND_FIELD, evt -> {
                    firePropertyChange(YML_CHANGE_INTEGRATION_PANEL, false, true);
                    if (!(collapsiblePanel.getTitle().equals(currentCommand.getName()))) {
                        collapsiblePanel.setTitle(currentCommand.getName());
                        msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
                    }
                });

                commandsPanel.add(collapsiblePanel);
            }
        }

        // ---- Add command button ----
        newCommandButton = new JButton(" Add Command", IconLoader.getIcon(PLUS_ICON));
        newCommandButton.setName("newCommandButton");
        newCommandButton.addActionListener(e -> {
            addNewCommand();
        });
        commandsConstraints.gridy = GridBagConstraints.LAST_LINE_START;
        commandsConstraints.fill = GridConstraints.FILL_NONE;
        commandsConstraints.anchor = GridBagConstraints.CENTER;
        commandsConstraints.insets = JBUI.insetsTop(10);
        commandsPanelLayout.setConstraints(newCommandButton, commandsConstraints);
        commandsPanel.add(newCommandButton);
        return commandsPanel;
    }

    private void repaintSettingsPanel(Boolean parametersPanelIsCollapsed, Boolean commandsPanelIsCollapsed) {
        // repaint these parts of the settings panel after changes
        wrapPanel.remove(collapsibleParametersPanel);
        collapsibleParametersPanel = createCollapsibleParametersPanel(parametersPanelIsCollapsed);
        wrapPanel.add(collapsibleParametersPanel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));

        wrapPanel.remove(collapsibleCommandsPanel);
        collapsibleCommandsPanel = createCollapsibleCommandsPanel(commandsPanelIsCollapsed);
        wrapPanel.add(collapsibleCommandsPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));

        wrapPanel.revalidate();
    }

    public void saveYML() {
        LOG.info("Saving integration YAML, file path: " + ymlFilePath);
        String detailedDescriptionFilePath = DemistoUtils.renameFileExtension(
                DemistoUtils.addFilePostfix(ymlFilePath, DemistoUtils.DESCRIPTION_POSTFIX), "md");
        String pngFilePath = DemistoUtils.renameFileExtension(
                DemistoUtils.addFilePostfix(ymlFilePath, DemistoUtils.IMAGE_POSTFIX), "png");
        String jpgFilePath = DemistoUtils.renameFileExtension(
                DemistoUtils.addFilePostfix(ymlFilePath, DemistoUtils.IMAGE_POSTFIX), "jpg");

        String description =  DemistoUtils.readFile(detailedDescriptionFilePath);
        String pngBase64 = DemistoUtils.readFile(pngFilePath, true);
        String jpgBase64 = DemistoUtils.readFile(jpgFilePath, true);
        Map unsupportedFields = this.integrationYML.getUnsupportedFields();

        // remove script, description, image if they are in their own files and write yml to string
        this.integrationYML.getScript().setScript(DemistoUtils.SCRIPT_DEFAULT);
        if(!description.isEmpty()) {
            this.integrationYML.setDetaileddescription(DemistoUtils.DETAILED_DESCRIPTION_DEFAULT);
        }

        if(DemistoUtils.stringIsNotEmptyOrNull(pngBase64) || DemistoUtils.stringIsNotEmptyOrNull(jpgBase64)) {
            unsupportedFields.remove("image");
            this.integrationYML.setUnsupportedFields(unsupportedFields);
        }

        Map demistoMap = this.integrationYML.getDemistoYMLMapWithUnsupportedFields(this.integrationYML);
        String demistoStringYML = DemistoUtils.getYMLStringFromMap(demistoMap);
        DemistoUtils.writeStringToFile(ymlFilePath, demistoStringYML);

        // adding latest script
        Document currentDoc = Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getDocument();
        String script = DemistoUtils.removeImportsFromPythonFile(DemistoUtils.ensureTextEndsInNewLine(currentDoc.getText()));
        this.integrationYML.getScript().setScript(script);

        // Insert image and description if they are in their own files
        unsupportedFields = integrationYML.getUnsupportedFields();
        String imageBase64 = "";
        if (DemistoUtils.stringIsNotEmptyOrNull(pngBase64)) {
            imageBase64 = DemistoUtils.BASE64_PNG_PREFIX + pngBase64;
        }
        else if (DemistoUtils.stringIsNotEmptyOrNull(jpgBase64)) {
            imageBase64 = DemistoUtils.BASE64_JPEG_PREFIX + jpgBase64;

        }

        if(DemistoUtils.stringIsNotEmptyOrNull(imageBase64)){
            if (unsupportedFields.containsKey("image")) {
                unsupportedFields.replace("image", imageBase64);
            }
            else {
                unsupportedFields.put("image", imageBase64);
            }
            integrationYML.setUnsupportedFields(unsupportedFields);
        }

        if(DemistoUtils.stringIsNotEmptyOrNull(description)) {
            integrationYML.setDetaileddescription(description);
        }

        LOG.info("Updated integration YAML, trying to save.");

        demistoStringYML = DemistoUtils.getYMLStringFromMap(integrationYML.getDemistoYMLMapWithUnsupportedFields(integrationYML));

        // write yml to unified file
        String unifiedFilePath = DemistoUtils.addFilePostfix(ymlFilePath, DemistoUtils.UNIFIED_POSTFIX);
        DemistoUtils.writeStringToFile(unifiedFilePath, demistoStringYML);

        LOG.info("Successfully saved integration YAML");
    }
}
