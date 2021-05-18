package com.demisto.plugin.ide.integrations.ui;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang.ArrayUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.demisto.plugin.ide.Events.DEMISTO_DELETE;
import static com.demisto.plugin.ide.automations.ui.AutomationArgumentPanel.DEMISTO_DELETE_ICON;

/**
 * @author Shachar Hirshberg
 * @since January 2, 2019
 */
public class IntegrationParameterPanel extends JPanel {
    MessageBus msgBus;

    private JTextArea nameTextArea;
    private JTextArea displayTextArea;
    private JLabel typeLabel;
    private ComboBox typeDropdown;
    private JCheckBox mandatoryCheckbox;
    private JButton deleteParameterButton;
    private JLabel initialValueLabel;
    private JTextArea initialValueTextArea;
    private JLabel optionsLabel;
    private JTextField optionsTextfield;
    private JLabel additionalinfoLabel;
    private JTextArea additionalinfoTextArea;

    public static final String YML_CHANGE_INTEGRATION_PARAMETERS = "yml_change_integration_parameters";
    public static final String YML_CHANGE_INTEGRATION_PARAMETER_FIELD = "yml_change_integration_parameters_field";
    public static final String PARAMETER_DEFAULT_TYPE = "Short Text";
    public static final Border DEFAULT_TEXT_BORDER = BorderFactory.createCompoundBorder(
            LineBorder.createGrayLineBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5));

    String[] typeOptionsArray = {
            "Boolean",
            "Short Text",
            "Encrypted",
            "Authentication",
            "Long Text",
            "Single Select",
            "Multi Select"
    };

    String[] typesWithoutInitialValue = {
            "Boolean",
            "Authentication",
            "Multi Select"
    };

    String[] typesWithOptions = {
            "Single Select",
            "Multi Select"
    };

    Map<Integer, String> typeIntToStringMapping = new HashMap<Integer, String>() {{
        put(0, "Short Text");
        put(4, "Encrypted");
        put(8, "Boolean");
        put(9, "Authentication");
        put(12, "Long Text");
        put(15, "Single Select");
        put(16, "Multi Select");
    }};

    Map<String, Integer> typeStringToIntMapping = new HashMap<String, Integer>() {{
        put("Short Text", 0);
        put("Encrypted", 4);
        put("Boolean", 8);
        put("Authentication", 9);
        put("Long Text", 12);
        put("Single Select", 15);
        put("Multi Select", 16);
    }};

    public IntegrationParameterPanel(DemistoIntegrationYML.DemistoParameter demistoParameter, Project project) {
        try {
            msgBus = project.getMessageBus();
        } catch (NullPointerException e) {
            // this is used to be able to test this panel
        }
        // Panel for the parameters of the automation
        JLabel nameLabel = new JLabel();
        nameTextArea = new JTextArea(demistoParameter.getName());
        nameTextArea.setName("nameTextArea");
        nameTextArea.setBorder(DEFAULT_TEXT_BORDER);
        nameTextArea.setLineWrap(true);
        nameTextArea.setFont(nameLabel.getFont());
        JLabel displayLabel = new JLabel();
        displayTextArea = new JTextArea(demistoParameter.getDisplay(), 1, 1);
        displayTextArea.setName("displayTextArea");
        displayTextArea.setLineWrap(true);
        displayTextArea.setWrapStyleWord(true);
        displayTextArea.setBorder(DEFAULT_TEXT_BORDER);
        displayTextArea.setFont(nameLabel.getFont());

        this.setLayout(new GridLayoutManager(7, 4, JBUI.insets(5, 5), -1, -1));
        this.setBorder(BorderFactory.createTitledBorder(""));

        //---- nameLabel ----
        nameLabel.setLabelFor(nameTextArea);
        nameLabel.setText("Parameter name");
        nameTextArea.getDocument().addDocumentListener(new DocumentListener() {
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
                demistoParameter.setName(nameTextArea.getText().trim());
                firePropertyChange(YML_CHANGE_INTEGRATION_PARAMETER_FIELD, false, true);
            }
        });
        this.add(nameLabel, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(nameTextArea, new GridConstraints(
                0, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //---- Type dropdown----
        typeLabel = new JLabel();
        typeLabel.setLabelFor(typeDropdown);
        typeLabel.setText("Type");
        Arrays.sort(typeOptionsArray);
        typeDropdown = new ComboBox(typeOptionsArray);
        typeDropdown.setName("typeDropdown");
        typeDropdown.setBorder(LineBorder.createGrayLineBorder());
        String currentType = mapTypeIntToString(demistoParameter.getType());
        if (DemistoUtils.stringIsNotEmptyOrNull(currentType)) {
            typeDropdown.setSelectedItem(currentType);
        } else {
            typeDropdown.setSelectedItem(PARAMETER_DEFAULT_TYPE);
        }

        this.add(typeLabel, new GridConstraints(1, 0, 1, 1,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        this.add(typeDropdown, new GridConstraints(1, 1, 1, 2,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        typeDropdown.addActionListener(e -> {
            ComboBox comboBox = (ComboBox) e.getSource();
            String selectedType = (String) comboBox.getSelectedItem();
            demistoParameter.setType(mapTypeStringToInt(selectedType));
            firePropertyChange(YML_CHANGE_INTEGRATION_PARAMETER_FIELD, false, true);
            if (ArrayUtils.contains(typesWithoutInitialValue, selectedType)) {
                initialValueLabel.setVisible(false);
                initialValueTextArea.setVisible(false);
            } else {
                initialValueTextArea.setVisible(true);
                initialValueLabel.setVisible(true);
            }
            if (ArrayUtils.contains(typesWithOptions, selectedType)) {
                optionsLabel.setVisible(true);
                optionsTextfield.setVisible(true);
            } else {
                optionsLabel.setVisible(false);
                optionsTextfield.setVisible(false);
            }
        });

        //---- isMandatory checkbox----
        mandatoryCheckbox = new JCheckBox("Mandatory", null, demistoParameter.getRequired());
        mandatoryCheckbox.setName("isMandatoryCheckbox");

        this.add(mandatoryCheckbox, new GridConstraints(1, 3, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));
        mandatoryCheckbox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            demistoParameter.setRequired(selected);
            firePropertyChange(YML_CHANGE_INTEGRATION_PARAMETER_FIELD, false, true);
        });

        //---- displayLabel ----
        displayLabel.setLabelFor(displayTextArea);
        displayLabel.setText("Display name");

        displayTextArea.getDocument().addDocumentListener(new DocumentListener() {
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
                demistoParameter.setDisplay(displayTextArea.getText());
                firePropertyChange(YML_CHANGE_INTEGRATION_PARAMETER_FIELD, false, true);
            }
        });
        this.add(displayLabel, new GridConstraints(
                2, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(displayTextArea, new GridConstraints(
                2, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        //---- initialValueLabel ----
        initialValueLabel = new JLabel();
        initialValueLabel.setLabelFor(initialValueTextArea);
        initialValueLabel.setText("Initial value");
        initialValueTextArea = new JTextArea(demistoParameter.getDefaultvalue());
        initialValueTextArea.setName("initialValueTextArea");
        initialValueTextArea.setBorder(DEFAULT_TEXT_BORDER);
        initialValueTextArea.setLineWrap(true);
        initialValueTextArea.setFont(nameLabel.getFont());
        initialValueTextArea.getDocument().addDocumentListener(new DocumentListener() {
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
                demistoParameter.setDefaultvalue(initialValueTextArea.getText().trim());
                firePropertyChange(YML_CHANGE_INTEGRATION_PARAMETER_FIELD, false, true);
            }
        });
        this.add(initialValueLabel, new GridConstraints(
                3, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(initialValueTextArea, new GridConstraints(
                3, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        if (ArrayUtils.contains(typesWithoutInitialValue, mapTypeIntToString(demistoParameter.getType()))) {
            initialValueLabel.setVisible(false);
            initialValueTextArea.setVisible(false);
        }

        //---- optionsLabel ----
        optionsTextfield = new JTextField(String.join(",", demistoParameter.getOptions()));
        optionsTextfield.setName("optionsTextfield");
        optionsTextfield.setBorder(DEFAULT_TEXT_BORDER);
        optionsLabel = new JLabel();
        optionsLabel.setLabelFor(optionsTextfield);
        optionsLabel.setText("Options");
        optionsTextfield.setFont(nameLabel.getFont());
        optionsTextfield.getDocument().addDocumentListener(new DocumentListener() {
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
                demistoParameter.setOptions(new ArrayList<>(Arrays.asList(optionsTextfield.getText().trim().split(","))));
                firePropertyChange(YML_CHANGE_INTEGRATION_PARAMETER_FIELD, false, true);
            }
        });

        this.add(optionsLabel, new GridConstraints(
                4, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(optionsTextfield, new GridConstraints(
                4, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        if (!ArrayUtils.contains(typesWithOptions, mapTypeIntToString(demistoParameter.getType()))) {
            optionsLabel.setVisible(false);
            optionsTextfield.setVisible(false);
        }

        //---- additionalinfoLabel ----
        additionalinfoLabel = new JLabel();
        additionalinfoLabel.setLabelFor(additionalinfoTextArea);
        additionalinfoLabel.setText("Additional Information");
        additionalinfoTextArea = new JTextArea(demistoParameter.getadditionalinfo());
        additionalinfoTextArea.setName("additionalinfoTextArea");
        additionalinfoTextArea.setBorder(DEFAULT_TEXT_BORDER);
        additionalinfoTextArea.setLineWrap(true);
        additionalinfoTextArea.setFont(nameLabel.getFont());
        additionalinfoTextArea.getDocument().addDocumentListener(new DocumentListener() {
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
                demistoParameter.setadditionalinfo(additionalinfoTextArea.getText().trim());
                firePropertyChange(YML_CHANGE_INTEGRATION_PARAMETER_FIELD, false, true);
            }
        });
        this.add(additionalinfoLabel, new GridConstraints(
                5, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(additionalinfoTextArea, new GridConstraints(
                5, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        // ---- Delete Argument button ----
        deleteParameterButton = new JButton("Delete Parameter", IconLoader.getIcon(DEMISTO_DELETE_ICON));
        deleteParameterButton.setName("deleteParameterButton");
        deleteParameterButton.addActionListener(e ->
        {
            msgBus.syncPublisher(DEMISTO_DELETE).deleteDemistoParameter(nameTextArea.getText().trim());
        });
        this.add(deleteParameterButton, new GridConstraints(6, 3, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

    }

    public String mapTypeIntToString(int type) {
        return typeIntToStringMapping.get(type);
    }

    public int mapTypeStringToInt(String type) {
        return typeStringToIntMapping.get(type);
    }
}
