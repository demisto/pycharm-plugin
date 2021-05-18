package com.demisto.plugin.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * PersistentStateComponent keeps project config values.
 */

@State(
        name = "DemistoUserPreferences",
        storages = {
                @Storage("DemistoUserPreferences.xml")
        }
)
public class DemistoUserPreferences implements ApplicationComponent, PersistentStateComponent<DemistoUserPreferences.State> {
    public State state = new State();

    @Nullable
    public static DemistoUserPreferences getInstance() {
        return ApplicationManager.getApplication().getComponent(DemistoUserPreferences.class);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getSimpleName();
    }

    @Override
    public void disposeComponent() {
    }

    @Override
    public void initComponent() {
    }

    @Override
    @org.jetbrains.annotations.Nullable
    public DemistoUserPreferences.State getState() {
        return state; //Saves all public variables to disk.
    }

    @Override
    public void loadState(@NotNull DemistoUserPreferences.State state) {
        this.state = state; //restores state from disk
    }

    public Map<String, String> getUserPreferences() {
        return state.USER_PREFERENCES;
    }

    public void setUserPreferences(Map<String, String> preferences) {
        this.state.USER_PREFERENCES = preferences;
    }

    public void setUserPreference(@NotNull String path, @NotNull String value) {
        state.USER_PREFERENCES.put(path, value);
    }

    public String getUserPreference(@NotNull String path) {
        return state.USER_PREFERENCES.get(path);
    }

    public static class State {
        /* NOTE: member should be "public" to be saved in xml */
        public Map<String, String> USER_PREFERENCES = new HashMap();
    }
}

