package com.demisto.plugin.ide.integrations.ui;

import com.demisto.plugin.ide.DemistoYML;
import com.demisto.plugin.ide.Events;
import com.demisto.plugin.ide.automations.ui.AutomationArgumentPanel;
import com.demisto.plugin.ide.automations.ui.AutomationOutputPanel;
import com.demisto.plugin.ide.generalUIComponents.CollapsiblePanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static com.demisto.plugin.ide.Events.DEMISTO_ARGUMENT_CHANGE;
import static com.demisto.plugin.ide.Events.DEMISTO_DELETE;
import static com.demisto.plugin.ide.automations.ui.AutomationArgumentPanel.DEMISTO_DELETE_ICON;
import static com.demisto.plugin.ide.automations.ui.AutomationArgumentPanel.YML_CHANGE_AUTOMATION_ARG;
import static com.demisto.plugin.ide.automations.ui.AutomationOutputPanel.YML_CHANGE_AUTOMATION_OUTPUT;
import static com.demisto.plugin.ide.integrations.ui.IntegrationParameterPanel.DEFAULT_TEXT_BORDER;
import static com.demisto.plugin.ide.integrations.ui.IntegrationSettingsPanel.MINUS_ICON;
import static com.demisto.plugin.ide.integrations.ui.IntegrationSettingsPanel.PLUS_ICON;

public class IntegrationCommandPanel extends JPanel {
    MessageBus msgBus;

    private JButton deleteCommandButton;
    private JTextField nameTextField;
    private JTextArea descriptionTextField;
    private JCheckBox isHarmfulCheckBox;
    private com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML.DemistoCommand demistoCommand;
    private Project project;
    public static final String YML_CHANGE_INTEGRATION_COMMAND_FIELD = "yml_change_integration_basic_settings_field";
    private JPanel argumentsPanel;
    private JPanel outputsPanel;
    private GridBagConstraints argumentsConstraints;
    private GridBagConstraints outputsConstraints;
    private GridBagLayout argumentsPanelLayout;
    private GridBagLayout outputsPanelLayout;
    private JButton newArgumentButton;
    private JButton newOutputButton;
    private JPanel integrationCommandsSettings;

    public IntegrationCommandPanel(com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML.DemistoCommand demistoCommand, Project project) {
        try {
            msgBus = project.getMessageBus();
        } catch (NullPointerException e) {
            // this is used to be able to test this panel
        }
        this.demistoCommand = demistoCommand;
        this.project = project;

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
                            if (commandName.equals(demistoCommand.getName())){
                                removeDemistoArgument(argumentName);
                            }
                        }

                        @Override
                        public void deleteDemistoParameter(@NotNull String parameterName) {
                        }

                        @Override
                        public void deleteDemistoOutput(@NotNull String contextPath, @NotNull String commandName) {
                            if (commandName.equals(demistoCommand.getName())) {
                                removeDemistoOutput(contextPath);
                            }
                        }
                    });
        } catch (NullPointerException e) {
            // this is used to be able to test this panel
        }
        this.setLayout(new GridLayoutManager(4, 3, JBUI.insets(0, 5), -1, -1));
        TitledBorder titledBorder = BorderFactory.createTitledBorder("");
        this.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        // ---- Integration command settings panel ----

        integrationCommandsSettings = createIntegrationCommandSettingsPanel();
        this.add(integrationCommandsSettings, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        // ---- Arguments panel ----

        argumentsPanel = createArgumentsPanel();
        this.add(argumentsPanel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        // ---- Outputs panel ----

        outputsPanel = createOutputsPanel();
        this.add(outputsPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
    }

    private JPanel createIntegrationCommandSettingsPanel() {
        integrationCommandsSettings = new JPanel();
        integrationCommandsSettings.setLayout(new GridLayoutManager(3, 3, JBUI.insets(0, 5), -1, -1));
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Settings");
        integrationCommandsSettings.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        //---- nameLabel ----
        JLabel nameLabel = new JLabel();
        nameTextField = new JTextField(demistoCommand.getName());
        nameTextField.setName("nameTextField");
        nameTextField.setBorder(LineBorder.createGrayLineBorder());
        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText("Command name *");
        nameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabel();
            }

            private void updateLabel() {
                demistoCommand.setName(nameTextField.getText());
                firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);
            }
        });
        integrationCommandsSettings.add(nameLabel, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        integrationCommandsSettings.add(nameTextField, new GridConstraints(
                0, 1, 1, 2,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //---- descriptionLabel ----
        JLabel descriptionLabel = new JLabel();
        descriptionTextField = new JTextArea(demistoCommand.getDescription(), 1, 1);
        descriptionTextField.setName("descriptionTextField");
        descriptionTextField.setLineWrap(true);
        descriptionTextField.setWrapStyleWord(true);
        descriptionTextField.setBorder(DEFAULT_TEXT_BORDER);
        descriptionTextField.setFont(descriptionLabel.getFont());
        descriptionLabel.setLabelFor(descriptionTextField);
        descriptionLabel.setText("Description");
        descriptionTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabel();
            }

            private void updateLabel() {
                demistoCommand.setDescription(descriptionTextField.getText());
                firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);
            }
        });
        integrationCommandsSettings.add(descriptionLabel, new GridConstraints(
                1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        integrationCommandsSettings.add(descriptionTextField, new GridConstraints(
                1, 1, 1, 2,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        //---- isHarmfulCheckBox checkbox----
        isHarmfulCheckBox = new JCheckBox("Potentially harmful", null, demistoCommand.getExecution());
        isHarmfulCheckBox.setName("isHarmfulCheckBox");

        integrationCommandsSettings.add(isHarmfulCheckBox, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ALIGN_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));
        isHarmfulCheckBox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            demistoCommand.setExecution(selected);
            firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);
        });

        // ---- Delete Command button ----
        deleteCommandButton = new JButton("Delete Command", IconLoader.getIcon(DEMISTO_DELETE_ICON));
        deleteCommandButton.setName("deleteCommandButton");
        deleteCommandButton.addActionListener(e ->
        {
            msgBus.syncPublisher(DEMISTO_DELETE).deleteDemistoCommand(nameTextField.getText().trim());
        });
        integrationCommandsSettings.add(deleteCommandButton, new GridConstraints(2, 1, 1, 2,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        return integrationCommandsSettings;
    }

    private JPanel createArgumentsPanel() {
        argumentsPanel = new JPanel();
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Arguments");
        argumentsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        argumentsConstraints = new GridBagConstraints();
        argumentsConstraints.fill = GridBagConstraints.HORIZONTAL;
        argumentsConstraints.weightx = 1;
        argumentsConstraints.weighty = 1;
        argumentsPanelLayout = new GridBagLayout();
        argumentsPanel.setLayout(argumentsPanelLayout);
        for (int i = 0; i < demistoCommand.getArguments().size(); i++) {
            // we create a panel for each of the arguments in the integrationYML object
            {
                DemistoYML.DemistoArgument currentArg = demistoCommand.getArguments().get(i);
                AutomationArgumentPanel argPanel = new AutomationArgumentPanel(currentArg, project, demistoCommand.getName());
                argPanel.setName(currentArg.getName());
                CollapsiblePanel collapsiblePanel = new CollapsiblePanel(argPanel, true, true, IconLoader.getIcon(MINUS_ICON),
                        IconLoader.getIcon(PLUS_ICON), demistoCommand.getArguments().get(i).getName(), false, false);
                addEmptyBorderToPanel(collapsiblePanel);
                argumentsConstraints.gridy = i;
                argumentsPanelLayout.setConstraints(collapsiblePanel, argumentsConstraints);
                argPanel.addPropertyChangeListener(YML_CHANGE_AUTOMATION_ARG, evt -> {
                    firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);
                    if (!(collapsiblePanel.getTitle().equals(currentArg.getName()))) {
                        collapsiblePanel.setTitle(currentArg.getName());
                    }
                    msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
                });
                argumentsPanel.add(collapsiblePanel);
            }
        }

        // ---- Add Argument button ----
        newArgumentButton = new JButton(" Add Argument", IconLoader.getIcon(PLUS_ICON));
        newArgumentButton.setName("newArgumentButton");
        newArgumentButton.addActionListener(e ->
                addNewArgument()
        );
        argumentsConstraints.gridy = demistoCommand.getArguments().size();
        argumentsConstraints.fill = GridConstraints.FILL_NONE;
        argumentsConstraints.anchor = GridBagConstraints.PAGE_END;
        argumentsConstraints.insets = JBUI.insetsTop(10);
        argumentsPanelLayout.setConstraints(newArgumentButton, argumentsConstraints);
        argumentsPanel.add(newArgumentButton);

        return argumentsPanel;
    }

    public void addEmptyBorderToPanel(JPanel panel) {
        TitledBorder argTitledBorder = BorderFactory.createTitledBorder("");
        panel.setBorder(BorderFactory.createCompoundBorder(argTitledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
    }

    private void addNewArgument() {
        argumentsConstraints.gridy = this.demistoCommand.getArguments().size();
        argumentsConstraints.weightx = 1;
        argumentsConstraints.weighty = 1;
        argumentsConstraints.fill = GridBagConstraints.BOTH;
        argumentsConstraints.insets = JBUI.insets(0);
        DemistoYML.DemistoArgument demistoArg = this.demistoCommand.addEmptyArg();
        AutomationArgumentPanel newArg = new AutomationArgumentPanel(demistoArg, project, this.demistoCommand.getName());
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel(newArg, true, false,
                IconLoader.getIcon(MINUS_ICON), IconLoader.getIcon(PLUS_ICON), demistoArg.getName(), false, false);
        TitledBorder argTitledBorder = BorderFactory.createTitledBorder("");
        collapsiblePanel.setBorder(BorderFactory.createCompoundBorder(argTitledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        argumentsPanelLayout.setConstraints(collapsiblePanel, argumentsConstraints);
        newArg.addPropertyChangeListener(YML_CHANGE_AUTOMATION_ARG,
                evt -> {
                    firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);
                    if (!(collapsiblePanel.getTitle().equals(demistoArg.getName()))) {
                        collapsiblePanel.setTitle(demistoArg.getName());
                    }
                    msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
                });
        argumentsPanel.remove(newArgumentButton);
        argumentsPanel.add(collapsiblePanel, -1);
        argumentsConstraints.gridy = demistoCommand.getArguments().size()+1;
        argumentsConstraints.fill = GridConstraints.FILL_NONE;
        argumentsConstraints.anchor = GridBagConstraints.PAGE_END;
        argumentsConstraints.insets = JBUI.insetsTop(10);
        argumentsPanelLayout.setConstraints(newArgumentButton, argumentsConstraints);
        argumentsPanel.add(newArgumentButton, -1);
        argumentsPanel.revalidate();
    }

    private JPanel createOutputsPanel() {
        outputsPanel = new JPanel();
        // automation outputs
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Outputs");
        outputsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        outputsConstraints = new GridBagConstraints();
        outputsConstraints.fill = GridBagConstraints.HORIZONTAL;
        outputsPanelLayout = new GridBagLayout();
        outputsPanel.setLayout(outputsPanelLayout);
        for (int i = 0; i < demistoCommand.getOutputs().size(); i++) {
            {
                // we create a panel for each of the outputs in the integrationYML object
                DemistoYML.DemistoOutput output = this.demistoCommand.getOutputs().get(i);
                AutomationOutputPanel outputPanel = new AutomationOutputPanel(output, project, this.demistoCommand.getName());
                CollapsiblePanel collapsiblePanel = new CollapsiblePanel(outputPanel, true, true,
                        IconLoader.getIcon(MINUS_ICON), IconLoader.getIcon(PLUS_ICON), output.getContextPath(), false, false);
                addEmptyBorderToPanel(collapsiblePanel);
                outputsConstraints.gridy = i;
                outputsConstraints.weightx = 1.0;
                outputsConstraints.weighty = 1.0;
                outputsConstraints.fill = GridBagConstraints.HORIZONTAL;
                outputsPanelLayout.setConstraints(collapsiblePanel, outputsConstraints);
                outputPanel.addPropertyChangeListener(YML_CHANGE_AUTOMATION_OUTPUT, evt -> {
                    firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);
                    if (!(collapsiblePanel.getTitle().equals(output.getContextPath()))) {
                        collapsiblePanel.setTitle(output.getContextPath());
                    }
                });

                outputsPanel.add(collapsiblePanel);
            }
        }

        // ---- Add output button ----
        newOutputButton = new JButton(" Add Output", IconLoader.getIcon(PLUS_ICON));
        newOutputButton.setName("newOutputButton");
        newOutputButton.addActionListener(e -> addNewOutput());
        outputsConstraints.gridy = demistoCommand.getOutputs().size();
        outputsConstraints.fill = GridConstraints.FILL_NONE;
        outputsConstraints.anchor = GridBagConstraints.CENTER;
        outputsConstraints.insets = JBUI.insetsTop(10);
        outputsPanelLayout.setConstraints(newOutputButton, outputsConstraints);
        outputsPanel.add(newOutputButton);

        return outputsPanel;
    }

    private void addNewOutput() {
        outputsConstraints.gridy = this.demistoCommand.getOutputs().size();
        outputsConstraints.weightx = 1;
        outputsConstraints.weighty = 1;
        outputsConstraints.fill = GridBagConstraints.BOTH;
        outputsConstraints.insets = JBUI.insets(0);
        DemistoYML.DemistoOutput emptyOutput = demistoCommand.addEmptyOutput();
        AutomationOutputPanel outputPanel = new AutomationOutputPanel(emptyOutput, project, this.demistoCommand.getName());
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel(outputPanel, true, false,
                IconLoader.getIcon(MINUS_ICON), IconLoader.getIcon(PLUS_ICON), emptyOutput.getContextPath(), false, false);
        addEmptyBorderToPanel(collapsiblePanel);
        outputsPanelLayout.setConstraints(collapsiblePanel, outputsConstraints);
        outputPanel.addPropertyChangeListener(YML_CHANGE_AUTOMATION_OUTPUT, evt -> {
            firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);
            if (!(collapsiblePanel.getTitle().equals(emptyOutput.getContextPath()))) {
                collapsiblePanel.setTitle(emptyOutput.getContextPath());
            }
        });

        outputsPanel.remove(newOutputButton);
        outputsPanel.add(collapsiblePanel, -1);
        outputsConstraints.gridy = this.demistoCommand.getOutputs().size()+1;
        outputsConstraints.fill = GridConstraints.FILL_NONE;
        outputsConstraints.anchor = GridBagConstraints.PAGE_END;
        outputsConstraints.insets = JBUI.insetsTop(10);
        outputsPanelLayout.setConstraints(newOutputButton, outputsConstraints);
        outputsPanel.add(newOutputButton, -1);
        outputsPanel.revalidate();
    }

    public void removeDemistoArgument(String name) {
        // removing argument from the argument list
        int index = this.demistoCommand.findArgumentInArray(name);
        this.demistoCommand.removeArgument(index);
        // saving the yml with the updated changes
        // updating the panel
        firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);

        repaintArguments();
        msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
    }

    public void removeDemistoOutput(String contextPath) {
        // removing output from the output list
        int index = this.demistoCommand.findOutputInArray(contextPath);
        this.demistoCommand.removeOutput(index);
        // saving the yml with the updated changes
        firePropertyChange(YML_CHANGE_INTEGRATION_COMMAND_FIELD, false, true);

        // updating the panel
        repaintOutputs();
    }

    private void repaintArguments() {
        // repaint these parts of the settings panel after changes
        this.remove(argumentsPanel);
        // create arguments panel
        argumentsPanel = createArgumentsPanel();
        this.add(argumentsPanel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));
        this.revalidate();
    }

    private void repaintOutputs() {
        // repaint these parts of the settings panel after changes
        this.remove(outputsPanel);
        // create outputs panel
        outputsPanel = createOutputsPanel();
        this.add(outputsPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));
        this.revalidate();
    }
}
