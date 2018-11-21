package com.mighty16.json;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.util.ui.UIUtil;
import com.mighty16.json.annotations.GsonAnnotations;
import com.mighty16.json.core.AnnotationGenerator;
import com.mighty16.json.core.FileSaver;
import com.mighty16.json.core.LanguageResolver;
import com.mighty16.json.core.models.ClassModel;
import com.mighty16.json.generator.DomainDataModelGenerator;
import com.mighty16.json.resolver.KotlinDataClassResolver;
import com.mighty16.json.resolver.KotlinFileType;
import com.mighty16.json.ui.JSONEditDialog;
import com.mighty16.json.ui.ModelTableDialog;
import com.mighty16.json.ui.NotificationsHelper;
import com.mighty16.json.ui.TextResources;

import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public class ClassFromJSONAction extends AnAction implements JSONEditDialog.JSONEditCallbacks, ModelTableDialog.ModelTableCallbacks {

    private PsiDirectory directory;
    private Point lastDialogLocation;
    private LanguageResolver languageResolver;
    private TextResources textResources;
    private String rootClassName;

    public ClassFromJSONAction() {
        super();
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        languageResolver = new KotlinDataClassResolver();
        textResources = new TextResources();

        Project project = event.getProject();
        if (project == null) return;
        DataContext dataContext = event.getDataContext();
        final Module module = DataKeys.MODULE.getData(dataContext);
        if (module == null) return;
        final Navigatable navigatable = DataKeys.NAVIGATABLE.getData(dataContext);

        if (navigatable != null) {
            if (navigatable instanceof PsiDirectory) {
                directory = (PsiDirectory) navigatable;
            }
        }

        if (directory == null) {
            ModuleRootManager root = ModuleRootManager.getInstance(module);
            for (VirtualFile file : root.getSourceRoots()) {
                directory = PsiManager.getInstance(project).findDirectory(file);
            }
        }

        JSONEditDialog dialog = new JSONEditDialog(this, textResources);
        dialog.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                lastDialogLocation = dialog.getLocation();
            }
        });
        dialog.setSize(640, 480);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    @Override
    public void onJsonParsed(List<ClassModel> classDataList, List<ClassModel> flatParsedClasses, String rootClassName) {
        this.rootClassName = rootClassName;
        ModelTableDialog tableDialog = new ModelTableDialog(classDataList, flatParsedClasses, languageResolver, textResources, this);
        if (lastDialogLocation != null) {
            tableDialog.setLocation(lastDialogLocation);
        }
        tableDialog.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                lastDialogLocation = tableDialog.getLocation();
            }
        });

        tableDialog.pack();
        tableDialog.setVisible(true);
    }

    @Override
    public void onModelsReady(List<ClassModel> data, List<ClassModel> flatData, String singleFileName, int annotationsType) {
        AnnotationGenerator annotations = new GsonAnnotations();
        Project project = directory.getProject();
        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(directory.getProject());
        String packageName = directoryFactory.getQualifiedName(directory, true);

        FileSaver fileSaver = new IDEFileSaver(factory, directory, KotlinFileType.INSTANCE);

        fileSaver.setListener(fileName -> {
            int ok = Messages.showOkCancelDialog(
                    textResources.getReplaceDialogMessage(fileName),
                    textResources.getReplaceDialogTitle(),
                    UIUtil.getQuestionIcon());
            return ok == 0;
        });

//        SourceFilesGenerator dataGenerator = new DataModelGenerator(rootClassName + DATA_MODEL_POSTFIX, languageResolver, annotations, fileSaver);
//        SourceFilesGenerator domainGenerator = new DomainModelGenerator(rootClassName, languageResolver, null, fileSaver);

        DomainDataModelGenerator generator = new DomainDataModelGenerator(rootClassName, languageResolver, annotations, fileSaver);
        generator.setListener(filesCount ->
                NotificationsHelper.showNotification(directory.getProject(),
                        textResources.getGeneratedFilesMessage(filesCount))
        );

        generator.generateFiles(packageName, data, flatData);
    }
}
