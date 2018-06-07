/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.RedURI;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;

/**
 * @author Michal Anglart
 */
public class ReferencedLibraryImporter implements IReferencedLibraryImporter {

    private final Shell shell;

    public ReferencedLibraryImporter(final Shell shell) {
        this.shell = shell;
    }

    @Override
    public Collection<ReferencedLibrary> importPythonLib(final RobotRuntimeEnvironment environment,
            final IProject project, final RobotProjectConfig config, final String fullLibraryPath) {
        final ILibraryStructureBuilder builder = new PythonLibStructureBuilder(environment, config, project);
        return importLib(builder, fullLibraryPath, Optional.empty(), RedImages.getPythonLibraryImage());
    }

    @Override
    public Collection<ReferencedLibrary> importPythonLib(final RobotRuntimeEnvironment environment,
            final IProject project, final RobotProjectConfig config, final String fullLibraryPath, final String name) {
        final ILibraryStructureBuilder builder = new PythonLibStructureBuilder(environment, config, project);
        return importLib(builder, fullLibraryPath, Optional.of(name), RedImages.getPythonLibraryImage());
    }

    @Override
    public Collection<ReferencedLibrary> importJavaLib(final RobotRuntimeEnvironment environment,
            final IProject project, final RobotProjectConfig config, final String fullLibraryPath) {
        final ILibraryStructureBuilder builder = new JarStructureBuilder(environment, config, project);
        return importLib(builder, fullLibraryPath, Optional.empty(), RedImages.getJavaClassImage());
    }

    @Override
    public Collection<ReferencedLibrary> importJavaLib(final RobotRuntimeEnvironment environment,
            final IProject project, final RobotProjectConfig config, final String fullLibraryPath, final String name) {
        final ILibraryStructureBuilder builder = new JarStructureBuilder(environment, config, project);
        return importLib(builder, fullLibraryPath, Optional.of(name), RedImages.getJavaClassImage());
    }

    public ReferencedLibrary importLibFromSpecFile(final String fullLibraryPath) {
        final IPath path = RedWorkspace.Paths.toWorkspaceRelativeIfPossible(new Path(fullLibraryPath));
        return ReferencedLibrary.create(LibraryType.VIRTUAL, path.lastSegment(), path.toPortableString());
    }

    private Collection<ReferencedLibrary> importLib(final ILibraryStructureBuilder builder,
            final String fullLibraryPath, final Optional<String> name, final ImageDescriptor libImageDescriptor) {
        final List<ILibraryClass> libClasses = new ArrayList<>();
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(monitor -> {
                monitor.beginTask("Reading classes/modules from module '" + fullLibraryPath + "'", 100);
                try {
                    final Collection<ILibraryClass> libClassesFromFile = builder
                            .provideEntriesFromFile(RedURI.fromString(fullLibraryPath));
                    libClasses.addAll(libClassesFromFile.stream()
                            .filter(libClass -> !name.isPresent() || libClass.getQualifiedName().equals(name.get()))
                            .collect(Collectors.toList()));
                } catch (final RobotEnvironmentException | URISyntaxException e) {
                    throw new InvocationTargetException(e);
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            DetailedErrorDialog.openErrorDialog(
                    "RED was unable to find classes/modules inside '" + fullLibraryPath + "' module",
                    e.getCause().getMessage());
            return new ArrayList<>();
        }

        if (libClasses.isEmpty()) {
            StatusManager.getManager()
                    .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "RED was unable to find classes/modules inside '" + fullLibraryPath + "' module"),
                            StatusManager.SHOW);
            return new ArrayList<>();
        } else if (libClasses.size() == 1) {
            return newArrayList(libClasses.get(0).toReferencedLibrary(fullLibraryPath));
        } else {
            final ElementListSelectionDialog classesDialog = createSelectionDialog(fullLibraryPath, libClasses,
                    libImageDescriptor);
            if (classesDialog.open() == Window.OK) {
                return Stream.of(classesDialog.getResult())
                        .map(ILibraryClass.class::cast)
                        .map(libClass -> libClass.toReferencedLibrary(fullLibraryPath))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private ElementListSelectionDialog createSelectionDialog(final String path, final Collection<ILibraryClass> classes,
            final ImageDescriptor libImageDescriptor) {
        final LabelProvider labelProvider = createLabelProvider(libImageDescriptor);
        final ElementListSelectionDialog classesDialog = new ElementListSelectionDialog(shell, labelProvider);
        classesDialog.setMultipleSelection(true);
        classesDialog.setTitle("Select library class");
        classesDialog.setMessage("Select the class(es) which defines library:\n\t" + path);
        classesDialog.setElements(classes.toArray());

        return classesDialog;
    }

    private static LabelProvider createLabelProvider(final ImageDescriptor libImageDescriptor) {
        return new LabelProvider() {

            @Override
            public Image getImage(final Object element) {
                return ImagesManager.getImage(libImageDescriptor);
            }

            @Override
            public String getText(final Object element) {
                return ((ILibraryClass) element).getQualifiedName();
            }

        };
    }
}
