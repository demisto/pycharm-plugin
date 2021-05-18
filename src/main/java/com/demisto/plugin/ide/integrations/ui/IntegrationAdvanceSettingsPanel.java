package com.demisto.plugin.ide.integrations.ui;


import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;


import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class IntegrationAdvanceSettingsPanel extends JPanel {

    private JLabel descriptionLinkField;
    private JLabel dockerLabel;
    private JCheckBox isBetaCheckbox;
    private JTextField dockerimageTextField;
    private JCheckBox isSeparateContainer;
    private String ymlFilePath;

    public static final String YML_CHANGE_INTEGRATION_ADVANCE_SETTINGS_FIELD = "yml_change_integration_advance_settings_field";

    public IntegrationAdvanceSettingsPanel(DemistoIntegrationYML integrationYML, Project project, String ymlFilePath) {
        this.setLayout(
                new GridLayoutManager(
                        5,
                        3,
                        JBUI.insets(1, 5),
                        -1,
                        -1)
        );
        TitledBorder titledBorder = BorderFactory.createTitledBorder("");
        this.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        this.ymlFilePath = ymlFilePath;


        //---- descriptionLabel ----
        String detailedDescriptionFilePath = DemistoUtils.renameFileExtension(
                DemistoUtils.addFilePostfix(ymlFilePath, DemistoUtils.DESCRIPTION_POSTFIX), "md");

        JLabel descriptionLabel = new JLabel();
        descriptionLinkField = new JLabel("Edit description");
        descriptionLinkField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        descriptionLinkField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                VirtualFile detailedDescriptionFile;
                detailedDescriptionFile = VfsUtil.findFileByIoFile(new File(detailedDescriptionFilePath), true);
                if (detailedDescriptionFile != null) {
                    DemistoUtils.openFile(detailedDescriptionFile, project);
                }
            }
        });


        descriptionLabel.setLabelFor(descriptionLinkField);
        descriptionLabel.setText("Detailed Description:");


        this.add(descriptionLabel, new GridConstraints(
                1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null)
        );
        this.add(descriptionLinkField, new GridConstraints(
                1, 1, 1, 2,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null)
        );

        //---- Docker image textField----
        dockerLabel = new JLabel();
        dockerLabel.setLabelFor(dockerimageTextField);
        dockerLabel.setText("Docker image name:");
        dockerimageTextField = new JTextField();
        dockerimageTextField.setName("dockerimageTextField");
        dockerimageTextField.setBorder(LineBorder.createGrayLineBorder());
        String currentImage = integrationYML.getScript().getDockerimage();
        dockerimageTextField.setText(currentImage);
        this.add(dockerLabel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        this.add(dockerimageTextField, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));
        dockerimageTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                integrationYML.getScript().setDockerimage(dockerimageTextField.getText());
                firePropertyChange(YML_CHANGE_INTEGRATION_ADVANCE_SETTINGS_FIELD, false, true);
            }
        });

        //---- isSeparateContainer checkbox----
        isSeparateContainer = new JCheckBox("Run on a separate container", null, integrationYML.getScript().getRunonce());
        isSeparateContainer.setName("isSeparateContainer");

        this.add(isSeparateContainer, new GridConstraints(3, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));
        isSeparateContainer.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            integrationYML.getScript().setRunonce(selected);
        });

        //---- isBeta checkbox----
        Boolean isBeta =  integrationYML.getBeta();
        isBetaCheckbox = new JCheckBox("Beta integration", null, isBeta);
        isBetaCheckbox.setName("isBetaCheckbox");


        this.add(isBetaCheckbox, new GridConstraints(4, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));
        isBetaCheckbox.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();
            integrationYML.setBeta(selected);
        });
    }
}
