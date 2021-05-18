package com.demisto.plugin.ide.generalUIComponents;

import javax.swing.*;
import java.awt.*;

public class ResultLabel extends JTextPane {

    private static final long serialVersionUID = -1;

    private static final Font DEFAULT_FONT;

    static {
        Font font = UIManager.getFont("Label.font");
        DEFAULT_FONT = (font != null) ? font: new Font("Tahoma", Font.PLAIN, 11);
    }

    public ResultLabel() {
        construct();
    }

    private void construct() {
        setContentType("text/html");

        setEditable(false);
        setBorder(null);
        setBackground(UIManager.getColor("Label.background"));

        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        setFont(DEFAULT_FONT);
    }
}