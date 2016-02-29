/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.fileWatcher.IWatchEventHandler;
import org.rf.ide.core.fileWatcher.RedFileWatcher;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.libs.LibrariesBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.swt.SwtThread;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @author mmarzec
 */
public class LibrariesWatchHandler implements IWatchEventHandler {

    private ListMultimap<LibrarySpecification, String> librarySpecifications = Multimaps
            .synchronizedListMultimap(ArrayListMultimap.<LibrarySpecification, String> create());

    private IEventBroker eventBroker = null;

    private final RobotProject robotProject;

    public LibrariesWatchHandler(final RobotProject robotProject) {
        this.robotProject = robotProject;
    }

    public void registerLibrary(final String absolutePathToLibraryFile, final LibrarySpecification spec) {

        if (absolutePathToLibraryFile != null && spec != null && !librarySpecifications.containsKey(spec)) {
            final File libFile = new File(absolutePathToLibraryFile);
            final File libDir = libFile.getParentFile();
            final String libFileName = libFile.getName();
            if (libDir != null && libDir.exists() && libDir.isDirectory()) {
                if (isPythonModule(absolutePathToLibraryFile)) {
                    String[] moduleFilesList = extractPythonModuleFiles(libDir);
                    if (moduleFilesList != null) {
                        for (int i = 0; i < moduleFilesList.length; i++) {
                            addLibraryToWatch(moduleFilesList[i], libDir.toPath(), spec);
                        }
                    }
                } else {
                    addLibraryToWatch(libFileName, libDir.toPath(), spec);
                }
            }
        }
    }

    public void unregisterLibraries(final List<ReferencedLibrary> libraries) {
        if (libraries != null) {
            for (ReferencedLibrary referencedLibrary : libraries) {
                final String path = referencedLibrary.getAbsolutePathToFile();
                if (path != null) {
                    final File libFile = new File(path);
                    final File libDir = libFile.getParentFile();
                    if (isPythonModule(path) && libDir != null && libDir.exists()) {
                        String[] moduleFilesList = extractPythonModuleFiles(libDir);
                        if (moduleFilesList != null) {
                            for (int i = 0; i < moduleFilesList.length; i++) {
                                removeLibraryToWatch(moduleFilesList[i]);
                            }
                        }
                    } else {
                        removeLibraryToWatch(libFile.getName());
                    }
                }
            }
        }
    }

    private boolean isPythonModule(final String absolutePathToLibraryFile) {
        return absolutePathToLibraryFile.endsWith("__init__.py");
    }

    private String[] extractPythonModuleFiles(final File fileDir) {
        return fileDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".py")) {
                    return true;
                }
                return false;
            }
        });
    }

    private void addLibraryToWatch(final String fileName, final Path dir, final LibrarySpecification spec) {
        librarySpecifications.put(spec, fileName);
        registerPath(dir, fileName, this);
    }

    private void removeLibraryToWatch(final String fileName) {
        removeLibrarySpecification(fileName);
        unregisterFile(fileName, this);
    }

    private void removeLibrarySpecification(final String fileName) {
        final List<LibrarySpecification> specsToRemove = new ArrayList<>();
        synchronized (librarySpecifications) {
            for (Entry<LibrarySpecification, String> entry : librarySpecifications.entries()) {
                if (entry.getValue().equals(fileName)) {
                    specsToRemove.add(entry.getKey());
                }
            }
            for (final LibrarySpecification spec : specsToRemove) {
                librarySpecifications.removeAll(spec);
            }
        }
    }

    @Override
    public void registerPath(final Path dir, final String fileName, final IWatchEventHandler handler) {
        RedFileWatcher.getInstance().registerPath(dir, fileName, this);
    }

    @Override
    public void unregisterFile(final String fileName, final IWatchEventHandler handler) {
        RedFileWatcher.getInstance().unregisterFile(fileName, this);
    }

    @Override
    public void handleModifyEvent(final String modifiedFileName) {
        if (librarySpecifications.containsValue(modifiedFileName)) {

            final IProject project = robotProject.getProject();
            if (!project.exists()) {
                removeLibrarySpecification(modifiedFileName);
                unregisterFile(modifiedFileName, this);
                return;
            }

            SwtThread.asyncExec(new Runnable() {

                @Override
                public void run() {

                    if (robotProject.getRobotProjectConfig().isReferencedLibrariesAutoReloadEnabled()) {
                        final List<LibrarySpecification> specsToRebuild = new ArrayList<>();
                        synchronized (librarySpecifications) {
                            for (Entry<LibrarySpecification, String> entry : librarySpecifications.entries()) {
                                if (entry.getValue().equals(modifiedFileName)) {
                                    specsToRebuild.add(entry.getKey());
                                }
                            }
                        }
                        rebuildLibrary(project, specsToRebuild);
                    } else {
                        changeSpecModifyFlag(modifiedFileName);
                    }
                    refreshNavigator(project);
                }
            });
        }
    }

    private void rebuildLibrary(final IProject project, final List<LibrarySpecification> specs) {
        final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        try {
            new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    final Multimap<IProject, LibrarySpecification> groupedSpecifications = LinkedHashMultimap.create();
                    groupedSpecifications.putAll(project, specs);
                    new LibrariesBuilder(new BuildLogger()).forceLibrariesRebuild(groupedSpecifications,
                            SubMonitor.convert(monitor));
                    robotProject.clearConfiguration();
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            MessageDialog.openError(shell, "Regenerating library specification",
                    "Problems occured during library specification generation " + e.getCause().getMessage());
        }
    }

    private void changeSpecModifyFlag(final String modifiedFileName) {
        final List<LibrarySpecification> specsToChange = new ArrayList<>();
        final Map<ReferencedLibrary, LibrarySpecification> referencedLibraries = robotProject.getReferencedLibraries();
        for (final ReferencedLibrary refLib : referencedLibraries.keySet()) {
            final String absolutePathToFile = refLib.getAbsolutePathToFile();
            if (absolutePathToFile != null && new File(absolutePathToFile).getName().equals(modifiedFileName)) {
                specsToChange.add(referencedLibraries.get(refLib));
            }
        }

        for (final LibrarySpecification spec : specsToChange) {
            spec.setIsModified(true);
        }
    }

    private void refreshNavigator(final IProject project) {
        if (eventBroker == null) {
            eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
        }
        if (eventBroker != null) {
            eventBroker.post(RobotModelEvents.ROBOT_LIBRARY_SPECIFICATION_CHANGE, project);
        }
    }

}
