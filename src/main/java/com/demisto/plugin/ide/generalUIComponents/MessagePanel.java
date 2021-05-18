package com.demisto.plugin.ide.generalUIComponents;

import javax.swing.*;
import java.awt.*;

public class MessagePanel extends JPanel {

    private JTextArea messageLabel;

    public MessagePanel(String message) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        messageLabel = new JTextArea(message);
        messageLabel.setName("messageLabel");
        messageLabel.setEditable(false);
        messageLabel.setLineWrap(true);
        messageLabel.setWrapStyleWord(true);
        this.add(messageLabel, gbc);
    }
}