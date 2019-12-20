/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * @author lwlodarc
 */
public class RedXmlVersionUpdater implements IResourceChangeListener, IMarkerResolution {

    @VisibleForTesting
    final static List<RedXmlVersionTransition> RED_XML_TRANSITIONS = ImmutableList
            .of(new TransitionFromVer10ToVer1("1.0", true), new TransitionFromVer1ToVer2("1", true));

    public static void init() {
        final List<RobotProject> projectsToUpdate = Arrays
                .stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
                .filter(RobotProjectNature::hasRobotNature)
                .map(p -> RedPlugin.getModelManager().getModel().createRobotProject(p))
                .filter(RedXmlVersionUpdater::hasVersionMismatch)
                .collect(Collectors.toList());
        askAndUpdateRedXml(projectsToUpdate);
        ResourcesPlugin.getWorkspace()
                .addResourceChangeListener(new RedXmlVersionUpdater(), IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getDelta() != null) {
            try {
                event.getDelta().accept(delta -> {
                    final IResource resource = delta.getResource();
                    if (((resource.getType() & IResource.PROJECT) != 0) && resource.getProject().isOpen()
                            && (delta.getKind() == IResourceDelta.CHANGED || delta.getKind() == IResourceDelta.ADDED)
                            && ((delta.getFlags() & IResourceDelta.OPEN) != 0)
                            && RobotProjectNature.hasRobotNature((IProject) resource)) {

                        final RobotProject robotProject = RedPlugin.getModelManager()
                                .createProject((IProject) resource);

                        // TODO: Importing project would not trigger this
                        // (IFile.exists() == false) in RedEclipseProjectConfigReader
                        if (hasVersionMismatch(robotProject)) {
                            askAndUpdateRedXml(newArrayList(robotProject));
                        }
                    }
                    return true;
                });
            } catch (final CoreException e) {
                StatusManager.getManager()
                        .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage()), StatusManager.SHOW);
            }
        }
    }

    private static boolean hasVersionMismatch(final RobotProject robotProject) {
        final RobotProjectConfig config = robotProject.getRobotProjectConfig();
        return !config.isNullConfig() && !RobotProjectConfig.CURRENT_VERSION.equals(config.getVersion().getVersion());
    }

    @Override
    public String getLabel() {
        return "Update red.xml to version " + RobotProjectConfig.CURRENT_VERSION;
    }

    @Override
    public void run(final IMarker marker) {
        final IResource redXmlFile = marker.getResource();
        if (redXmlFile == null || !redXmlFile.exists() || !redXmlFile.getName().equals(RobotProjectConfig.FILENAME)) {
            throw new IllegalStateException("Marker is assigned to invalid file");
        }
        final IProject project = redXmlFile.getProject();
        final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
        tryToUpdateRedXml(newArrayList(robotProject));
        if (RobotProjectConfig.CURRENT_VERSION.equals(robotProject.getRobotProjectConfig().getVersion().getVersion())) {
            try {
                marker.delete();
            } catch (final CoreException e) {
                StatusManager.getManager()
                        .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage()), StatusManager.SHOW);
            }
        }
    }

    @VisibleForTesting
    static boolean isAutoUpdatePossible(final RobotProject robotProject) {
        final String currentRedXmlVersion = getRedXmlVersionFromProject(robotProject);
        return isAutoUpdatePossible(findCurrentVersionIndex(currentRedXmlVersion));
    }

    private static boolean isAutoUpdatePossible(final int currentVersionId) {
        if (currentVersionId > -1) {
            for (int i = currentVersionId; i < RED_XML_TRANSITIONS.size(); i++) {
                if (!RED_XML_TRANSITIONS.get(i).canBeAutoUpgraded) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static String getRedXmlVersionFromProject(final RobotProject robotProject) {
        return getRedXmlVersionFromConfig(robotProject.getRobotProjectConfig());
    }

    private static String getRedXmlVersionFromConfig(final RobotProjectConfig config) {
        return config.getVersion().getVersion();
    }

    private static int findCurrentVersionIndex(final String currentRedXmlVersion) {
        for (int i = 0; i < RED_XML_TRANSITIONS.size(); i++) {
            if (RED_XML_TRANSITIONS.get(i).version.equals(currentRedXmlVersion)) {
                return i;
            }
        }
        return -1;
    }

    private static void executeRedXmlUpdate(final RobotProject robotProject) {
        final RobotProjectConfig config = new RedEclipseProjectConfigReader()
                .readConfiguration(robotProject.getConfigurationFile());

        executeRedXmlUpdate(config);

        new RedEclipseProjectConfigWriter().writeConfiguration(config, robotProject);
    }

    @VisibleForTesting
    static void executeRedXmlUpdate(final RobotProjectConfig config) {
        final int startId = findCurrentVersionIndex(getRedXmlVersionFromConfig(config));
        for (int i = startId; i < RED_XML_TRANSITIONS.size(); i++) {
            RED_XML_TRANSITIONS.get(i).upgradeRedXml(config);
        }
        config.setVersion(RobotProjectConfig.CURRENT_VERSION);
    }

    private static void askAndUpdateRedXml(final List<RobotProject> robotProjects) {
        // do not do anything for headless
        if (PlatformUI.isWorkbenchRunning() && !robotProjects.isEmpty()) {
            new Thread(() -> {
                final List<RobotProject> projectsToUpdate = askUserAboutUpdateRedXml(robotProjects);
                tryToUpdateRedXml(projectsToUpdate);
            }).start();
        }
    }

    private static void tryToUpdateRedXml(final List<RobotProject> robotProjects) {
        final List<RobotProject> notUpdated = new ArrayList<>();
        for (final RobotProject project : robotProjects) {
            if (isAutoUpdatePossible(project)) {
                executeRedXmlUpdate(project);
            } else {
                notUpdated.add(project);
            }
        }
        if (!notUpdated.isEmpty()) {
            final String detailedMessage = robotProjects.stream()
                    .map(project -> "\nProject " + project.getName() + " from version "
                            + project.getRobotProjectConfig().getVersion().getVersion())
                    .collect(Collectors.joining());
            DetailedErrorDialog.openErrorDialog(
                    "Could not auto-update red.xml file(s) to version " + RobotProjectConfig.CURRENT_VERSION + ".",
                    detailedMessage);
        }
    }

    private static List<RobotProject> askUserAboutUpdateRedXml(final List<RobotProject> robotProjects) {
        final String redXmlDetectedVersion = robotProjects.get(0).getRobotProjectConfig().getVersion().getVersion();
        final List<String> projectNames = robotProjects.stream()
                .map(RobotProject::getName)
                .collect(Collectors.toList());

        final Evaluation<List<RobotProject>> projectsEval = new Evaluation<List<RobotProject>>() {

            @Override
            public List<RobotProject> runCalculation() {
                final Shell shell = Display.getCurrent().getActiveShell();
                final ListSelectionDialog dialog = new ListSelectionDialog(shell, projectNames,
                        ArrayContentProvider.getInstance(), new LabelProvider(),
                        "RED detected old version of configuration file: \"" + redXmlDetectedVersion + "\", but now \""
                                + RobotProjectConfig.CURRENT_VERSION + "\" is in use."
                                + "\nSelect projects which you want to auto-update."
                                + "\nRemark: After updating your projects would be incompatible with old RED versions.");
                dialog.setTitle("Red.xml auto-updater");
                dialog.setInitialElementSelections(projectNames);
                if (dialog.open() == Window.OK) {
                    return robotProjects.stream()
                            .filter(p -> Arrays.asList(dialog.getResult()).contains(p.getName()))
                            .collect(Collectors.toList());
                }

                return new ArrayList<>();
            }
        };
        return SwtThread.syncEval(projectsEval);
    }

    private static abstract class RedXmlVersionTransition {

        final String version;

        final boolean canBeAutoUpgraded;

        RedXmlVersionTransition(final String version, final boolean canBeAutoUpgraded) {
            this.version = version;
            this.canBeAutoUpgraded = canBeAutoUpgraded;
        }

        abstract void upgradeRedXml(final RobotProjectConfig config);
    }

    private static class TransitionFromVer10ToVer1 extends RedXmlVersionTransition {

        TransitionFromVer10ToVer1(final String version, final boolean canBeAutoUpgraded) {
            super(version, canBeAutoUpgraded);
        }

        @Override
        void upgradeRedXml(final RobotProjectConfig config) {
            // nothing to do, version alias only
        }

    }

    private static class TransitionFromVer1ToVer2 extends RedXmlVersionTransition {

        TransitionFromVer1ToVer2(final String version, final boolean canBeAutoUpgraded) {
            super(version, canBeAutoUpgraded);
        }

        @Override
        void upgradeRedXml(final RobotProjectConfig config) {
            final List<ReferencedLibrary> refLibs = config.getReferencedLibraries();

            for (final ReferencedLibrary lib : refLibs) {
                updateReferencedLibraryRecord(lib);
            }
        }

        private void updateReferencedLibraryRecord(final ReferencedLibrary lib) {
            // this method uses python search order - directory module then file then file with
            // class inside
            if (LibraryType.PYTHON.name().equals(lib.getType())) {
                final String oldPathWithName = lib.getPath() + "/" + lib.getName().replaceAll("\\.", "/");
                if (!checkAndUpdatePathIfCorrect(lib, new Path(oldPathWithName + "/__init__.py"))) {
                    if (!checkAndUpdatePathIfCorrect(lib, new Path(oldPathWithName + ".py"))) {
                        final IPath pathToCheck = new Path(oldPathWithName).removeLastSegments(1)
                                .addFileExtension("py");
                        checkAndUpdatePathIfCorrect(lib, pathToCheck);
                    }
                }
            }
        }

        private boolean checkAndUpdatePathIfCorrect(final ReferencedLibrary lib, final IPath pathToCheck) {
            final File file = RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(pathToCheck).toFile();
            if (file.exists()) {
                lib.setPath(pathToCheck.toString());
                return true;
            }
            return false;
        }
    }
}
