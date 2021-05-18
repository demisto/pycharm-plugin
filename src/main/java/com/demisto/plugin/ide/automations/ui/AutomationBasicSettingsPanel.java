package com.demisto.plugin.ide.automations.ui;

import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.Arrays;

import static com.demisto.plugin.ide.automations.ui.DemistoAutomationActionsPanel.TITLE_FONT_SIZE;

public class AutomationBasicSettingsPanel extends JPanel {

    private JTextField nameTextField;
    private JTextArea descriptionTextField;
    private JTextField tagsTextField;
    public static final String YML_CHANGE_AUTOMATION_BASIC_SETTINGS = "yml_change_automation_basic_settings";
    public static final String YML_CHANGE_AUTOMATION_BASIC_SETTINGS_FIELD = "yml_change_automation_basic_settings_field";

    public AutomationBasicSettingsPanel(DemistoAutomationYML automationYML) {
        // Panel for the basic settings of the automation

        this.setLayout(new GridLayoutManager(3, 2, JBUI.insets(0, 5), -1, -1));
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Basic");
        titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(TITLE_FONT_SIZE));
        this.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        //---- nameLabel ----
        JLabel nameLabel = new JLabel();
        nameTextField = new JTextField(automationYML.getName());
        nameTextField.setName("nameTextField");
        nameTextField.setBorder(LineBorder.createGrayLineBorder());
        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText("Name *");
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
                automationYML.setName(nameTextField.getText());
                firePropertyChange(YML_CHANGE_AUTOMATION_BASIC_SETTINGS_FIELD, false, true);
            }
        });
        this.add(nameLabel, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(nameTextField, new GridConstraints(
                0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //---- descriptionLabel ----
        JLabel descriptionLabel = new JLabel();
        /* automationYML's comment is the automation description */
        descriptionTextField = new JTextArea(automationYML.getComment(), 1, 1);
        descriptionTextField.setName("descriptionTextField");
        descriptionTextField.setLineWrap(true);
        descriptionTextField.setWrapStyleWord(true);
        descriptionTextField.setFont(descriptionLabel.getFont());
        descriptionTextField.setBorder(BorderFactory.createCompoundBorder(
                LineBorder.createGrayLineBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
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
                automationYML.setComment(descriptionTextField.getText());
                firePropertyChange(YML_CHANGE_AUTOMATION_BASIC_SETTINGS_FIELD, false, true);
            }
        });
        this.add(descriptionLabel, new GridConstraints(
                1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(descriptionTextField, new GridConstraints(
                1, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        //---- tagsLabel ----
        JLabel tagsLabel = new JLabel();
        tagsTextField = new JTextField(String.join(",", automationYML.getTags()));
        tagsTextField.setName("tagsTextField");
        tagsTextField.setBorder(LineBorder.createGrayLineBorder());
        tagsLabel.setLabelFor(tagsTextField);
        tagsLabel.setText("Tags");
        tagsTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                automationYML.setTags(new ArrayList<>(Arrays.asList(tagsTextField.getText().split(","))));
                firePropertyChange(YML_CHANGE_AUTOMATION_BASIC_SETTINGS_FIELD, false, true);
            }
        });
        this.add(tagsLabel, new GridConstraints(
                2, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(tagsTextField, new GridConstraints(
                2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.addPropertyChangeListener(YML_CHANGE_AUTOMATION_BASIC_SETTINGS_FIELD, evt -> {
            firePropertyChange(YML_CHANGE_AUTOMATION_BASIC_SETTINGS, false, true);
        });
    }
}
