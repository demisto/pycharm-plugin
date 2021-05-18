package com.demisto.plugin.ide.automations.ui;

import com.demisto.plugin.ide.Events;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.demisto.plugin.ide.DemistoUtils.*;
import static com.demisto.plugin.ide.Events.DEMISTO_ARGUMENT_CHANGE;

/**
 * @author Shachar Hirshberg
 * @since December 25, 2018
 */
public class DemistoAutomationPanel extends JBTabbedPane {
    public static final String YML_CHANGE_AUTOMATION_PANEL = "yml_change_automation_panel";
    public static final Logger LOG = Logger.getInstance(DemistoAutomationPanel.class);
    private JBScrollPane automationActionsPanel;

    private DemistoAutomationYML automationYML;
    private String ymlFilePath;
    private Project project;
    private MessageBus msgBus;

    public DemistoAutomationPanel(DemistoAutomationYML automationYML, String ymlFilePath, Project project) {
        this.automationYML = automationYML;
        this.project = project;
        try {
            this.msgBus = project.getMessageBus();
            msgBus.connect(project).subscribe(DEMISTO_ARGUMENT_CHANGE,
                    new Events() {
                        @Override
                        public void updateDemistoEvent() {
                            refreshActionsPanel();
                        }

                        @Override
                        public void deleteDemistoCommand(@NotNull String commandName) {

                        }

                        @Override
                        public void deleteDemistoArgument(@NotNull String argumentName, @NotNull String commandName) {
                        }

                        @Override
                        public void deleteDemistoParameter(@NotNull String parameterName) {

                        }

                        @Override
                        public void deleteDemistoOutput(@NotNull String contextPath, @NotNull String commandName) {
                        }
                    });
        } catch (NullPointerException e) {
            // this is used to be able to test this panel
        }
        this.ymlFilePath = ymlFilePath;

        LOG.info("Creating DemistoAutomationPanel");

        JBScrollPane automationScrollPanel = createSettingsPanel();
        String activeTabKey = createUserPreferenceKey(automationYML.getName(), "", "activeTab");
        String preference = getSingleUserPreference(activeTabKey);
        int activeTab;
        if (stringIsNotEmptyOrNull(preference)) {
            activeTab = Integer.valueOf(preference);
        } else {
            activeTab = 0;
        }
        this.addChangeListener(e -> {
            JTabbedPane tabSource = (JTabbedPane) e.getSource();
            setSingleUserPreference(activeTabKey, String.valueOf(tabSource.getSelectedIndex()));
        });
        this.automationActionsPanel = createAutomationActionsPanel();
        this.setTabLayoutPolicy(JTabbedPane.TOP);
        this.addTab("Automation Settings", automationScrollPanel);
        this.addTab("Run Automation", automationActionsPanel);
        this.setSelectedIndex(activeTab);
        this.setVisible(true);

    }

    public void refreshActionsPanel() {
        // we want to update the Demisto actions tab if a new argument was created or existing argument has changed
        this.automationActionsPanel = createAutomationActionsPanel();
        this.removeTabAt(1);
        this.addTab("Demisto Actions", automationActionsPanel);
    }

    private JBScrollPane createSettingsPanel() {
        JPanel settingsPanel = new AutomationSettingsPanel(this.automationYML, this.project, this.ymlFilePath);
        return new JBScrollPane(settingsPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JBScrollPane createAutomationActionsPanel() {
        JPanel automationActionsPanel = new DemistoAutomationActionsPanel(this.automationYML, this.project, this.ymlFilePath);
        return new JBScrollPane(automationActionsPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
}
