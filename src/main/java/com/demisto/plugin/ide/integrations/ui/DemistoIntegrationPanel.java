package com.demisto.plugin.ide.integrations.ui;

import com.demisto.plugin.ide.Events;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
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
public class DemistoIntegrationPanel extends JBTabbedPane {
    public static final String YML_CHANGE_INTEGRATION_PANEL = "yml_change_integration_panel";
    public static final Logger LOG = Logger.getInstance(DemistoIntegrationPanel.class);
    private JBScrollPane integrationActionsPanel;
    private DemistoIntegrationYML integrationYML;
    private String ymlFilePath;
    private Project project;
    private MessageBus msgBus;

    public DemistoIntegrationPanel(DemistoIntegrationYML integrationYML, String ymlFilePath, Project project) {
        this.integrationYML = integrationYML;
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
        String activeTabKey = createUserPreferenceKey(integrationYML.getName(), "", "activeTab");
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
        JBScrollPane integrationScrollPanel = createSettingsPanel();
        integrationActionsPanel = createIntegrationActionsPanel();
        this.setTabLayoutPolicy(JTabbedPane.TOP);
        this.addTab("Integration Settings", integrationScrollPanel);
        this.addTab("Run Integration", integrationActionsPanel);
        this.setSelectedIndex(activeTab);
        this.setVisible(true);

    }

    public void refreshActionsPanel() {
        // we want to update the Demisto actions tab if a new argument was created or existing argument has changed
        this.integrationActionsPanel = createIntegrationActionsPanel();
        this.removeTabAt(1);
        this.addTab("Demisto Actions", integrationActionsPanel);
    }

    private JBScrollPane createSettingsPanel() {
        JPanel settingsPanel = new IntegrationSettingsPanel(this.integrationYML, this.project, this.ymlFilePath);
        return new JBScrollPane(settingsPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JBScrollPane createIntegrationActionsPanel() {
        JPanel integrationActionsPanel = new DemistoIntegrationActionsPanel(this.integrationYML, this.project, this.ymlFilePath);
        return new JBScrollPane(integrationActionsPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
}
