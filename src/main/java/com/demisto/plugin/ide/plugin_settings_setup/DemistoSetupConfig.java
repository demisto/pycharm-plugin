package com.demisto.plugin.ide.plugin_settings_setup;

import com.demisto.plugin.ide.DemistoUtils;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * PersistentStateComponent keeps project config values.
 */
@State(
        name="DemistoSetupConfig",
        storages = {
                @Storage(
                        value = "DemistoSetupConfig.xml",
                        roamingType = RoamingType.DISABLED
                        /* Important !! this ensures that the data won't be shared with the data repository plugin,
                           keeping the API key safe. DO NOT REMOVE */
                )}
)
public class DemistoSetupConfig implements PersistentStateComponent<DemistoSetupConfig> {

    /* NOTE: member should be "public" to be saved in xml */
    public static final String DEFAULT_SERVER_NAME = "";
    public String serverURL = DEFAULT_SERVER_NAME;  // persistent member should be public
    /* set_target_properties(): runtime output directory */
    private String apiKey = "";   // set empty string as default, persistent member should be public
    public Boolean trustAnyCertificate = Boolean.FALSE;
    public Boolean trackLocalChanges = Boolean.FALSE;
    private static final String DEMISTO_SETTINGS_PASSWORD_KEY = "DEMISTO_SETTINGS_PASSWORD_KEY";
    private static final String DEMISTO_SETTINGS_USERNAME = "DEMISTO_SETTINGS_USERNAME";

    public DemistoSetupConfig() { }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL.trim().replaceAll("/$", "");
    }

    public String getApiKey() {
        return getPassword();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey.trim();
        setPassword(this.apiKey);
    }

    public Boolean getTrustAnyCertificate(){ return trustAnyCertificate; }

    public void setTrackLocalChanges(Boolean trackLocalChanges) {
        this.trackLocalChanges = trackLocalChanges;
    }

    public Boolean getTrackLocalChanges(){ return trackLocalChanges; }

    public void setTrustAnyCertificate(Boolean trustAnyCertificate) {
        this.trustAnyCertificate = trustAnyCertificate;
    }

    @Nullable
    @Override
    public DemistoSetupConfig getState() {
        return this;
    }

    @Override
    public void loadState(DemistoSetupConfig demistoSetupConfig) {
        XmlSerializerUtil.copyBean(demistoSetupConfig, this);
    }

    @Nullable
    public static DemistoSetupConfig getInstance(Project project) {
        DemistoSetupConfig config = ServiceManager.getService(project, DemistoSetupConfig.class);
        return config;
    }

    public String getPassword() {
        String password = "";
        CredentialAttributes attributes = new CredentialAttributes(DEMISTO_SETTINGS_PASSWORD_KEY, DEMISTO_SETTINGS_USERNAME, this.getClass(), false);
        String savedPassword = PasswordSafe.getInstance().getPassword(attributes);
        if (DemistoUtils.stringIsNotEmptyOrNull(savedPassword)){
            return savedPassword;
        } else {
            return password;
        }
    }

    /* we use IntelliJ's password manager to save the api key */
    public void setPassword(final String password) {
        CredentialAttributes attributes = new CredentialAttributes(DEMISTO_SETTINGS_PASSWORD_KEY, DEMISTO_SETTINGS_USERNAME, this.getClass(), false);
        Credentials saveCredentials = new Credentials(attributes.getUserName(), password != null ? password : "");
        PasswordSafe.getInstance().set(attributes, saveCredentials);
    }

}

