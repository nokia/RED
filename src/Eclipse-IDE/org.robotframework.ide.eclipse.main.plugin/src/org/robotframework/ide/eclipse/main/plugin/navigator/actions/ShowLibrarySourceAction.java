/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
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
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
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
        final IProject project = (IProject) selection.getPaths()[0].getFirstSegment();

        openLibrarySource(page, project, spec);
    }

    public static void openLibrarySource(final IWorkbenchPage page, final IProject project,
            final LibrarySpecification spec) {
        try {
            final String libName = spec.getName() + ".py";
            final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
            final IFile file = LibspecsFolder.get(project).getFile(libName);
            
            final IPath location = extractLibraryLocation(robotProject, spec);
            if(location == null) {
                throw new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "Empty location path!"));
            }
            
            file.createLink(location, IResource.REPLACE | IResource.HIDDEN, null);

            IEditorDescriptor desc = IDE.getEditorDescriptor(file);
            if (!desc.isInternal()) {
                // we don't want to open .py file with interpreter, so if there
                // is no internal editor, then we will use default text editor
                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                desc = editorRegistry.findEditor("org.eclipse.ui.DefaultTextEditor");
                if (desc == null) {
                    throw new SourceOpeningException(
                            "Unable to open editor for library: " + spec.getName()
                            + ". No suitable editor.");
                }
            }
            page.openEditor(new FileEditorInput(file), desc.getId());
        } catch (final CoreException e) {
            throw new SourceOpeningException("Unable to open editor for library: " + spec.getName(), e);
        }
    }
    
    private static IPath extractLibraryLocation(final RobotProject robotProject, final LibrarySpecification spec) {
        if (robotProject.isStandardLibrary(spec)) {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            final File standardLibraryPath = runtimeEnvironment.getStandardLibraryPath(spec.getName());
            return standardLibraryPath == null ? null : new Path(standardLibraryPath.getAbsolutePath());
        } else if (robotProject.isReferencedLibrary(spec)) {
            final IPath pythonLibPath = new Path(robotProject.getPythonLibraryPath(spec.getName()));
            if (pythonLibPath.toFile().exists()) {
                return pythonLibPath;
            } else if (spec.getName().contains(".")) {
                final IPath path = tryToFindLibWithoutQualifiedPart(pythonLibPath);
                if (path != null) {
                    return path;
                }
            }
            return findModuleLibrary(pythonLibPath, spec.getName());
        }

        return null;
    }
    
    private static IPath tryToFindLibWithoutQualifiedPart(final IPath pythonLibPath) {
        final String fileExt = pythonLibPath.getFileExtension();
        final String lastSegment = pythonLibPath.removeFileExtension().lastSegment();
        final String withoutDot = lastSegment.substring(0, lastSegment.lastIndexOf('.'));

        final IPath resultPath = pythonLibPath.removeLastSegments(1).append(withoutDot).addFileExtension(fileExt);
        return resultPath.toFile().exists() ? resultPath : null;
    }

    private static IPath findModuleLibrary(final IPath pythonLibPath, final String libName) {
        final IPath pathToInitFile = pythonLibPath.removeLastSegments(1).append(libName).append("__init__.py");
        return pathToInitFile.toFile().exists() ? pathToInitFile : null;
    }

    private static class SourceOpeningException extends RuntimeException {

        public SourceOpeningException(final String message) {
            super(message);
        }

        public SourceOpeningException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
