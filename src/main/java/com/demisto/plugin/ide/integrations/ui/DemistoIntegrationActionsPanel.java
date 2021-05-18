package com.demisto.plugin.ide.integrations.ui;

import com.demisto.plugin.ide.DemistoRESTClient;
import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.DemistoYML;
import com.demisto.plugin.ide.actions.DemistoAutomationRunner;
import com.demisto.plugin.ide.actions.ExportToDemisto;
import com.demisto.plugin.ide.generalUIComponents.DemistoResultPanel;
import com.demisto.plugin.ide.generalUIComponents.MessagePanel;
import com.demisto.plugin.ide.generalUIComponents.ResultLabel;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import org.json.JSONArray;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.demisto.plugin.ide.DemistoUtils.*;
import static com.demisto.plugin.ide.actions.ExportToDemisto.INTEGRATION_UPLOAD_PATH;
import static com.demisto.plugin.ide.integrations.ui.IntegrationParameterPanel.DEFAULT_TEXT_BORDER;

/**
 * @author Shachar Hirshberg
 * @since January 2, 2019
 */
public class DemistoIntegrationActionsPanel extends JPanel {
    private JPanel commandDropdownPanel;
    private JPanel argumentsPanel;
    private JPanel wrapPanel;
    private ComboBox commandDropdown;
    private JLabel commandLabel;
    private String selectedCommand;
    private DemistoAutomationRunner demistoAutomationRunner;
    private DemistoIntegrationYML integrationYML;
    private Project project;
    private Collection<JTextArea> textFields = new ArrayList<>();
    public static final Logger LOG = Logger.getInstance(DemistoIntegrationActionsPanel.class);
    private JButton runInDemistoButton;
    private String ymlFilePath;
    public static final float TITLE_FONT_SIZE = 14f;
    private final String DEMISTO_RESULTS_TOOLBAR_ICON = "/icons/demisto_icon.png";
    public static String RUN_IN_DEMISTO_ICON = "/icons/run-demisto.svg";
    private JLabel loadingLabel;
    private JPanel loadingPanel;
    private AsyncProcessIcon.Big loadingIcon;

    public DemistoIntegrationActionsPanel(DemistoIntegrationYML integrationYML, Project project, String ymlFilePath) {
        this.integrationYML = integrationYML;
        this.project = project;
        this.ymlFilePath = ymlFilePath;
        this.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));

        wrapPanel = new JPanel();
        wrapPanel.setLayout(new GridLayoutManager(2, 1, JBUI.insets(5, 0), -1, -1));

        commandDropdownPanel = createCommandDropdownPanel();
        wrapPanel.add(commandDropdownPanel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.FILL_HORIZONTAL,
                null, null, null));

        this.add(wrapPanel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

    }

    public JPanel createCommandDropdownPanel() {
        commandDropdownPanel = new JPanel();
        commandDropdownPanel.setLayout(new GridLayoutManager(3, 1, JBUI.emptyInsets(), -1, -1));

        ArrayList<DemistoIntegrationYML.DemistoCommand> commandsOptions = integrationYML.getScript().getCommands();
        ArrayList<String> commandOptionsNames = new ArrayList<>();
        commandsOptions.forEach(command -> commandOptionsNames.add(command.getName()));
        commandOptionsNames.add(0, "");
        //---- Command dropdown----
        commandLabel = new JLabel();
        commandLabel.setLabelFor(commandDropdown);
        commandLabel.setText("Choose the command you want to run:");
        commandOptionsNames.sort(String::compareToIgnoreCase);
        commandDropdown = new ComboBox(commandOptionsNames.toArray());
        commandDropdown.setName("commandDropdown");
        commandDropdown.setBorder(LineBorder.createGrayLineBorder());

        commandDropdownPanel.add(commandLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        commandDropdownPanel.add(commandDropdown, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        commandDropdown.addActionListener(e -> {
            ComboBox comboBox = (ComboBox) e.getSource();
            selectedCommand = (String) comboBox.getSelectedItem();
            if (argumentsPanel != null) {
                wrapPanel.remove(argumentsPanel);
            }
            createAndAddCommandArgumentsPanel(selectedCommand);
        });
        return commandDropdownPanel;
    }

    public void createAndAddCommandArgumentsPanel(String selectedCommand) {
        int commandIndex = integrationYML.getScript().findCommandInArray(selectedCommand);
        if (commandIndex >= 0) {
            DemistoIntegrationYML.DemistoCommand currentCommand = integrationYML.getScript().getCommands().get(commandIndex);
            int argumentsRows = currentCommand.getArguments().size();
            int panelRows;
            if (argumentsRows > 0) {
                panelRows = argumentsRows;
            } else {
                panelRows = 1;
            }
            textFields = new ArrayList<>();
            argumentsPanel = new JPanel(new GridLayoutManager(panelRows + 3, 2, JBUI.insets(0, 5), -1, -1));
            TitledBorder titledBorder = BorderFactory.createTitledBorder("Arguments");
            titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(TITLE_FONT_SIZE));
            argumentsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 2, 5, 2)));
            Map<String, String> cache = getUserPreferencesCache();

            for (int i = 0; i < argumentsRows; i++) {
                {
                    DemistoIntegrationYML.DemistoArgument arg = currentCommand.getArguments().get(i);

                    JLabel argLabel = new JLabel();
                    if (arg.getRequired()) {
                        argLabel.setText(arg.getName() + " *");
                    } else {
                        argLabel.setText(arg.getName());
                    }
                    argLabel.setName(arg.getName() + "Label");

                    JTextArea argTextField = new JTextArea("");
                    argTextField.setLineWrap(true);
                    argTextField.setWrapStyleWord(true);
                    argTextField.setBorder(DEFAULT_TEXT_BORDER);
                    argTextField.setFont(argLabel.getFont());
                    argTextField.setName(arg.getName() + "TextField");
                    String argUserPreferenceKey = createUserPreferenceKey(integrationYML.getName(), currentCommand.getName(), arg.getName());
                    argTextField.setText(safeGetKeyFromHashmap(cache, argUserPreferenceKey));
                    DeferredDocumentListener listener = new DeferredDocumentListener(TEXT_DEBOUNCE, e -> {
                        setSingleUserPreference(argUserPreferenceKey, argTextField.getText().trim());
                    }, true);
                    argTextField.getDocument().addDocumentListener(listener);
                    argTextField.addFocusListener(new FocusListener() {
                        @Override
                        public void focusGained(FocusEvent e) {
                            listener.start();
                        }

                        @Override
                        public void focusLost(FocusEvent e) {
                            listener.stop();
                        }
                    });

                    argLabel.setLabelFor(argTextField);
                    textFields.add(argTextField);

                    argumentsPanel.add(argLabel, new GridConstraints(i, 0, 1, 1,
                            GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                            null, null, null));
                    argumentsPanel.add(argTextField, new GridConstraints(i, 1, 1, 1,
                            GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                            null, null, null));
                }
            }

            // ---- Using instance label ----

            JLabel instanceLabel = new JLabel("instance name (using)");
            instanceLabel.setName("instanceLabel");
            String instanceUserPreferenceKey = createUserPreferenceKey(integrationYML.getName(), currentCommand.getName(), "instanceName");
            JTextArea instanceTextArea = new JTextArea("");
            instanceTextArea.setLineWrap(true);
            instanceTextArea.setWrapStyleWord(true);
            instanceTextArea.setBorder(DEFAULT_TEXT_BORDER);
            instanceTextArea.setFont(instanceLabel.getFont());
            instanceTextArea.setName("usingTextField");
            instanceTextArea.setText(safeGetKeyFromHashmap(cache, instanceUserPreferenceKey));
            DeferredDocumentListener listener = new DeferredDocumentListener(TEXT_DEBOUNCE, e -> {
                setSingleUserPreference(instanceUserPreferenceKey, instanceTextArea.getText().trim());
            }, true);
            instanceTextArea.getDocument().addDocumentListener(listener);
            instanceTextArea.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    listener.start();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    listener.stop();
                }
            });
            instanceLabel.setLabelFor(instanceTextArea);
            textFields.add(instanceTextArea);

            argumentsPanel.add(instanceLabel, new GridConstraints(currentCommand.getArguments().size(), 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            argumentsPanel.add(instanceTextArea, new GridConstraints(currentCommand.getArguments().size(), 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

            // ---- Add Run in Demisto button ----

            runInDemistoButton = new JButton(" Export Integration & Run Command in Demisto", IconLoader.getIcon(RUN_IN_DEMISTO_ICON));
            runInDemistoButton.setName("runInDemistoButton");
            runInDemistoButton.addActionListener(e ->
            {
                loadingIcon.resume();
                loadingPanel.setVisible(true);
                // update Integration's yml with current Python script
                Boolean exportRes = ExportToDemisto.updateCurrentYmlScript(project);
                // running the export & run in a background thread
                if (exportRes) {
                    new SwingWorker<Content, Void>() {
                        @Override
                        protected Content doInBackground() throws Exception {
                            try {
                                // create query to run in Demisto
                                String query = createIntegrationRunQuery(false);
                                demistoAutomationRunner = new DemistoAutomationRunner(project);
                                String runResult = demistoAutomationRunner.runAutomationInDemisto(query);
                                // Finished
                                return createDemistoResultContent(runResult, createIntegrationRunQuery(true));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return createDemistoResultContent("", "");
                            }
                        }

                        @Override
                        protected void done() {
                            Content backgroundTaskResult;
                            try {
                                backgroundTaskResult = get();
                                // remove old results toolwindow
                                ToolWindowManager.getInstance(project).unregisterToolWindow("Demisto Results");
                                // add new results toolwindow
                                ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("Demisto Results", true, ToolWindowAnchor.BOTTOM);
                                toolWindow.setIcon(IconLoader.getIcon(DEMISTO_RESULTS_TOOLBAR_ICON));
                                loadingPanel.setVisible(false);
                                loadingIcon.suspend();
                                toolWindow.getContentManager().addContent(backgroundTaskResult);
                                toolWindow.activate(() -> {
                                }, true);
                            } catch (Exception ex) {
                                loadingLabel.setVisible(false);
                                loadingIcon.suspend();
                                ex.printStackTrace();
                            }
                        }
                    }.execute();
                } else {
                    loadingPanel.setVisible(false);
                    loadingIcon.suspend();
                }
            });
            argumentsPanel.add(runInDemistoButton, new GridConstraints(currentCommand.getArguments().size() + 1, 0, 1, 2,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

            loadingPanel = new JPanel();
            loadingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            loadingLabel = new JLabel("Loading...");
            loadingIcon = new AsyncProcessIcon.Big("Loading...");
            loadingIcon.suspend();

            loadingLabel = new JLabel("Loading...");
            loadingPanel.add(loadingIcon);
            loadingPanel.add(loadingLabel);
            loadingPanel.setVisible(false);
            argumentsPanel.add(loadingPanel, new GridConstraints(currentCommand.getArguments().size() + 2, 0, 1, 2,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            wrapPanel.add(argumentsPanel, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.FILL_HORIZONTAL,
                    null, null, null));
            wrapPanel.revalidate();
        }
    }


    public Content createDemistoResultContent(String runResult, String query) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        LOG.info("Creating Demisto Results Tool window");

        // initialize new result panel
        DemistoResultsPanel panel = new DemistoResultsPanel(false, false);

        GridBagConstraints generalPanelCons = new GridBagConstraints();
        generalPanelCons.fill = GridBagConstraints.HORIZONTAL;
        generalPanelCons.gridwidth = 1;
        generalPanelCons.anchor = GridBagConstraints.CENTER;
        generalPanelCons.weightx = 1;
        generalPanelCons.weighty = 1;
        generalPanelCons.insets = new Insets(5, 10, 5, 5);
        GridBagLayout settingsPanelLayout = new GridBagLayout();
        panel.setLayout(settingsPanelLayout);

        // add query as title of the result panel
        if (stringIsNotEmptyOrNull(query)) {
            JLabel resultsLabel = new JLabel();
            String resultsTitle = "Results from Demisto";
            resultsLabel.setText(resultsTitle);
            resultsLabel.setFont(resultsLabel.getFont().deriveFont(20.0f));
            generalPanelCons.gridy = 0;
            settingsPanelLayout.setConstraints(resultsLabel, generalPanelCons);
            panel.add(resultsLabel);

            JPanel wrapPanel = new JPanel();
            wrapPanel.setLayout(new GridLayoutManager(1, 2, JBUI.emptyInsets(), -1, -1));
            JLabel queryLabel = DemistoUtils.underlineJLabel("Query:");
            queryLabel.setFont(queryLabel.getFont().deriveFont(18.0f));

            wrapPanel.add(queryLabel, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    null, null, null));
            ResultLabel queryText = new ResultLabel();
            queryText.setText(query);
            queryText.setFont(queryText.getFont().deriveFont(16.0f));
            wrapPanel.add(queryText, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_WANT_GROW,
                    null, null, null));
            generalPanelCons.gridy = 1;
            settingsPanelLayout.setConstraints(wrapPanel, generalPanelCons);
            panel.add(wrapPanel);
        }

        // add results from Demisto to the result panel
        if (stringIsNotEmptyOrNull(runResult.trim())) {
            JSONArray resultsArray = new JSONArray(runResult);
            for (int i = 0; i < resultsArray.length(); i++) {
                generalPanelCons.gridy = i + 2;
                JPanel currentPanel = new DemistoResultPanel(resultsArray.getJSONObject(i));
                settingsPanelLayout.setConstraints(currentPanel, generalPanelCons);
                panel.add(currentPanel);
            }
        } else {
            // no results from Demisto
            JPanel noResultsPanel = new MessagePanel("No results were returned from Demisto, please check your command and that your instance is configured correctly");
            generalPanelCons.gridy = 2;
            settingsPanelLayout.setConstraints(noResultsPanel, generalPanelCons);
            panel.add(noResultsPanel);
        }
        JBScrollPane scrollPanel = new JBScrollPane(panel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // create content for the tool window
        return contentFactory.createContent(scrollPanel, "", false);
    }


    public String createIntegrationRunQuery(Boolean censorSensitive) {
        // prepare map with fields to to censor
        String censoredValue = "*****";
        ArrayList<DemistoIntegrationYML.DemistoCommand> commands = integrationYML.getScript().getCommands();
        HashMap<String, Boolean> argumentsMap = new HashMap<>();
        for (DemistoIntegrationYML.DemistoCommand command : commands) {
            if (command.getName().equals(this.selectedCommand)) {
                for (DemistoYML.DemistoArgument arg : command.getArguments()) {
                    argumentsMap.put(arg.getName(), arg.getSecret());
                }
            }
        }

        // build query
        StringBuilder query = new StringBuilder("!");
        query.append(this.selectedCommand);
        for (JTextArea textField : textFields) {
            if (stringIsNotEmptyOrNull(textField.getText())) {
                // each textfield ends in "TextField"
                query.append(" ");
                String argumentName = textField.getName().substring(0, textField.getName().length() - 9);
                query.append(argumentName);
                query.append("=");
                String argumentValue = (censorSensitive && argumentsMap.containsKey(argumentName) && argumentsMap.get(argumentName))
                        ? censoredValue
                        : textField.getText();
                if (argumentValue.contains("\"")) {
                    query.append("`");
                    query.append(argumentValue);
                    query.append("`");
                } else {
                    query.append("\"");
                    query.append(argumentValue);
                    query.append("\"");
                }
            }
        }
        return query.toString();
    }

    class DemistoResultsPanel extends SimpleToolWindowPanel {
        public DemistoResultsPanel(boolean vertical, boolean borderless) {
            super(vertical, borderless);
        }
    }

}
