package com.demisto.plugin.ide.automations.ui;


import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.intellij.openapi.project.Project;
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
import java.util.ArrayList;
import java.util.Arrays;

import static com.demisto.plugin.ide.Events.DEMISTO_ARGUMENT_CHANGE;
import static com.demisto.plugin.ide.Events.DEMISTO_DELETE;

/**
 * @author Shachar Hirshberg
 * @since November 1, 2018
 */
public class AutomationArgumentPanel extends JPanel {

    public static final String YML_CHANGE_AUTOMATION_ARG = "yml_change_automation_argument";
    public static final String YML_CHANGE_AUTOMATION_ARG_FIELD = "yml_change_automation_argument_field";
    private static final String PREDEFINED = "PREDEFINED";
    private final JButton deleteArgumentButton;
    public static final String DEMISTO_DELETE_ICON = "/icons/delete-icon.svg";

    MessageBus msgBus;
    JLabel titleLabel;
    JTextField titleTextField;


    JLabel descriptionLabel;
    JTextArea descriptionText;

    JLabel attributesLabel;
    JCheckBox isMandatoryCheckBox;
    JCheckBox isDefaultCheckBox;
    JCheckBox isSensitiveCheckBox;
    JCheckBox isArrayCheckBox;

    JLabel initialValueLabel;
    JTextField initialValueText;
    JLabel listOptionsLabel;
    JTextArea listOptionsText;

    /**
     * Constructor for the AutomationArgumentPanel object
     *
     * @param arg inputs of the running command
     * @param project project the output belongs to
     * @param commandName name of the command
     */
    public AutomationArgumentPanel(DemistoAutomationYML.DemistoArgument arg, Project project, String commandName) {
        super();

        this.setLayout(new GridLayoutManager(4, 3, JBUI.insets(0, 5), -1, -1));
        try {
            msgBus = project.getMessageBus();
        } catch (NullPointerException e) {
            // this is used to be able to test this panel
        }
        JPanel argDescWrapPanel = new JPanel();
        argDescWrapPanel.setLayout(new GridLayoutManager(2, 2, JBUI.insets(0, 5), -1, -1));

        titleLabel = new JLabel();
        titleLabel.setLabelFor(titleTextField);
        titleLabel.setText("Argument *");
        titleTextField = new JTextField(arg.getName());
        titleTextField.setName("titleTextField");
        titleTextField.setBorder(LineBorder.createGrayLineBorder());
        titleTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                arg.setName(titleTextField.getText());
                firePropertyChange(YML_CHANGE_AUTOMATION_ARG_FIELD, false, true);
                msgBus.syncPublisher(DEMISTO_ARGUMENT_CHANGE).updateDemistoEvent();
            }
        });

        argDescWrapPanel.add(titleLabel, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        argDescWrapPanel.add(titleTextField, new GridConstraints(
                0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        descriptionLabel = new JLabel();
        descriptionLabel.setLabelFor(descriptionText);
        descriptionLabel.setText("Description");
        descriptionText = new JTextArea(arg.getDescription());
        descriptionText.setName("descriptionText");
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setBorder(BorderFactory.createCompoundBorder(
                LineBorder.createGrayLineBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        descriptionText.setFont(titleTextField.getFont());
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
                arg.setDescription(descriptionText.getText());
                firePropertyChange(YML_CHANGE_AUTOMATION_ARG_FIELD, false, true);
            }
        });

        argDescWrapPanel.add(descriptionLabel, new GridConstraints(
                1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        argDescWrapPanel.add(descriptionText, new GridConstraints(
                1, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        JPanel checkBoxesWrapPanel = new JPanel();
        checkBoxesWrapPanel.setLayout(new GridLayoutManager(2, 3, JBUI.insets(0, 5), -1, -1));

        attributesLabel = new JLabel("Attributes");
        checkBoxesWrapPanel.add(attributesLabel, new GridConstraints(0, 0, 2, 1,
                GridConstraints.ALIGN_FILL, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        isMandatoryCheckBox = new JCheckBox("Mandatory", null, arg.getRequired());
        isMandatoryCheckBox.setName("isMandatoryCheckBox");

        checkBoxesWrapPanel.add(isMandatoryCheckBox, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));

        isMandatoryCheckBox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            arg.setRequired(selected);
            firePropertyChange(YML_CHANGE_AUTOMATION_ARG_FIELD, false, true);
        });

        isDefaultCheckBox = new JCheckBox("Default", null, arg.getDefault());
        isDefaultCheckBox.setName("isDefaultCheckBox");
        checkBoxesWrapPanel.add(isDefaultCheckBox, new GridConstraints(0, 2, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));

        isDefaultCheckBox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            arg.setDefault(selected);
            firePropertyChange(YML_CHANGE_AUTOMATION_ARG_FIELD, false, true);
        });

        isSensitiveCheckBox = new JCheckBox("Sensitive", null, arg.getSecret());
        isSensitiveCheckBox.setName("isSensitiveCheckBox");
        checkBoxesWrapPanel.add(isSensitiveCheckBox, new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));

        isSensitiveCheckBox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            arg.setSecret(selected);
            firePropertyChange(YML_CHANGE_AUTOMATION_ARG_FIELD, false, true);
        });

        isArrayCheckBox = new JCheckBox("Is array", null, arg.getIsArray());
        isArrayCheckBox.setName("isArrayCheckBox");
        checkBoxesWrapPanel.add(isArrayCheckBox, new GridConstraints(1, 2, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));
        isArrayCheckBox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            arg.setIsArray(selected);
            firePropertyChange(YML_CHANGE_AUTOMATION_ARG_FIELD, false, true);
        });


        JPanel initialValOptionsWrapPanel = new JPanel();
        initialValOptionsWrapPanel.setLayout(new GridLayoutManager(2, 2, JBUI.insets(0, 5), -1, -1));

        initialValueLabel = new JLabel("Initial value");
        initialValueLabel.setLabelFor(initialValueText);
        initialValueText = new JTextField(arg.getDefaultValue());
        initialValueText.setName("initialValueText");
        initialValueText.setBorder(LineBorder.createGrayLineBorder());
        initialValueText.getDocument().addDocumentListener(new DocumentListener() {
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
                arg.setDefaultValue(initialValueText.getText());
                firePropertyChange(YML_CHANGE_AUTOMATION_ARG_FIELD, false, true);
            }
        });

        initialValOptionsWrapPanel.add(initialValueLabel, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        initialValOptionsWrapPanel.add(initialValueText, new GridConstraints(
                0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));


        listOptionsLabel = new JLabel("List options");
        listOptionsLabel.setLabelFor(listOptionsText);

        listOptionsText = new JTextArea(String.join(",", arg.getPredefined()));
        listOptionsText.setLineWrap(true);
        listOptionsText.setWrapStyleWord(true);
        listOptionsText.setBorder(BorderFactory.createCompoundBorder(
                LineBorder.createGrayLineBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        listOptionsText.setName("listOptionsText");
        listOptionsText.setFont(titleTextField.getFont());
        listOptionsText.getDocument().addDocumentListener(new DocumentListener() {
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
                arg.setPredefined(new ArrayList<>(Arrays.asList(listOptionsText.getText().split(","))));
                if (!listOptionsText.getText().trim().equals("")) {
                    arg.setAuto(PREDEFINED);
                } else {
                    arg.setAuto("");
                }
                firePropertyChange(YML_CHANGE_AUTOMATION_ARG_FIELD, false, true);
            }
        });

        initialValOptionsWrapPanel.add(listOptionsLabel, new GridConstraints(
                1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        initialValOptionsWrapPanel.add(listOptionsText, new GridConstraints(
                1, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        // ---- Delete Argument button ----
        JPanel deleteButtonPanel = new JPanel();
        deleteButtonPanel.setLayout(new GridLayoutManager(1, 2, JBUI.insets(0, 5), -1, -1));

        deleteArgumentButton = new JButton("Delete Argument", IconLoader.getIcon(DEMISTO_DELETE_ICON));
        deleteArgumentButton.setName("deleteArgumentButton");
        deleteArgumentButton.addActionListener(e ->
                msgBus.syncPublisher(DEMISTO_DELETE).deleteDemistoArgument(titleTextField.getText(), commandName));
        deleteButtonPanel.add(deleteArgumentButton, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));

        this.add(argDescWrapPanel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(checkBoxesWrapPanel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));
        this.add(initialValOptionsWrapPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(deleteButtonPanel, new GridConstraints(3, 0, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray), BorderFactory.createEmptyBorder(10, 0, 10, 0)));
        this.addPropertyChangeListener(YML_CHANGE_AUTOMATION_ARG_FIELD, evt -> firePropertyChange(YML_CHANGE_AUTOMATION_ARG, false, true));
    }
}

