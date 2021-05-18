package com.demisto.plugin.ide.actions;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.ex.FileTypeChooser;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class NewDemistoAutomationFile extends CreateElementActionBase implements DumbAware {
    public static final Logger LOG = Logger.getInstance(NewDemistoAutomationFile.class);

    @Override
    public boolean isDumbAware() {
        return NewDemistoAutomationFile.class.equals(getClass());
    }

    @Override
    @NotNull
    protected PsiElement[] invokeDialog(final Project project, PsiDirectory directory) {
    MyInputValidator validator = new NewDemistoAutomationFile.MyValidator(project, directory);
        Messages.showInputDialog(project, IdeBundle.message("prompt.enter.new.file.name"),
                IdeBundle.message("title.new.file"), null, null, validator);
        return validator.getCreatedElements();
    }

    @Override
    @NotNull
    protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
        PsiDirectory parentDirectory = directory.getParentDirectory();
        if (parentDirectory != null &&
                parentDirectory.getName().equals("Integrations")) {
            directory = directory.getParentDirectory();

        }

        CreateFileAction.MkDirs mkdirs = new CreateFileAction.MkDirs(newName, directory);
        // create new directory

        LOG.info("In NewDemistoAutomationFile, creating directory named " + newName);
        PsiDirectory dir = mkdirs.directory.createSubdirectory(FileUtilRt.getNameWithoutExtension(newName));

        // create new file
        LOG.info("In NewDemistoAutomationFile, creating file named " + newName);
        PsiElement[] file = new PsiElement[]{WriteAction.compute(() -> dir.createFile(getFileName(mkdirs.newName)))};
        VirtualFile f = dir.getVirtualFile().findChild(newName);
        assert f != null;
        // create new demisto yml and write it to filesystem
        DemistoAutomationYML demistoAutomationYML = new DemistoAutomationYML(new HashMap());
        Map demistoMap = demistoAutomationYML.getDemistoYMLMapWithUnsupportedFields(demistoAutomationYML);
        String ymlFilePath = DemistoUtils.renameFileExtension(f.getPath(), "yml");
        demistoAutomationYML.setName(FilenameUtils.removeExtension(f.getName()));

        // write demisto imports to the python file
        DemistoUtils.writeStringToFile(f.getPath(),DemistoUtils.getDemistoScriptWithImports(""));

        demistoAutomationYML.setScript(DemistoUtils.SCRIPT_DEFAULT);
        String demistoStringYML = DemistoUtils.getYMLStringFromMap(demistoAutomationYML.getDemistoYMLMapWithUnsupportedFields(demistoAutomationYML));
        DemistoUtils.writeStringToFile(ymlFilePath, demistoStringYML);

        LOG.info("Created Python and YML for " + newName);
        return file;
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName) {
        return IdeBundle.message("progress.creating.file", directory.getVirtualFile().getPresentableUrl(), File.separator, newName);
    }

    @Override
    protected String getErrorTitle() {
        return IdeBundle.message("title.cannot.create.file");
    }

    @Override
    protected String getCommandName() {
        return IdeBundle.message("command.create.file");
    }

    protected String getFileName(String newName) {
        if (getDefaultExtension() == null || FileUtilRt.getExtension(newName).length() > 0) {
            return newName;
        }
        return newName + "." + getDefaultExtension();
    }

    @Nullable
    protected String getDefaultExtension() {
        // we want to create the Python part
        return "py";
    }

    protected class MyValidator extends MyInputValidator implements InputValidatorEx {
        private String myErrorText;

        public MyValidator(Project project, PsiDirectory directory){
            super(project, directory);
        }

        @Override
        public boolean checkInput(String inputString) {
            final StringTokenizer tokenizer = new StringTokenizer(inputString, "\\/");
            VirtualFile vFile = getDirectory().getVirtualFile();
            boolean firstToken = true;
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken();
                if ((token.equals(".") || token.equals("..")) && !tokenizer.hasMoreTokens()) {
                    myErrorText = "Can't create file with name '" + token + "'";
                    return false;
                }
                if (vFile != null) {
                    if (firstToken && "~".equals(token)) {
                        final VirtualFile userHomeDir = VfsUtil.getUserHomeDir();
                        if (userHomeDir == null) {
                            myErrorText = "User home directory not found";
                            return false;
                        }
                        vFile = userHomeDir;
                    }
                    else if ("..".equals(token)) {
                        vFile = vFile.getParent();
                        if (vFile == null) {
                            myErrorText = "Not a valid directory";
                            return false;
                        }
                    }
                    else if (!".".equals(token)){
                        final VirtualFile child = vFile.findChild(token);
                        if (child != null) {
                            if (!child.isDirectory()) {
                                myErrorText = "A file with name '" + token + "' already exists";
                                return false;
                            }
                            else if (!tokenizer.hasMoreTokens()) {
                                myErrorText = "A directory with name '" + token + "' already exists";
                                return false;
                            }
                        }
                        vFile = child;
                    }
                }
                if (FileTypeManager.getInstance().isFileIgnored(getFileName(token))) {
                    myErrorText = "'" + token + "' is an ignored name (Settings | Editor | File Types | Ignore files and folders)";
                    return true;
                }
                firstToken = false;
            }
            myErrorText = null;
            return true;
        }

        @Override
        public String getErrorText(String inputString) {
            return myErrorText;
        }

        @Override
        public PsiElement[] create(String newName) throws Exception {
            return super.create(newName);
        }

        @Override
        public boolean canClose(final String inputString) {
            if (inputString.length() == 0) {
                return super.canClose(inputString);
            }

            final PsiDirectory psiDirectory = getDirectory();

            final Project project = psiDirectory.getProject();
            final boolean[] result = {false};
            FileTypeChooser.getKnownFileTypeOrAssociate(psiDirectory.getVirtualFile(), getFileName(inputString), project);
            result[0] = super.canClose(getFileName(inputString));
            return result[0];
        }
    }
}
