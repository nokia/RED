package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.viewers.Selections;

public class ShowLibrarySourceAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public ShowLibrarySourceAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Show library source");

        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof LibrarySpecification);
    }

    @Override
    public void run() {
        final ITreeSelection selection = (ITreeSelection) selectionProvider.getSelection();
        final LibrarySpecification spec = Selections.getSingleElement(
                selection, LibrarySpecification.class);
        try {
            final String libName = spec.getName() + ".py";
            final IProject project = (IProject) selection.getPaths()[0].getFirstSegment();
            final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
            final IFile file = LibspecsFolder.get(project).getFile(libName);
            
            IPath location = extractLibraryLocation(robotProject, spec);
            if(location == null) {
                throw new CoreException(Status.CANCEL_STATUS);
            }
            
            file.createLink(location, IResource.REPLACE | IResource.HIDDEN, null);

            IEditorDescriptor desc = IDE.getEditorDescriptor(file);
            if (!desc.isInternal()) {
                // we don't want to open .py file with interpreter, so if there
                // is no internal editor, then we will use default text editor
                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                desc = editorRegistry.findEditor("org.eclipse.ui.DefaultTextEditor");
                if (desc == null) {
                    throw new RuntimeException("Unable to open editor for library: " + spec.getName()
                            + ". No suitable editor.");
                }
            }
            page.openEditor(new FileEditorInput(file), desc.getId());
        } catch (final CoreException e) {
            throw new RuntimeException("Unable to open editor for library: " + spec.getName(), e);
        }
    }
    
    private Path extractLibraryLocation(RobotProject robotProject, LibrarySpecification spec) {
        if (robotProject.isStandardLibrary(spec)) {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            final File standardLibraryPath = runtimeEnvironment.getStandardLibraryPath(spec.getName());
            return standardLibraryPath == null ? null : new Path(standardLibraryPath.getAbsolutePath());
        } else if (robotProject.isReferencedLibrary(spec)) {
            String pythonLibPath = robotProject.getPythonLibraryPath(spec.getName());
            if (new File(pythonLibPath).exists()) {
                return new Path(pythonLibPath);
            }
        }

        return null;
    }
}
