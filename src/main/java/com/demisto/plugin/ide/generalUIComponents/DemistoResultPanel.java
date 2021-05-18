package com.demisto.plugin.ide.generalUIComponents;

import com.demisto.plugin.ide.DemistoUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;

/**
 * @author Shachar Hirshberg
 * @since December 22, 2018
 * This panel is used to display a single result from Demisto
 */
public class DemistoResultPanel extends JPanel {

    private JTextArea resultTextArea = new JTextArea();
    private JLabel vendorResultOrErrorLabel;
    private String vendorResultOrErrorTitle;

    public DemistoResultPanel(JSONObject result) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;

        this.setBorder(BorderFactory.createLineBorder(Color.darkGray));

        if (DemistoUtils.stringIsNotEmptyOrNull((String) result.get("errorSource"))) {
            // run result was an error
            vendorResultOrErrorTitle = "Received an Error from " + result.get("errorSource") + ":";
        } else if (result.get("brand").equals("Scripts")) {
            // successful Automation run
            vendorResultOrErrorTitle = "Result is:";
        } else {
            // successful Integration run
            vendorResultOrErrorTitle = "Result from " + result.get("brand") + " is:";
        }
        vendorResultOrErrorLabel = DemistoUtils.underlineJLabel(vendorResultOrErrorTitle);
        vendorResultOrErrorLabel.setName("vendorResultOrErrorLabel");
        vendorResultOrErrorLabel.setFont(vendorResultOrErrorLabel.getFont().deriveFont(18.0f));

        gbc.gridy = 0;
        this.add(vendorResultOrErrorLabel, gbc);


        gbc.gridy = 1;
        if (DemistoUtils.stringIsNotEmptyOrNull(String.valueOf(result.get("format"))) && result.get("format").equals("text")){
            if (DemistoUtils.stringIsNotEmptyOrNull(String.valueOf(result.get("file")))) {
                // file
                resultTextArea.setText("File name is: " + result.get("file"));
            } else {
                // regular entry
                resultTextArea.setText(String.valueOf(result.get("contents")));
            }
            resultTextArea.setFont(resultTextArea.getFont().deriveFont(15.0f));
            resultTextArea.setBackground(null); //this is the same as a JLabel
            resultTextArea.setBorder(null);
            resultTextArea.setOpaque(false);
            this.add(resultTextArea, gbc);
        } else {
            // markdown etc.
            ResultLabel resultLabel = new ResultLabel();
            resultLabel.setName("resultLabel");
            resultLabel.setText(DemistoUtils.markdownToHtml(String.valueOf(result.get("contents"))));
            resultLabel.setFont(resultLabel.getFont().deriveFont(15.0f));
            this.add(resultLabel, gbc);
        }
    }
}

