package com.demisto.plugin.ide.actions;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.DemistoYML;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.intellij.icons.AllIcons;
import com.intellij.ide.GeneralSettings;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.OpenProjectFileChooserDescriptor;
import com.intellij.ide.highlighter.ProjectFileType;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.PathChooserDialog;
import com.intellij.openapi.fileChooser.impl.FileChooserUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.ex.FileTypeChooser;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.impl.welcomeScreen.NewWelcomeScreen;
import com.intellij.platform.PlatformProjectOpenProcessor;
import com.intellij.projectImport.ProjectAttachProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.demisto.plugin.ide.DemistoUtils.*;

public class OpenDemistoFile extends AnAction implements DumbAware {
    private Project eventProject;
    public static final Logger LOG = Logger.getInstance(OpenDemistoFile.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.eventProject = e.getProject();
        ApplicationManager.getApplication().invokeLater(() -> prepareFileChooserAndOpen(eventProject));
    }

    private void prepareFileChooserAndOpen(final Project eventProject) {
        final boolean showFiles = eventProject != null || PlatformProjectOpenProcessor.getInstanceIfItExists() != null;
        final FileChooserDescriptor descriptor = showFiles ? new ProjectOrFileChooserDescriptor() : new ProjectOnlyFileChooserDescriptor();

        VirtualFile toSelect = null;
        if (StringUtil.isNotEmpty(GeneralSettings.getInstance().getDefaultProjectDirectory())) {
            toSelect = VfsUtil.findFileByIoFile(new File(GeneralSettings.getInstance().getDefaultProjectDirectory()), true);
        }

        descriptor.putUserData(PathChooserDialog.PREFER_LAST_OVER_EXPLICIT, toSelect == null && showFiles);

        FileChooser.chooseFiles(descriptor, eventProject, toSelect != null ? toSelect : getPathToSelect(), files -> {
            for (VirtualFile file : files) {
                if (!descriptor.isFileSelectable(file)) {
                    String message = IdeBundle.message("error.dir.contains.no.project", file.getPresentableUrl());
                    Messages.showInfoMessage(eventProject, message, IdeBundle.message("title.cannot.open.project"));
                    return;
                }
            }
            doOpenFile(eventProject, files);
        });
    }

    @Nullable
    protected VirtualFile getPathToSelect() {
        return VfsUtil.getUserHomeDir();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (NewWelcomeScreen.isNewWelcomeScreen(e)) {
            e.getPresentation().setIcon(AllIcons.Actions.Menu_open);
        }
    }

    private void doOpenFile(@Nullable Project project, @NotNull List<VirtualFile> result) {
        for (VirtualFile file : result) {
            LOG.info("In OpenDemistoFile, opening file : " + file.getPath());
            if (file.isDirectory()) {
                Project openedProject;
                if (ProjectAttachProcessor.canAttachToProject()) {
                    EnumSet<PlatformProjectOpenProcessor.Option> options = EnumSet.noneOf(PlatformProjectOpenProcessor.Option.class);
                    openedProject = PlatformProjectOpenProcessor.doOpenProject(file, project, -1, null, options);
                }
                else {
                    openedProject = ProjectUtil.openOrImport(file.getPath(), project, false);
                }
                FileChooserUtil.setLastOpenedFile(openedProject, file);
                return;
            }

            // try to open as a project - unless the file is an .ipr of the current one
            if ((project == null || !file.equals(project.getProjectFile())) && OpenProjectFileChooserDescriptor.isProjectFile(file)) {
                int answer = file.getFileType() instanceof ProjectFileType
                        ? Messages.YES
                        : Messages.showYesNoCancelDialog(project,
                        IdeBundle.message("message.open.file.is.project", file.getName()),
                        IdeBundle.message("title.open.project"),
                        IdeBundle.message("message.open.file.is.project.open.as.project"),
                        IdeBundle.message("message.open.file.is.project.open.as.file"),
                        IdeBundle.message("button.cancel"),
                        Messages.getQuestionIcon());
                if (answer == Messages.CANCEL)  return;

                if (answer == Messages.YES) {
                    Project openedProject = ProjectUtil.openOrImport(file.getPath(), project, false);
                    if (openedProject != null) {
                        FileChooserUtil.setLastOpenedFile(openedProject, file);
                    }
                    return;
                }
            }

            FileType type = FileTypeChooser.getKnownFileTypeOrAssociate(file, project);
            if (type == null) return;
            VirtualFile scriptFile = null;
            VirtualFile imageFile = null;
            VirtualFile detailedDescriptionFile = null;
            String newFilePath = "";
            VirtualFile dirPath = file.getParent();
            // demisto yml file, we can add further checks later
            if (Objects.equals(file.getExtension(), "yml")) {
                Yaml yaml = new Yaml();
                String ymlString = DemistoUtils.readFile(file.getPath());
                Map ymlMap = yaml.load(ymlString);
                String ymlType = ymlIsIntegrationOrAutomation(ymlMap);
                String script = "";
                String image = "";
                String detailedDescription = "";

                // get script from current yml
                if (ymlType.equals("py-automation")){
                    LOG.info("Opening automation file.");
                    DemistoAutomationYML automationYML = new DemistoAutomationYML(ymlMap);
                    script = ensureTextEndsInNewLine(getDemistoScriptWithImports(automationYML.getScript()));
                    String dirName = DemistoUtils.removePrefix(DemistoUtils.getFileNameWithoutExtension(file.getName()), SCRIPT_PREFIX);
                    String newDirPath = dirPath.getPath() + "/" + dirName;

                    boolean dirCreated = DemistoUtils.createDirectory(newDirPath);

                    if(!dirCreated) {
                        LOG.error("Could not create directory.");
                        return;
                    }
                    newFilePath = newDirPath + "/" + file.getName();

                    automationYML.setScript(SCRIPT_DEFAULT);
                    String newYmlString = DemistoUtils.getYMLStringFromMap(automationYML.getDemistoYMLMapWithUnsupportedFields(automationYML));

                    DemistoUtils.writeStringToFile(DemistoUtils.removePrefix(newFilePath, SCRIPT_PREFIX), newYmlString);
                    file.getPath();
                    DemistoUtils.deleteFile(file.getPath());

                    newFilePath = DemistoUtils.removePrefix(newFilePath, SCRIPT_PREFIX);

                } else if (ymlType.equals("py-integration")) {
                    LOG.info("Opening integration file.");
                    String dirName = DemistoUtils.removePrefix(DemistoUtils.getFileNameWithoutExtension(file.getName()), INTEGRATION_PREFIX);
                    String newDirPath = dirPath.getPath() + "/" + dirName;
                    boolean dirCreated = DemistoUtils.createDirectory(newDirPath);

                    if(!dirCreated) {
                        LOG.error("Could not create directory.");
                        return;
                    }
                    newFilePath = newDirPath + "/" + file.getName();

                    DemistoIntegrationYML integrationYML = new DemistoIntegrationYML(ymlMap);
                    Map integrationMap = integrationYML.getDemistoYMLMapWithUnsupportedFields(integrationYML);
                    image = (String) integrationMap.get("image");
                    detailedDescription = integrationYML.getDetaileddescription();
                    script = ensureTextEndsInNewLine(getDemistoScriptWithImports(integrationYML.getScript().getScript()));

                    integrationYML.setDetaileddescription(DETAILED_DESCRIPTION_DEFAULT);
                    integrationYML.getScript().setScript(SCRIPT_DEFAULT);
                    Map unsupportedFields = integrationYML.getUnsupportedFields();
                    if (unsupportedFields.containsKey("image")) {
                        unsupportedFields.remove("image");
                    }
                    integrationYML.setUnsupportedFields(unsupportedFields);

                    String newYmlString = DemistoUtils.getYMLStringFromMap(integrationYML.getDemistoYMLMapWithUnsupportedFields(integrationYML));

                    // Write the YML to the new directory and delete the old one
                    DemistoUtils.writeStringToFile(DemistoUtils.removePrefix(newFilePath, INTEGRATION_PREFIX), newYmlString);
                    DemistoUtils.deleteFile(file.getPath());

                    newFilePath = DemistoUtils.removePrefix(newFilePath, INTEGRATION_PREFIX);

                    LOG.info("Opening image and description files.");
                    // Split and open image and description files

                    createImageFile(newFilePath, image);

                    // Write the detailed description to a file
                    if(!DemistoUtils.stringIsNotEmptyOrNull(detailedDescription)) {
                        detailedDescription = " ";
                    }

                    String detailedDescriptionFilePath = DemistoUtils.renameFileExtension(
                            DemistoUtils.addFilePostfix(newFilePath, DESCRIPTION_POSTFIX), "md");
                    DemistoUtils.writeStringToFile(detailedDescriptionFilePath, detailedDescription);
                }

            LOG.info("Creating python file.");
            String scriptFilePath = DemistoUtils.renameFileExtension(newFilePath,"py");
            DemistoUtils.writeStringToFile(scriptFilePath, script);
            // open it as a virtual file
            scriptFile = VfsUtil.findFileByIoFile(new File(scriptFilePath), true);

            if (project != null && scriptFile != null) {
                DemistoUtils.openFile(scriptFile, project);
            }

            LOG.info("Successfully opened Demisto file.");

            }
            else {
                PlatformProjectOpenProcessor.doOpenProject(file, null, -1, null,
                        EnumSet.of(PlatformProjectOpenProcessor.Option.TEMP_PROJECT));
            }
        }
    }

    private static class ProjectOnlyFileChooserDescriptor extends OpenProjectFileChooserDescriptor {
        ProjectOnlyFileChooserDescriptor() {
            super(true);
            setTitle(IdeBundle.message("title.open.project"));
        }
    }

    // vanilla OpenProjectFileChooserDescriptor only accepts project files; this one is overridden to accept any files
    private static class ProjectOrFileChooserDescriptor extends OpenProjectFileChooserDescriptor {
        private final FileChooserDescriptor myStandardDescriptor =
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withHideIgnored(false);

        ProjectOrFileChooserDescriptor() {
            super(true);
            setTitle(IdeBundle.message("title.open.file.or.project"));
        }

        @Override
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
            return file.isDirectory() ? super.isFileVisible(file, showHiddenFiles) : myStandardDescriptor.isFileVisible(file, showHiddenFiles);
        }

        @Override
        public boolean isFileSelectable(VirtualFile file) {
            return file.isDirectory() ? super.isFileSelectable(file) : myStandardDescriptor.isFileSelectable(file);
        }

        @Override
        public boolean isChooseMultiple() {
            return true;
        }
    }
}
