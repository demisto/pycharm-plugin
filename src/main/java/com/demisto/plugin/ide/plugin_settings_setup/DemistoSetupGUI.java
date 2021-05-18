package com.demisto.plugin.ide.plugin_settings_setup;

import com.demisto.plugin.ide.DemistoRESTClient;
import com.demisto.plugin.ide.DemistoUtils;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.demisto.plugin.ide.DemistoUtils.*;

/**
 * GUI for the {@link DemistoSetupConfigurable}
 */
public class DemistoSetupGUI extends JPanel{
    private final JPanel extraSettingsPanel;
    private JBCheckBox trustAnyCertificateCheckbox;
    private JBCheckBox trackLocalChangesCheckbox;
    private JPanel buttonsPanel;
    private JButton updateMocksButton;
    private AsyncProcessIcon.Big loadingIcon;
    private JLabel loadingLabel;
    private JPanel loadingPanel;
    private JTextField demistoServerUrl;
    private JPanel apiPanel;
    private JPanel serverUrlPanel;
    private JPasswordField demistoApiKey;
    private JButton testButton;
    private DemistoSetupConfig mConfig;
    private Project project;
    private String VALIDATE_DEMISTO_CREDS = "/incidenttype";
    private final String AUTOMATION_LOAD_PATH = "/automation/load/";
    private final String COMMON_SERVER_PYTHON = "CommonServerPython.py";
    private final String COMMON_USER_PYTHON = "CommonServerUserPython.py";
    private final String DEMISTO_MOCK_NAME = "demistomock.py";
    private final String DEMISTO_MOCK_PATH = "/META-INF/" + DEMISTO_MOCK_NAME;

    public DemistoSetupGUI() {
        this.setLayout(new GridLayoutManager(5, 5, JBUI.emptyInsets(), -1, -1));
        this.setRequestFocusEnabled(true);

        serverUrlPanel = new JPanel();
        serverUrlPanel.setLayout(new GridLayoutManager(2, 2, JBUI.emptyInsets(), -1, -1));
        final JLabel label6 = new JLabel("Demisto server URL");
        serverUrlPanel.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(80, 16), null, 0, false));
        demistoServerUrl = new JTextField();
        demistoServerUrl.setAutoscrolls(true);
        demistoServerUrl.setEditable(true);
        demistoServerUrl.setEnabled(true);
        demistoServerUrl.setHorizontalAlignment(10);
        label6.setLabelFor(demistoServerUrl);
        serverUrlPanel.add(demistoServerUrl, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label7 = new JLabel(" For example : https://myserver.com");
        label7.setVerticalAlignment(0);
        serverUrlPanel.add(label7, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        apiPanel = new JPanel();
        apiPanel.setLayout(new GridLayoutManager(7, 3, JBUI.emptyInsets(), -1, -1));

        final JLabel label1 = new JLabel("Demisto API key      ");
        apiPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        demistoApiKey = new JPasswordField();
        apiPanel.add(demistoApiKey, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        final JLabel label4 = new JLabel(" To generate the Demisto API key:");
        apiPanel.add(label4, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel(" 1) In Demisto, navigate to Settings -> Integrations -> API Keys.");
        apiPanel.add(label2, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel(" 2) Click the \"Get Your Key\" button.");
        apiPanel.add(label3, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel(" 3) Type a name for the API key, and click \"Generate key\".");
        apiPanel.add(label5, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        extraSettingsPanel = new JPanel();
        extraSettingsPanel.setLayout(new GridLayoutManager(2, 1, JBUI.emptyInsets(), -1, -1));

        trustAnyCertificateCheckbox = new JBCheckBox("Trust any certificate (unsecure)");
        extraSettingsPanel.add(trustAnyCertificateCheckbox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        trackLocalChangesCheckbox = new JBCheckBox("Dev-Prod Mode: Track in version control (Dev environment only)");
        extraSettingsPanel.add(trackLocalChangesCheckbox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayoutManager(2, 3, JBUI.insetsTop(10), -1, -1));

        updateMocksButton = new JButton("Update Demisto Mocks");

        buttonsPanel.add(updateMocksButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateMocksButton.addActionListener(e -> {
            loadingIcon.resume();
            loadingPanel.setVisible(true);
            // run in background thread
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        // Start Progress
                        downloadDependenciesToRepo();
                        loadingPanel.setVisible(false);
                        loadingIcon.suspend();
                        JOptionPane.showMessageDialog(apiPanel, "Updated successfully!");
                    } catch (Exception ex) {
                        loadingPanel.setVisible(false);
                        loadingIcon.suspend();
                        ex.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    // make sure that we don't keep the "loading..." icon
                    loadingPanel.setVisible(false);
                    loadingIcon.suspend();
                }
            }.execute();
        });
        final Spacer spacer1 = new Spacer();
        buttonsPanel.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

        testButton = new JButton("Test");
        buttonsPanel.add(testButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_VERTICAL, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        testButton.addActionListener(e -> {
            loadingIcon.resume();
            loadingPanel.setVisible(true);
            // run in background thread
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        // Start Progress
                        mConfig.setServerURL(demistoServerUrl.getText());
                        mConfig.setApiKey(String.copyValueOf(demistoApiKey.getPassword()));
                        mConfig.setTrustAnyCertificate(trustAnyCertificateCheckbox.isSelected());
                        mConfig.setTrackLocalChanges(trackLocalChangesCheckbox.isSelected());
                        // test Demisto creds
                        DemistoRESTClient demistoRESTClient = new DemistoRESTClient(project);
                        LOG.debug("Validating Demisto's credentials");
                        String res = demistoRESTClient.sendGetRequest("", VALIDATE_DEMISTO_CREDS);
                        LOG.debug("Checking if running on MT");
                        if (demistoRESTClient.getIsMasterMT()) {
                            loadingPanel.setVisible(false);
                            loadingIcon.suspend();
                            JOptionPane.showMessageDialog(apiPanel, "Server is running on Multi-Tenant environment. Change Server URL to one of the tenants.");
                            LOG.error("Running on MT Env");
                        } else if (!stringIsNotEmptyOrNull(res)) {
                            loadingPanel.setVisible(false);
                            loadingIcon.suspend();
                            JOptionPane.showMessageDialog(apiPanel, "Validation was not successful, please recheck your parameters!");
                            LOG.error("Wrong credentials");
                        } else {
                            downloadDependenciesToRepo();
                            loadingPanel.setVisible(false);
                            loadingIcon.suspend();
                            JOptionPane.showMessageDialog(apiPanel, "Validated successfully!");
                            LOG.info("Creds validated");
                        }
                    } catch (Exception ex) {
                        loadingPanel.setVisible(false);
                        loadingIcon.suspend();
                        ex.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    // make sure that we don't keep the "loading..." icon
                    loadingPanel.setVisible(false);
                    loadingIcon.suspend();
                }
            }.execute();
        });
        loadingPanel = new JPanel();
        loadingLabel = new JLabel("Loading...");
        loadingIcon = new AsyncProcessIcon.Big("Loading...");
        loadingIcon.suspend();

        loadingLabel = new JLabel("Loading...");
        loadingPanel.add(loadingIcon);
        loadingPanel.add(loadingLabel);
        loadingPanel.setVisible(false);

        buttonsPanel.add(loadingPanel, new GridConstraints(1, 0, 1, 3,
                GridConstraints.ANCHOR_CENTER, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));

        this.add(serverUrlPanel, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        this.add(apiPanel, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.ALIGN_FILL, GridConstraints.ALIGN_FILL, null, null, null, 0, false));
        this.add(extraSettingsPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        this.add(buttonsPanel, new GridConstraints(3, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.ALIGN_FILL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        final Spacer spacer2 = new Spacer();
        this.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    public void downloadDependenciesToRepo() {
        DemistoRESTClient demistoRESTClient = new DemistoRESTClient(project);
        String projectRoot = ModuleRootManager.getInstance(ModuleManager.getInstance(project).getModules()[0]).getContentRoots()[0].getPath();
        try {
            // download CommonServerPython and put it in the repo's root
            JSONObject commonServerPythonYml = new JSONObject(demistoRESTClient.sendPostRequest("", AUTOMATION_LOAD_PATH + "CommonServerPython", ""));
            String commonServerPython = String.valueOf(commonServerPythonYml.get("script"));
            String commonServerPythonWithImports = DemistoUtils.getDemistoScriptWithDemistoMockImport(commonServerPython);
            Path filePath = Paths.get(projectRoot, COMMON_SERVER_PYTHON);
            DemistoUtils.writeStringToFile(filePath.toString(), commonServerPythonWithImports);
        } catch (Exception err) {
            JOptionPane.showMessageDialog(apiPanel, "Failed to download CommonServerPython from Demisto, error was: \n" + err.getMessage());
            LOG.error(err.getStackTrace());
        }
        try {
            // download CommonServerUserPython and put it in the repo's root
            JSONObject commonServerPythonYml = new JSONObject(demistoRESTClient.sendPostRequest("", AUTOMATION_LOAD_PATH + "CommonServerUserPython", ""));
            String CommonServerUserPython = String.valueOf(commonServerPythonYml.get("script"));
            Path filePath = Paths.get(projectRoot, COMMON_USER_PYTHON);
            DemistoUtils.writeStringToFile(filePath.toString(), CommonServerUserPython);
            // download CommonServerUserPython and put it in the repo's root
        } catch (Exception err) {
            JOptionPane.showMessageDialog(apiPanel, "Failed to download CommonServerPython from Demisto, error was: \n" + err.getMessage());
            LOG.error(err.getStackTrace());
        }
        try {
            // copy demistomock from plugin resources to repo's root
            copyResource(DEMISTO_MOCK_PATH, Paths.get(projectRoot, DEMISTO_MOCK_NAME).toString(), this.getClass());
        } catch (Exception err) {
            LOG.error(err.getStackTrace());
            JOptionPane.showMessageDialog(apiPanel, "Failed to copy demistomock from plugin resources to repo's root, error was: \n" + err.getMessage());
        }
    }

    public void createUI(Project project) {
        mConfig = DemistoSetupConfig.getInstance(project);
        assert mConfig != null;
        demistoServerUrl.setText(mConfig.getServerURL());
        demistoApiKey.setText(mConfig.getApiKey());
        trustAnyCertificateCheckbox.setSelected(mConfig.getTrustAnyCertificate());
        trackLocalChangesCheckbox.setSelected(mConfig.getTrackLocalChanges());
        this.project = project;
    }

    public JTextField getDemistoServerUrl() {
        return demistoServerUrl;
    }

    public void setDemistoServerUrl(String demistoServerUrl) {
        this.demistoServerUrl.setText(demistoServerUrl);
    }

    public void setDemistoApiKey(String demistoApiKey) {
        this.demistoApiKey.setText(demistoApiKey);
    }

    public boolean isModified() {
        boolean modified = false;
        modified |= !demistoServerUrl.getText().equals(mConfig.getServerURL());
        modified |= !String.copyValueOf(demistoApiKey.getPassword()).equals(mConfig.getApiKey());
        modified |= !trustAnyCertificateCheckbox.isSelected() == mConfig.getTrustAnyCertificate();
        modified |= !trackLocalChangesCheckbox.isSelected() == mConfig.getTrackLocalChanges();
        return modified;
    }

    public void apply() {
        mConfig.setServerURL(demistoServerUrl.getText());
        mConfig.setApiKey(String.copyValueOf(demistoApiKey.getPassword()));
        mConfig.setTrustAnyCertificate(trustAnyCertificateCheckbox.isSelected());
        mConfig.setTrackLocalChanges(trackLocalChangesCheckbox.isSelected());

        loadingIcon.resume();
        loadingPanel.setVisible(true);
        // run in background thread
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Start Progress
                    downloadDependenciesToRepo();
                    loadingPanel.setVisible(false);
                    loadingIcon.suspend();
                    JOptionPane.showMessageDialog(apiPanel, "Updated successfully!");
                } catch (Exception ex) {
                    loadingPanel.setVisible(false);
                    loadingIcon.suspend();
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                // make sure that we don't keep the "loading..." icon
                loadingPanel.setVisible(false);
                loadingIcon.suspend();
            }
        }.execute();
    }

    public void reset() {
        demistoServerUrl.setText(mConfig.getServerURL());
        demistoApiKey.setText(mConfig.getApiKey());
        trustAnyCertificateCheckbox.setSelected(mConfig.getTrustAnyCertificate());
        trackLocalChangesCheckbox.setSelected(mConfig.getTrackLocalChanges());
    }
}
