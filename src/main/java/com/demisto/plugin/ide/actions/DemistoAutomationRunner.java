package com.demisto.plugin.ide.actions;

import com.demisto.plugin.ide.DemistoRESTClient;
import com.demisto.plugin.ide.generalUIComponents.MessagePanel;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.AnActionButton;

import javax.swing.*;

public class DemistoAutomationRunner extends AnActionButton {
    public static final Logger LOG = Logger.getInstance(DemistoAutomationRunner.class);
    private Project project;
    private DemistoRESTClient demistoRESTClient;

    public DemistoAutomationRunner() {
    }

    public DemistoAutomationRunner(Project project) {
        assert project != null;
        this.project = project;
        this.demistoRESTClient = new DemistoRESTClient(project);
    }

    public String runAutomationInDemisto(String query) {
        if (this.demistoRESTClient.getIsMasterMT()) {
            return "[" +
                    "{" +
                    "\"errorSource\": \"IDE Plugin\"," +
                    "\"format\": \"text\"," +
                    "\"contents\": \"Demisto is running in Multi-Tenant Master environment.\\nPlease switch the server's url to one of the tenants\"," +
                    "\"file\": \"null\"" +
                    "}" +
                    "]";
        }
        return this.demistoRESTClient.sendQueryToDemisto(query);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar(ActionPlaces.TOOLBAR, new DefaultActionGroup(), true);
        LOG.info("In DemistoAutomationRunner, Creating new Demisto results panel");
        JComponent c = toolbar.getComponent();
        SimpleToolWindowPanel filterPanel = new SimpleToolWindowPanel(false);
        filterPanel.setToolbar(c);
        // placeholder in which will have the real data later on
        filterPanel.setContent(new MessagePanel("Response from Demisto"));
    }
}
