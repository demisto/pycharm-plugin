package com.demisto.plugin.ide.integrations.ui;

import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Arrays;

import static com.demisto.plugin.ide.automations.ui.DemistoAutomationActionsPanel.TITLE_FONT_SIZE;

public class IntegrationBasicSettingsPanel extends JPanel {

    private JTextField nameTextField;
    private JTextArea descriptionTextField;
    private JLabel categoryLabel;
    private ComboBox categoryDropdown;
    private JCheckBox isFetchCheckBox;
    private JCheckBox feedCheckBox;

    public static final String YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD = "yml_change_integration_basic_settings_field";

    String[] categoryOptionsArray = {
            "Data Enrichment & Threat Intelligence",
            "Case Management",
            "Deception",
            "Forensics & Malware Analysis",
            "Analytics & SIEM",
            "End Point",
            "Authentication",
            "Ticketing",
            "Database",
            "Messaging",
            "Vulnerability Management",
            "IT Services",
            "Network Security",
            "Email Gateway",
            "Utilities",
            "Endpoint",
    };

    public IntegrationBasicSettingsPanel(DemistoIntegrationYML integrationYML) {

        // Panel for the basic settings of the automation

        // children belonging to the basic settings panel
        JLabel nameLabel = new JLabel();
        nameTextField = new JTextField(integrationYML.getDisplay());
        nameTextField.setName("nameTextField");
        nameTextField.setBorder(LineBorder.createGrayLineBorder());
        JLabel descriptionLabel = new JLabel();
        descriptionTextField = new JTextArea(integrationYML.getDescription(), 1, 1);
        descriptionTextField.setName("descriptionTextField");
        descriptionTextField.setLineWrap(true);
        descriptionTextField.setWrapStyleWord(true);
        descriptionTextField.setBorder(BorderFactory.createCompoundBorder(
                LineBorder.createGrayLineBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        this.setLayout(new GridLayoutManager(5, 4, JBUI.insets(0, 5), -1, -1));
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Basic");
        titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(TITLE_FONT_SIZE));
        this.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        //---- nameLabel ----
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
                integrationYML.setDisplay(nameTextField.getText());
                firePropertyChange(YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD, false, true);
            }
        });
        this.add(nameLabel, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(nameTextField, new GridConstraints(
                0, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //---- descriptionLabel ----
        descriptionLabel.setLabelFor(descriptionTextField);
        descriptionLabel.setText("Description");
        descriptionTextField.setFont(descriptionLabel.getFont());
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
                integrationYML.setDescription(descriptionTextField.getText());
                firePropertyChange(YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD, false, true);
            }
        });
        this.add(descriptionLabel, new GridConstraints(
                1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(descriptionTextField, new GridConstraints(
                1, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        //---- Category dropdown----
        categoryLabel = new JLabel();
        categoryLabel.setLabelFor(categoryDropdown);
        categoryLabel.setText("Category *");
        Arrays.sort(categoryOptionsArray);
        categoryDropdown = new ComboBox(categoryOptionsArray);
        categoryDropdown.setName("categoryDropdown");
        categoryDropdown.setBorder(LineBorder.createGrayLineBorder());
        String currentType = integrationYML.getCategory();
        if (!currentType.equals("Unknown")) {
            categoryDropdown.setSelectedItem(currentType);
        }

        this.add(categoryLabel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        this.add(categoryDropdown, new GridConstraints(2, 1, 1, 2,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        categoryDropdown.addActionListener(e -> {
            ComboBox comboBox = (ComboBox) e.getSource();
            String selectedType = (String) comboBox.getSelectedItem();
            integrationYML.setCategory(selectedType);
            firePropertyChange(YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD, false, true);
        });

        //---- isFetch checkbox----
        isFetchCheckBox = new JCheckBox("Fetches Incidents", null, integrationYML.getScript().getIsfetch());
        isFetchCheckBox.setName("isFetchCheckBox");

        this.add(isFetchCheckBox, new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));
        isFetchCheckBox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            integrationYML.getScript().setIsfetch(selected);
            firePropertyChange(YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD, false, true);
        });

        //---- feed checkbox----
        feedCheckBox = new JCheckBox("Fetches Indicators", null, integrationYML.getScript().getFeed());
        feedCheckBox.setName("feedCheckBox");

        this.add(feedCheckBox, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));
        feedCheckBox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            integrationYML.getScript().setFeed(selected);
            firePropertyChange(YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD, false, true);
        });
    }
}
