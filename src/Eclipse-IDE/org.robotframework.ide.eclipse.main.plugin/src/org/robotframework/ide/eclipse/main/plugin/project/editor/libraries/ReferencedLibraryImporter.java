/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
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
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;
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
    public Collection<ReferencedLibrary> importPythonLib(final IRuntimeEnvironment environment, final IProject project,
            final RobotProjectConfig config, final File library) {
        final ILibraryStructureBuilder builder = new PythonLibStructureBuilder(environment, config, project);
        return importLib(builder, library, libClass -> true);
    }

    @Override
    public Collection<ReferencedLibrary> importPythonLib(final IRuntimeEnvironment environment, final IProject project,
            final RobotProjectConfig config, final File library, final String name) {
        final ILibraryStructureBuilder builder = new PythonLibStructureBuilder(environment, config, project);
        return importLib(builder, library, libClass -> libClass.getQualifiedName().equals(name));
    }

    @Override
    public Collection<ReferencedLibrary> importJavaLib(final IRuntimeEnvironment environment, final IProject project,
            final RobotProjectConfig config, final File library) {
        final ILibraryStructureBuilder builder = new ArchiveStructureBuilder(environment, config, project);
        return importLib(builder, library, libClass -> true);
    }

    @Override
    public Collection<ReferencedLibrary> importJavaLib(final IRuntimeEnvironment environment, final IProject project,
            final RobotProjectConfig config, final File library, final String name) {
        final ILibraryStructureBuilder builder = new ArchiveStructureBuilder(environment, config, project);
        return importLib(builder, library, libClass -> libClass.getQualifiedName().equals(name));
    }

    public ReferencedLibrary importLibFromSpecFile(final File library) {
        final IPath path = RedWorkspace.Paths.toWorkspaceRelativeIfPossible(new Path(library.getAbsolutePath()));
        return ReferencedLibrary.create(LibraryType.VIRTUAL, path.lastSegment(), path.toPortableString());
    }

    private Collection<ReferencedLibrary> importLib(final ILibraryStructureBuilder builder, final File library,
            final Predicate<ILibraryClass> shouldInclude) {
        final List<ILibraryClass> classes = new ArrayList<>();
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(monitor -> {
                monitor.beginTask("Reading classes/modules from module '" + library.getAbsolutePath() + "'", 100);
                try {
                    classes.addAll(builder.provideEntriesFromFile(library, shouldInclude));
                } catch (final RuntimeEnvironmentException | URISyntaxException e) {
                    throw new InvocationTargetException(e);
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            DetailedErrorDialog.openErrorDialog(
                    "RED was unable to find classes/modules inside '" + library.getAbsolutePath() + "' module",
                    e.getCause().getMessage());
            return new ArrayList<>();
        }

        if (classes.isEmpty()) {
            StatusManager.getManager()
                    .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "RED was unable to find classes/modules inside '" + library.getAbsolutePath() + "' module"),
                            StatusManager.SHOW);
            return new ArrayList<>();
        } else {
            final List<ILibraryClass> classesToImport = classes.size() > 1 ? askUserToSelectClasses(library, classes)
                    : classes;
            return classesToImport.stream()
                    .map(libClass -> libClass.toReferencedLibrary(library.getAbsolutePath()))
                    .collect(Collectors.toList());
        }
    }

    private List<ILibraryClass> askUserToSelectClasses(final File library, final List<ILibraryClass> classes) {
        final LabelProvider labelProvider = createLabelProvider();
        final ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, labelProvider);
        dialog.setMultipleSelection(true);
        dialog.setTitle("Select library class");
        dialog.setMessage("Select the class(es) which defines library:\n\t" + library.getAbsolutePath());
        dialog.setElements(classes.toArray());
        if (dialog.open() == Window.OK) {
            return Stream.of(dialog.getResult()).map(ILibraryClass.class::cast).collect(Collectors.toList());
        }
        return new ArrayList<>();

    }

    private static LabelProvider createLabelProvider() {
        return new LabelProvider() {

            @Override
            public Image getImage(final Object element) {
                final ImageDescriptor image = ((ILibraryClass) element).getType() == LibraryType.JAVA
                        ? RedImages.getJavaClassImage()
                        : RedImages.getPythonLibraryImage();
                return ImagesManager.getImage(image);
            }

            @Override
            public String getText(final Object element) {
                return ((ILibraryClass) element).getQualifiedName();
            }

        };
    }
}
