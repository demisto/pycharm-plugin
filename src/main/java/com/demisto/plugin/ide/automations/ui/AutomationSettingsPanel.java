package com.demisto.plugin.ide.automations.ui;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.Events;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
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
import static com.demisto.plugin.ide.automations.ui.AutomationArgumentPanel.YML_CHANGE_AUTOMATION_ARG;
import static com.demisto.plugin.ide.automations.ui.AutomationBasicSettingsPanel.YML_CHANGE_AUTOMATION_BASIC_SETTINGS;
import static com.demisto.plugin.ide.automations.ui.AutomationOutputPanel.YML_CHANGE_AUTOMATION_OUTPUT;
import static com.demisto.plugin.ide.automations.ui.DemistoAutomationActionsPanel.TITLE_FONT_SIZE;
import static com.demisto.plugin.ide.automations.ui.DemistoAutomationPanel.LOG;
import static com.demisto.plugin.ide.automations.ui.DemistoAutomationPanel.YML_CHANGE_AUTOMATION_PANEL;

/**
 * @author Shachar Hirshberg
 * @since December 22, 2018
 */

public class AutomationSettingsPanel extends JPanel {
    private GridBagLayout argumentsPanelLayout;
    private JButton newArgumentButton;
    private JPanel outputsPanel;
    private JPanel wrapPanel;
    private GridBagLayout outputsPanelLayout;
    private JButton newOutputButton;
    private JButton saveButton;
    private String ymlFilePath;
    private JPanel argumentsPanel;
    private GridBagConstraints argumentsConstraints;
    private DemistoAutomationYML automationYML;
    private Project project;
    private GridBagConstraints outputsConstraints;
    private MessageBus msgBus;
    private GridBagConstraints generalPanelCons = new GridBagConstraints();
    private GridBagLayout settingsPanelLayout = new GridBagLayout();
    public final String PLUS_ICON = "/icons/plus-button.png";

    public AutomationSettingsPanel(DemistoAutomationYML automationYML, Project project, String ymlFilePath) {
        //======== settingsPanel ========
        this.automationYML = automationYML;
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
                        }

                        @Override
                        public void deleteDemistoArgument(@NotNull String argumentName, @NotNull String commandName) {
                            removeDemistoArgument(argumentName);
                        }

                        @Override
                        public void deleteDemistoParameter(@NotNull String parameterName) {
                        }

                        @Override
                        public void deleteDemistoOutput(@NotNull String contextPath, @NotNull String commandName) {
                            removeDemistoOutput(contextPath);
                        }
                    });
        } catch (NullPointerException e) {
            // this is used to be able to test this panel
        }

        generalPanelCons.fill = GridBagConstraints.HORIZONTAL;
        generalPanelCons.weightx = 1;
        generalPanelCons.weighty = 1;
        this.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        wrapPanel = new JPanel();
        wrapPanel.setLayout(new GridLayoutManager(4, 1, JBUI.emptyInsets(), -1, -1));

        //======== basicSettingsPanel ========
        AutomationBasicSettingsPanel basicSettingsPanel = new AutomationBasicSettingsPanel(this.automationYML);

        generalPanelCons.gridwidth = GridBagConstraints.HORIZONTAL;
        generalPanelCons.gridy = 0;
        settingsPanelLayout.setConstraints(basicSettingsPanel, generalPanelCons);
        basicSettingsPanel.addPropertyChangeListener(YML_CHANGE_AUTOMATION_BASIC_SETTINGS, evt -> firePropertyChange(YML_CHANGE_AUTOMATION_PANEL, false, true));
        wrapPanel.add(basicSettingsPanel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));
        //======== argumentsPanel ========

        // Panel for the Automation arguments
        argumentsPanel = createArgumentsPanel();

        this.addPropertyChangeListener(YML_CHANGE_AUTOMATION_PANEL, evt -> {
            saveYML();
        });
        generalPanelCons.gridy = 1;
        generalPanelCons.gridwidth = GridBagConstraints.HORIZONTAL;
        generalPanelCons.weighty = 1;
        generalPanelCons.weightx = 1;
        settingsPanelLayout.setConstraints(argumentsPanel, generalPanelCons);
        wrapPanel.add(argumentsPanel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));
        //======== outputsPanel ========
        // Panel for the outputs
        outputsPanel = createOutputsPanel();
        wrapPanel.add(outputsPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));
        //---- save Button ----
        saveButton = new JButton("Save");
        saveButton.setName("saveButton");
        saveButton.addActionListener(e -> {
            saveYML();
            JOptionPane.showMessageDialog(this, "Saved successfully!");
        });
        generalPanelCons.gridy = 3;
        generalPanelCons.gridx = 1;
        generalPanelCons.weighty = 1;
        generalPanelCons.weightx = 1;
        generalPanelCons.anchor = GridBagConstraints.EAST;
        generalPanelCons.fill = GridBagConstraints.NONE;
        settingsPanelLayout.setConstraints(saveButton, generalPanelCons);
        wrapPanel.add(saveButton, new GridConstraints(3, 0, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.ANCHOR_EAST,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        this.add(wrapPanel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
    }

    private JPanel createOutputsPanel() {
        outputsPanel = new JPanel();
        // automation outputs
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Outputs");
        titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(TITLE_FONT_SIZE));
        outputsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        outputsConstraints = new GridBagConstraints();
        outputsConstraints.fill = GridBagConstraints.HORIZONTAL;
        outputsPanelLayout = new GridBagLayout();
        outputsPanel.setLayout(outputsPanelLayout);
        for (int i = 0; i < automationYML.getOutputs().size(); i++) {
            {
                // we create a panel for each of the outputs in the automationYML object
                AutomationOutputPanel output = new AutomationOutputPanel(automationYML.getOutputs().get(i), project, "");
                outputsConstraints.gridy = i;
                outputsConstraints.weightx = 1.0;
                outputsConstraints.weighty = 1.0;
                outputsConstraints.fill = GridBagConstraints.HORIZONTAL;
                outputsPanelLayout.setConstraints(output, outputsConstraints);
                output.addPropertyChangeListener(YML_CHANGE_AUTOMATION_OUTPUT, evt -> {
                    firePropertyChange(YML_CHANGE_AUTOMATION_PANEL, false, true);
                });
                outputsPanel.add(output);
            }
        }
        generalPanelCons.gridy = 2;
        generalPanelCons.weighty = 1;
        generalPanelCons.weightx = 1;
        settingsPanelLayout.setConstraints(outputsPanel, generalPanelCons);

        // ---- Add output button ----
        newOutputButton = new JButton(" Add Output", IconLoader.getIcon(PLUS_ICON));
        newOutputButton.setName("newOutputButton");
        newOutputButton.addActionListener(e -> {
            addNewOutput();
        });
        outputsConstraints.gridy = automationYML.getOutputs().size();
        outputsConstraints.fill = GridConstraints.FILL_NONE;
        outputsConstraints.anchor = GridBagConstraints.CENTER;
        outputsConstraints.insets = JBUI.insetsTop(10);
        outputsPanelLayout.setConstraints(newOutputButton, outputsConstraints);
        outputsPanel.add(newOutputButton);

        return outputsPanel;
    }

    private JPanel createArgumentsPanel() {
        argumentsPanel = new JPanel();
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Arguments");
        titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(TITLE_FONT_SIZE));
        argumentsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        argumentsConstraints = new GridBagConstraints();
        argumentsConstraints.fill = GridBagConstraints.HORIZONTAL;
        argumentsConstraints.weightx = 1;
        argumentsConstraints.weighty = 1;
        argumentsPanelLayout = new GridBagLayout();
        argumentsPanel.setLayout(argumentsPanelLayout);
        for (int i = 0; i < automationYML.getArgs().size(); i++) {
            // we create a panel for each of the arguments in the automationYML object
            {
                AutomationArgumentPanel arg = new AutomationArgumentPanel(automationYML.getArgs().get(i), project, "");
                arg.setName(automationYML.getArgs().get(i).getName());
                argumentsConstraints.gridy = i;
                argumentsPanelLayout.setConstraints(arg, argumentsConstraints);
                arg.addPropertyChangeListener(YML_CHANGE_AUTOMATION_ARG, evt -> {
                    firePropertyChange(YML_CHANGE_AUTOMATION_PANEL, false, true);
                });
                argumentsPanel.add(arg);
            }
        }

        // ---- Add Argument button ----
        newArgumentButton = new JButton(" Add Argument", IconLoader.getIcon(PLUS_ICON));
        newArgumentButton.setName("newArgumentButton");
        newArgumentButton.addActionListener(e ->
                addNewArgument());
        argumentsConstraints.gridy = automationYML.getArgs().size();
        argumentsConstraints.fill = GridConstraints.FILL_NONE;
        argumentsConstraints.anchor = GridBagConstraints.PAGE_END;
        argumentsConstraints.insets = JBUI.insetsTop(10);
        argumentsPanelLayout.setConstraints(newArgumentButton, argumentsConstraints);
        argumentsPanel.add(newArgumentButton);

        return argumentsPanel;
    }

    private void addNewArgument() {
        argumentsConstraints.gridy = this.automationYML.getArgs().size();
        argumentsConstraints.weightx = 1;
        argumentsConstraints.weighty = 1;
        argumentsConstraints.fill = GridBagConstraints.BOTH;
        argumentsConstraints.insets = JBUI.insets(0);
        DemistoAutomationYML.DemistoArgument demistoArg = this.automationYML.addEmptyArg();
        AutomationArgumentPanel newArg = new AutomationArgumentPanel(demistoArg, project, "");
        argumentsPanelLayout.setConstraints(newArg, argumentsConstraints);
        newArg.addPropertyChangeListener(YML_CHANGE_AUTOMATION_ARG,
                evt -> firePropertyChange(YML_CHANGE_AUTOMATION_PANEL, false, true));
        argumentsPanel.remove(newArgumentButton);
        argumentsPanel.add(newArg, -1);
        argumentsConstraints.gridy = this.automationYML.getArgs().size()+1;
        argumentsConstraints.fill = GridConstraints.FILL_NONE;
        argumentsConstraints.anchor = GridBagConstraints.PAGE_END;
        argumentsConstraints.insets = JBUI.insetsTop(10);
        argumentsPanelLayout.setConstraints(newArgumentButton, argumentsConstraints);
        argumentsPanel.add(newArgumentButton, -1);
        argumentsPanel.revalidate();
    }

    private void addNewOutput() {
        outputsConstraints.gridy = this.automationYML.getOutputs().size();
        outputsConstraints.weightx = 1;
        outputsConstraints.weighty = 1;
        outputsConstraints.fill = GridBagConstraints.BOTH;
        outputsConstraints.insets = JBUI.insets(0);
        DemistoAutomationYML.DemistoOutput emptyOutput = automationYML.addEmptyOutput();
        AutomationOutputPanel newOutput = new AutomationOutputPanel(emptyOutput, project, "");
        outputsPanelLayout.setConstraints(newOutput, outputsConstraints);
        newOutput.addPropertyChangeListener(YML_CHANGE_AUTOMATION_OUTPUT,
                evt -> firePropertyChange(YML_CHANGE_AUTOMATION_PANEL, false, true));
        outputsPanel.remove(newOutputButton);
        outputsPanel.add(newOutput, -1);
        outputsConstraints.gridy = this.automationYML.getOutputs().size()+1;
        outputsConstraints.fill = GridConstraints.FILL_NONE;
        outputsConstraints.anchor = GridBagConstraints.PAGE_END;
        outputsConstraints.insets = JBUI.insetsTop(10);
        outputsPanelLayout.setConstraints(newOutputButton, outputsConstraints);
        outputsPanel.add(newOutputButton, -1);
        outputsPanel.revalidate();
    }

    public void removeDemistoArgument(String name) {
        // removing argument from the argument list
        int index = this.automationYML.findArgumentInArray(name);
        this.automationYML.removeArgument(index);
        // saving the yml with the updated changes
        saveYML();
        // updating the panel
        repaintArgumentsAndOutputs();
        // updating Demisto actions panel
        msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
    }

    public void removeDemistoOutput(String contextPath) {
        // removing output from the output list
        int index = this.automationYML.findOutputInArray(contextPath);
        this.automationYML.removeOutput(index);
        // saving the yml with the updated changes
        saveYML();
        // updating the panel
        repaintArgumentsAndOutputs();
    }

    private void repaintArgumentsAndOutputs() {
        // repaint these parts of the settings panel after changes
        wrapPanel.remove(argumentsPanel);
        wrapPanel.remove(outputsPanel);
        // create arguments panel
        argumentsPanel = createArgumentsPanel();
        wrapPanel.add(argumentsPanel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));
        // create outputs panel
        outputsPanel = createOutputsPanel();
        wrapPanel.add(outputsPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));
        wrapPanel.revalidate();
    }

    public void saveYML() {
        LOG.info("Saving automation YAML, file path: " + ymlFilePath);
        // get string representation of the updated automation yml
        this.automationYML.setScript(DemistoUtils.SCRIPT_DEFAULT);
        Map demistoMap = this.automationYML.getDemistoYMLMapWithUnsupportedFields(this.automationYML);
        String demistoStringYML = DemistoUtils.getYMLStringFromMap(demistoMap);
        // write yml to string
        DemistoUtils.writeStringToFile(ymlFilePath, demistoStringYML);

        // adding latest script
        Document currentDoc = Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getDocument();
        String script = DemistoUtils.removeImportsFromPythonFile(DemistoUtils.ensureTextEndsInNewLine(currentDoc.getText()));
        this.automationYML.setScript(script);

        demistoStringYML = DemistoUtils.getYMLStringFromMap(this.automationYML.getDemistoYMLMapWithUnsupportedFields(this.automationYML));

        // write yml to unified file
        String unifiedFilePath = DemistoUtils.addFilePostfix(ymlFilePath, DemistoUtils.UNIFIED_POSTFIX);
        DemistoUtils.writeStringToFile(unifiedFilePath, demistoStringYML);

        LOG.info("Successfully saved automation YAML");
    }
}
