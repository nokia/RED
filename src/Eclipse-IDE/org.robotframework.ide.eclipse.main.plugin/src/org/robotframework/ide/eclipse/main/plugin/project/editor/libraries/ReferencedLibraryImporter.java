/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder.JarClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder.PythonClass;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 */
public class ReferencedLibraryImporter {

    public Collection<ReferencedLibrary> importPythonLib(final Shell shellForDialogs,
            final RobotRuntimeEnvironment environment, final String fullLibraryPath) {
        final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(environment);
        final List<PythonClass> pythonClasses = newArrayList();

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Reading classes/modules from module '" + fullLibraryPath + "'", 100);
                    try {
                        pythonClasses.addAll(pythonLibStructureBuilder.provideEntriesFromFile(fullLibraryPath));
                    } catch (final RobotEnvironmentException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            StatusManager.getManager()
                    .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "RED was unable to find classes/modules inside '" + fullLibraryPath + "' module",
                            e.getCause()), StatusManager.SHOW);
            return new ArrayList<>();
        }

        if (pythonClasses.isEmpty()) {
            StatusManager.getManager()
                    .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "RED was unable to find classes/modules inside '" + fullLibraryPath + "' module"),
                            StatusManager.SHOW);
            return new ArrayList<>();
        } else if (pythonClasses.size() == 1) {
            return newArrayList(pythonClasses.get(0).toReferencedLibrary(fullLibraryPath));
        } else {
            final ElementListSelectionDialog classesDialog = createSelectionDialog(shellForDialogs, fullLibraryPath,
                    pythonClasses, new PythonClassesLabelProvider());
            if (classesDialog.open() == Window.OK) {
                final Object[] result = classesDialog.getResult();

                final Collection<ReferencedLibrary> libraries = new ArrayList<>();
                for (final Object selectedClass : result) {
                    final PythonClass pythonClass = (PythonClass) selectedClass;
                    libraries.add(pythonClass.toReferencedLibrary(fullLibraryPath));
                }
                return libraries;
            }
            return new ArrayList<>();
        }
    }

    public Collection<ReferencedLibrary> importJavaLib(final Shell shell, final RobotRuntimeEnvironment environment, final String fullLibraryPath) {
        final JarStructureBuilder jarStructureBuilder = new JarStructureBuilder(environment);
        final List<JarClass> classesFromJar = newArrayList();
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Reading classes from module '" + fullLibraryPath + "'", 100);
                    classesFromJar.addAll(jarStructureBuilder.provideEntriesFromFile(fullLibraryPath));
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            StatusManager.getManager()
                    .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "RED was unable to find classes inside '" + fullLibraryPath + "' module", e.getCause()),
                            StatusManager.SHOW);
            return new ArrayList<>();
        }

        if (classesFromJar.isEmpty()) {
            StatusManager.getManager()
                    .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "RED was unable to find classes inside '" + fullLibraryPath + "' module"),
                            StatusManager.SHOW);
            return new ArrayList<>();
        } else if (classesFromJar.size() == 1) {
            return newArrayList(classesFromJar.get(0).toReferencedLibrary(fullLibraryPath));
        } else {
            final ElementListSelectionDialog classesDialog = createSelectionDialog(shell, fullLibraryPath,
                    classesFromJar, new JarClassesLabelProvider());

            if (classesDialog.open() == Window.OK) {
                final Object[] result = classesDialog.getResult();

                final Collection<ReferencedLibrary> libraries = new ArrayList<>();
                for (final Object selectedClass : result) {
                    final JarClass jarClass = (JarClass) selectedClass;
                    libraries.add(jarClass.toReferencedLibrary(fullLibraryPath));
                }
                return libraries;
            }
            return new ArrayList<>();
        }
    }

    public ReferencedLibrary importLibFromSpecFile(final String fullLibraryPath) {
        final IPath path = PathsConverter.toWorkspaceRelativeIfPossible(new Path(fullLibraryPath));
        final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
        referencedLibrary.setType(LibraryType.VIRTUAL.toString());
        referencedLibrary.setName(path.lastSegment());
        referencedLibrary.setPath(path.toPortableString());
        return referencedLibrary;
    }

    static ElementListSelectionDialog createSelectionDialog(final Shell shell, final String path,
            final List<?> classes, final LabelProvider labelProvider) {
        final ElementListSelectionDialog classesDialog = new ElementListSelectionDialog(shell, labelProvider);
        classesDialog.setMultipleSelection(true);
        classesDialog.setTitle("Select library class");
        classesDialog.setMessage("Select the class(es) which defines library:\n\t" + path);
        classesDialog.setElements(classes.toArray());

        return classesDialog;
    }

    private static class PythonClassesLabelProvider extends LabelProvider {

        @Override
        public Image getImage(final Object element) {
            return ImagesManager.getImage(RedImages.getJavaClassImage());
        }

        @Override
        public String getText(final Object element) {
            return ((PythonClass) element).getQualifiedName();
        }
    }

    private static class JarClassesLabelProvider extends LabelProvider {

        @Override
        public Image getImage(final Object element) {
            return ImagesManager.getImage(RedImages.getJavaClassImage());
        }

        @Override
        public String getText(final Object element) {
            return ((JarClass) element).getQualifiedName();
        }
    }
}
