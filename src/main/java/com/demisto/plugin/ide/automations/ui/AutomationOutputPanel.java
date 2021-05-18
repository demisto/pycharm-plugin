package com.demisto.plugin.ide.automations.ui;

import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static com.demisto.plugin.ide.Events.DEMISTO_DELETE;
import static com.demisto.plugin.ide.automations.ui.AutomationArgumentPanel.DEMISTO_DELETE_ICON;
import static com.demisto.plugin.ide.integrations.ui.IntegrationParameterPanel.DEFAULT_TEXT_BORDER;

/**
 * @author Shachar Hirshberg
 * @since November 1, 2018
 */
public class AutomationOutputPanel extends JPanel {
    public static final String YML_CHANGE_AUTOMATION_OUTPUT = "yml_change_automation_output";
    public static final String YML_CHANGE_AUTOMATION_OUTPUT_FIELD = "yml_change_automation_output_field";
    JLabel contextPath;
    JTextArea contextPathTextField;

    JLabel descriptionLabel;
    JTextArea descriptionText;

    JLabel typeLabel;
    JComboBox typeOptionsDropdown;

    String[] typeOptionsArray = {
            "Unknown",
            "String",
            "Number",
            "Date",
            "Boolean"
    };
    JButton deleteOutputButton;
    MessageBus msgBus;

    /**
     * Constructor for the AutomationOutputPanel object
     *
     * @param output outputs of the running command
     * @param project project the output belongs to
     * @param commandName name of the command
     */
    public AutomationOutputPanel(DemistoAutomationYML.DemistoOutput output, Project project, String commandName) {
        super();

        this.setLayout(new GridLayoutManager(5, 3, JBUI.insets(0, 5), -1, -1));
        this.setName("AutomationOutputPanel");
        try {
            msgBus = project.getMessageBus();
        } catch (NullPointerException e) {
            // this is used to be able to test this panel
        }
        contextPath = new JLabel();
        contextPath.setLabelFor(contextPathTextField);
        contextPath.setText("Context Path");
        contextPathTextField = new JTextArea(output.getContextPath(), 1, 1);
        contextPathTextField.setName("contextPathTextField");
        contextPathTextField.setLineWrap(true);
        contextPathTextField.setWrapStyleWord(true);
        contextPathTextField.setBorder(DEFAULT_TEXT_BORDER);
        contextPathTextField.setFont(contextPath.getFont());
        contextPathTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            private void updateLabel(DocumentEvent e) {
                output.setContextPath(contextPathTextField.getText());
                firePropertyChange(YML_CHANGE_AUTOMATION_OUTPUT_FIELD, false, true);
            }
        });
        this.add(contextPath, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(contextPathTextField, new GridConstraints(0, 1, 1, 2,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        descriptionLabel = new JLabel();
        descriptionLabel.setLabelFor(descriptionText);
        descriptionLabel.setText("Description");

        descriptionText = new JTextArea(output.getDescription(), 1, 1);
        descriptionText.setName("descriptionText");
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setBorder(DEFAULT_TEXT_BORDER);
        descriptionText.setFont(contextPath.getFont());
        descriptionText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            private void updateLabel(DocumentEvent e) {
                output.setDescription(descriptionText.getText());
                firePropertyChange(YML_CHANGE_AUTOMATION_OUTPUT_FIELD, false, true);
            }
        });
        this.add(descriptionLabel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED | GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        this.add(descriptionText, new GridConstraints(1, 1, 1, 2,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        typeLabel = new JLabel();
        typeLabel.setLabelFor(typeOptionsDropdown);
        typeLabel.setText("Type");
        typeOptionsDropdown = new ComboBox(typeOptionsArray);
        typeOptionsDropdown.setName("typeOptionsDropdown");
        typeOptionsDropdown.setBorder(LineBorder.createGrayLineBorder());
        String currentType = output.getType();
        if (!currentType.equals("Unknown")) {
            typeOptionsDropdown.setSelectedItem(currentType);
        }

        this.add(typeLabel, new GridConstraints(3, 0, 1, 1,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        this.add(typeOptionsDropdown, new GridConstraints(3, 1, 1, 2,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        typeOptionsDropdown.addActionListener(e -> {
            ComboBox comboBox = (ComboBox) e.getSource();
            String selectedType = (String) comboBox.getSelectedItem();
            output.setType(selectedType);
            firePropertyChange(YML_CHANGE_AUTOMATION_OUTPUT_FIELD, false, true);
        });
        // ---- Delete Argument button ----
        deleteOutputButton = new JButton("Delete Output", IconLoader.getIcon(DEMISTO_DELETE_ICON));
        deleteOutputButton.setName("deleteArgumentButton");
        deleteOutputButton.addActionListener(e ->
        {
            msgBus.syncPublisher(DEMISTO_DELETE).deleteDemistoOutput(contextPathTextField.getText(), commandName);
        });
        this.add(deleteOutputButton, new GridConstraints(4, 2, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.addPropertyChangeListener(YML_CHANGE_AUTOMATION_OUTPUT_FIELD, evt -> {
            firePropertyChange(YML_CHANGE_AUTOMATION_OUTPUT, false, true);
        });
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray), BorderFactory.createEmptyBorder(10, 0, 10, 0)));
    }
}

