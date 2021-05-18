package com.demisto.plugin.ide.plugin_settings_setup;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * This ProjectConfigurable class appears on Settings dialog,
 * to let user to configure this plugin's behavior.
 */
public class DemistoSetupConfigurable implements SearchableConfigurable {

    private DemistoSetupGUI mGUI;

    private final DemistoSetupConfig mConfig;

    @SuppressWarnings("FieldCanBeLocal")
    private final Project mProject;

    public DemistoSetupConfigurable(@NotNull Project project) {
        mProject = project;
        mConfig = DemistoSetupConfig.getInstance(project);
    }

    public DemistoSetupConfig getmConfig() {
        return mConfig;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Demisto Setup Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preference.DemistoSetupConfigurable";
    }

    @NotNull
    @Override
    public String getId() {
        return "preference.DemistoSetupConfigurable";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mGUI = new DemistoSetupGUI();
        mGUI.createUI(mProject);
        return mGUI;
    }

    @Override
    public boolean isModified() {
        return mGUI.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        mGUI.apply();
    }

    @Override
    public void reset() {
        mGUI.reset();
    }

    @Override
    public void disposeUIResources() {
        mGUI = null;
    }

    public DemistoSetupGUI getmGUI() {
        return mGUI;
    }
}
