package com.demisto.plugin.ide.generalUIComponents;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.MissingResourceException;

public class CollapsiblePanel extends JPanel {
    private final JButton myToggleCollapseButton;
    private final JComponent myContent;
    private GridBagConstraints titleConstraints;
    private boolean myIsCollapsed;
    private final Collection<DemistoCollapsingListeners> myListeners = ContainerUtil.createLockFreeCopyOnWriteList();
    private boolean myIsInitialized = false;
    private final Icon myExpandIcon;
    private final Icon myCollapseIcon;
    private Label myTitleLabel;
    public static final KeyStroke LEFT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
    public static final KeyStroke RIGHT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
    @NonNls
    public static final String EXPAND = "expand";
    @NonNls
    public static final String COLLAPSE = "collapse";

    public CollapsiblePanel(JComponent content, boolean collapseButtonAtLeft,
                            boolean isCollapsed, Icon collapseIcon, Icon expandIcon,
                            String title, Boolean boldTitle, Boolean centerTitle) {
        super(new GridBagLayout());
        myContent = content;
        setBackground(content.getBackground());
        myExpandIcon = expandIcon;
        myCollapseIcon = collapseIcon;
        Dimension buttonDimension = getButtonDimension();
        myToggleCollapseButton = new JButton();
        myToggleCollapseButton.setOpaque(false);
        myToggleCollapseButton.setBorderPainted(false);
        myToggleCollapseButton.setBackground(content.getBackground());

        myToggleCollapseButton.setSize(buttonDimension);
        myToggleCollapseButton.setPreferredSize(buttonDimension);
        myToggleCollapseButton.setMinimumSize(buttonDimension);
        myToggleCollapseButton.setMaximumSize(buttonDimension);

        myToggleCollapseButton.setFocusable(true);

        myToggleCollapseButton.getActionMap().put(COLLAPSE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                collapse();
            }
        });

        myToggleCollapseButton.getActionMap().put(EXPAND, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                expand();
            }
        });

        myToggleCollapseButton.getInputMap().put(LEFT_KEY_STROKE, COLLAPSE);
        myToggleCollapseButton.getInputMap().put(RIGHT_KEY_STROKE, EXPAND);


        final int iconAnchor = collapseButtonAtLeft ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        add(myToggleCollapseButton,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                        iconAnchor,
                        GridBagConstraints.NONE,
                        JBUI.insets(0, collapseButtonAtLeft ? 5 : -5, 0, collapseButtonAtLeft ? -5 : 5), 0,
                        0));
        if (title != null) {
            myTitleLabel = new Label(title);
            if (boldTitle) {
                myTitleLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
            } else {
                myTitleLabel.setFont(UIUtil.getLabelFont());
            }
            myTitleLabel.setBackground(content.getBackground());
            if (centerTitle) {
                titleConstraints = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        JBUI.insets(0, -3), 0,
                        0);
            } else {
                titleConstraints = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        JBUI.insets(0, 25, 3, 0), 0,
                        0);
            }
            add(myTitleLabel, titleConstraints);

        }

        myToggleCollapseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCollapsed(!myIsCollapsed);
            }
        });
        setCollapsed(isCollapsed);

    }

    private Dimension getButtonDimension() {
        if (myExpandIcon == null) {
            return new Dimension(10, 10);
        } else {
            return new Dimension(myExpandIcon.getIconWidth(), myExpandIcon.getIconHeight());
        }
    }

    public CollapsiblePanel(JComponent content, boolean collapseButtonAtLeft) {
        this(content, collapseButtonAtLeft, false, null, null, null, true, true);
    }

    protected void setCollapsed(boolean collapse) {
        try {
            if (collapse) {
                if (myIsInitialized) remove(myContent);
            } else {
                add(myContent,
                        new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, -3, 0, -3), 0, 0));
            }
            myIsCollapsed = collapse;

            Icon icon = getIcon();
            if (icon != null) {
                myToggleCollapseButton.setIcon(icon);
                myToggleCollapseButton.setBorder(null);
                myToggleCollapseButton.setBorderPainted(false);
                myToggleCollapseButton.setToolTipText(getToggleButtonToolTipText());
            }

            if (collapse) {
                setFocused(true);
                setSelected(true);
            } else {
                myContent.requestFocusInWindow();
            }

            notifyListeners();

            revalidate();
            repaint();
        } finally {
            myIsInitialized = true;
        }
    }

    private String getToggleButtonToolTipText() {
        try {
            if (myIsCollapsed) {
                return ("Expand Panel");
            } else {
                return ("Collapse Panel");
            }
        } catch (MissingResourceException e) {
            return ("Tooltip not found");
        }
    }

    private Icon getIcon() {
        if (myIsCollapsed) {
            return myExpandIcon;
        } else {
            return myCollapseIcon;
        }
    }

    private void notifyListeners() {
        for (DemistoCollapsingListeners listener : myListeners) {
            listener.onCollapsingChanged(this, isCollapsed());
        }
    }

    public void addCollapsingListener(DemistoCollapsingListeners listener) {
        myListeners.add(listener);
    }

    public void removeCollapsingListener(DemistoCollapsingListeners listener) {
        myListeners.remove(listener);
    }

    public boolean isCollapsed() {
        return myIsCollapsed;
    }

    public void expand() {
        if (myIsCollapsed) {
            setCollapsed(false);
        }
    }

    public void collapse() {
        if (!myIsCollapsed) {
            setCollapsed(true);
        }
    }

    public void setFocused(boolean focused) {
        myToggleCollapseButton.requestFocusInWindow();
    }

    public void setSelected(boolean selected) {
        myToggleCollapseButton.setSelected(selected);
    }

    public ActionMap getCollapsibleActionMap() {
        return myToggleCollapseButton.getActionMap();
    }

    public InputMap getCollapsibleInputMap() {
        return myToggleCollapseButton.getInputMap();
    }

    @Override
    protected void paintComponent(Graphics g) {
        updatePanel();
        super.paintComponent(g);
    }

    private void updatePanel() {
        if (paintAsSelected()) {
            setBackground(UIUtil.getTableSelectionBackground());
        } else {
            setBackground(myContent.getBackground());
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        if (myTitleLabel != null) {
            updateTitle();
        }
        updateToggleButton();
        super.paintChildren(g);
    }

    private void updateToggleButton() {
        if (paintAsSelected()) {
            myToggleCollapseButton.setBackground(UIUtil.getTableSelectionBackground());
        } else {
            myToggleCollapseButton.setBackground(myContent.getBackground());
        }
    }

    private void updateTitle() {
        if (paintAsSelected()) {
            myTitleLabel.setForeground(UIUtil.getTableSelectionForeground());
            myTitleLabel.setBackground(UIUtil.getTableSelectionBackground());
        } else {
            myTitleLabel.setForeground(UIUtil.getLabelForeground());
            myTitleLabel.setBackground(myContent.getBackground());
        }
    }

    private boolean paintAsSelected() {
        return myToggleCollapseButton.hasFocus() && isCollapsed();
    }

    public String getTitle() {
        if (this.myTitleLabel != null) {
            return this.myTitleLabel.getText();
        } else {
            return "";

        }
    }

    public void setTitle(String title) {
        if (this.myTitleLabel != null) {
            this.myTitleLabel.setText(title);
            updateTitle();
            this.revalidate();
        }
    }
}
