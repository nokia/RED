/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.JarStructureBuilder.JarClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.PythonLibStructureBuilder.PythonClass;

/**
 * @author Michal Anglart
 *
 */
public class ReferencedLibraryImporter {


    public ReferencedLibrary importPythonLib(final Shell shellForDialogs, final RobotRuntimeEnvironment environment,
            final String fullLibraryPath) {
        if (fullLibraryPath != null) {
            final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(
                    environment);
            final List<PythonClass> pythonClasses = newArrayList();
            
            try {
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

                    @Override
                    public void run(final IProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException {
                        monitor.beginTask("Reading classes from module '" + fullLibraryPath + "'", 100);
                        try {
                            pythonClasses
                                    .addAll(pythonLibStructureBuilder.provideEntriesFromFile(fullLibraryPath));
                        } catch (final RobotEnvironmentException e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                    "RED was unable to find classes inside '" + fullLibraryPath + "' module", e.getCause()),
                        StatusManager.SHOW);
                return null;
            }

            if (pythonClasses.isEmpty()) {
                StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                    "RED was unable to find classes inside '" + fullLibraryPath + "' module"), 
                        StatusManager.SHOW);
                return null;
            }
            final ElementListSelectionDialog classesDialog = ClassesSelectionDialog.create(shellForDialogs,
                    pythonClasses, new PythonClassesLabelProvider());
            if (classesDialog.open() == Window.OK) {
                final Object[] result = classesDialog.getResult();

                for (final Object selectedClass : result) {
                    final PythonClass pythonClass = (PythonClass) selectedClass;
                    final IPath path = new Path(fullLibraryPath);
                    final IPath pathWithoutModuleName = fullLibraryPath.endsWith("__init__.py")
                            ? path.removeLastSegments(2) : path.removeLastSegments(1);

                    final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
                    referencedLibrary.setType(LibraryType.PYTHON.toString());
                    referencedLibrary.setName(pythonClass.getQualifiedName());
                    referencedLibrary.setPath(
                            PathsConverter.toWorkspaceRelativeIfPossible(pathWithoutModuleName).toPortableString());
                            
                    return referencedLibrary;
                }
            }
        }
        return null;
    }
    
    public ReferencedLibrary importJavaLib(final Shell shell, final String fullLibraryPath) {
        final JarStructureBuilder jarStructureBuilder = new JarStructureBuilder();
        final List<JarClass> classesFromJar = newArrayList();
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Reading classes from module '" + fullLibraryPath + "'", 100);
                    classesFromJar.addAll(jarStructureBuilder.provideEntriesFromFile(fullLibraryPath));
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
                StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                    "RED was unable to find classes inside '" + fullLibraryPath + "' module", e.getCause()),
                        StatusManager.SHOW);
            return null;
        }

        if (classesFromJar.isEmpty()) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                "RED was unable to find classes inside '" + fullLibraryPath + "' module"), 
                    StatusManager.SHOW);
            return null;
        }

        final ElementListSelectionDialog classesDialog = ClassesSelectionDialog
                .create(shell, classesFromJar, new JarClassesLabelProvider());

        if (classesDialog.open() == Window.OK) {
            final Object[] result = classesDialog.getResult();

            for (final Object selectedClass : result) {
                final JarClass jarClass = (JarClass) selectedClass;

                final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
                referencedLibrary.setType(LibraryType.JAVA.toString());
                referencedLibrary.setName(jarClass.getQualifiedName());
                referencedLibrary.setPath(
                        PathsConverter.toWorkspaceRelativeIfPossible(new Path(fullLibraryPath)).toPortableString());

                return referencedLibrary;
            }
        }
        return null;
    }

    public ReferencedLibrary importLibFromSpecFile(final String fullLibraryPath) {
        final IPath path = PathsConverter.toWorkspaceRelativeIfPossible(new Path(fullLibraryPath));
        final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
        referencedLibrary.setType(LibraryType.VIRTUAL.toString());
        referencedLibrary.setName(path.lastSegment());
        referencedLibrary.setPath(path.toPortableString());
        return referencedLibrary;
    }

}
