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
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
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

/**
 * @author lwlodarc
 */
public class RedXmlVersionUpdater implements IResourceChangeListener, IMarkerResolution {

    @VisibleForTesting
    final static List<RedXmlVersionTransition> RED_XML_TRANSITIONS = newArrayList(
            new TransitionFromVer1_0ToVer1("1.0", true), new TransitionFromVer1ToVer2("1", true));

    public RedXmlVersionUpdater() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        final IResourceDelta delta = event.getDelta();
        if (delta != null) {
            try {
                delta.accept(new IResourceDeltaVisitor() {

                    public boolean visit(IResourceDelta delta) throws CoreException {
                        IResource resource = delta.getResource();
                        if (((resource.getType() & IResource.PROJECT) != 0) && resource.getProject().isOpen()
                                && (delta.getKind() == IResourceDelta.CHANGED
                                        || delta.getKind() == IResourceDelta.ADDED)
                                && ((delta.getFlags() & IResourceDelta.OPEN) != 0)
                                && RobotProjectNature.hasRobotNature((IProject) resource)) {

                            final RobotProject robotProject = RedPlugin.getModelManager()
                                    .createProject((IProject) resource);
                            final RobotProjectConfig config = robotProject.getRobotProjectConfig();

                            // TODO: Importing project would not trigger this.
                            // IFile.exists() == false;
                            if (!config.isNullConfig()
                                    && !RobotProjectConfig.CURRENT_VERSION.equals(config.getVersion().getVersion())) {
                                askAndUpdateRedXml(newArrayList(robotProject));
                            }
                        }
                        return true;
                    }
                });
            } catch (CoreException e) {
                StatusManager.getManager()
                        .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage()), StatusManager.SHOW);
            }
        }
    }

    public static void checkAlreadyOpenedProjects() {
        List<RobotProject> projectsToUpdate = Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
                .filter(p -> RobotProjectNature.hasRobotNature(p))
                .map(p -> RedPlugin.getModelManager().getModel().createRobotProject(p))
                .filter(p -> !RobotProjectConfig.CURRENT_VERSION
                        .equals(p.getRobotProjectConfig().getVersion().getVersion()))
                .collect(Collectors.toList());
        if (!projectsToUpdate.isEmpty()) {
            askAndUpdateRedXml(projectsToUpdate);
        }
    }

    @Override
    public String getLabel() {
        return "Update red.xml to version " + RobotProjectConfig.CURRENT_VERSION;
    }

    @Override
    public void run(IMarker marker) {
        final IResource redXmlFile = marker.getResource();
        if (redXmlFile == null || !redXmlFile.exists() || !redXmlFile.getName().equals(RobotProjectConfig.FILENAME)) {
            throw new IllegalStateException("Marker is assigned to invalid file");
        }
        IProject project = redXmlFile.getProject();
        final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
        tryToUpdateRedXml(newArrayList(robotProject));
        if (RobotProjectConfig.CURRENT_VERSION.equals(robotProject.getRobotProjectConfig().getVersion().getVersion())) {
            try {
                marker.delete();
            } catch (CoreException e) {
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

    public static void executeRedXmlUpdate(final RobotProject robotProject) {
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

    private static void askAndUpdateRedXml(List<RobotProject> robotProjects) {
        new Thread(() -> {
            List<RobotProject> projects = askUserAboutUpdateRedXml(robotProjects);
            tryToUpdateRedXml(projects);
        }).start();
    }

    private static void tryToUpdateRedXml(final List<RobotProject> robotProjects) {
        final List<RobotProject> nonUpdatable = new ArrayList<>();
        for (RobotProject project : robotProjects) {
            if (isAutoUpdatePossible(project)) {
                executeRedXmlUpdate(project);
            } else {
                nonUpdatable.add(project);
            }
        }
        if (!nonUpdatable.isEmpty()) {
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

    static class RobotProjectListContainer {

        List<RobotProject> projects;

        RobotProjectListContainer(final List<RobotProject> projects) {
            this.projects = projects;
        }

        List<RobotProject> getProjects() {
            return projects;
        }

        void setProjects(final List<RobotProject> projects) {
            this.projects = projects;
        }
    }

    static abstract class RedXmlVersionTransition {

        final String version;

        final boolean canBeAutoUpgraded;

        RedXmlVersionTransition(final String version, final boolean canBeAutoUpgraded) {
            this.version = version;
            this.canBeAutoUpgraded = canBeAutoUpgraded;
        }

        abstract void upgradeRedXml(final RobotProjectConfig config);
    }

    static class TransitionFromVer1_0ToVer1 extends RedXmlVersionTransition {

        TransitionFromVer1_0ToVer1(String version, boolean canBeAutoUpgraded) {
            super(version, canBeAutoUpgraded);
        }

        @Override
        void upgradeRedXml(RobotProjectConfig config) {
            // nothing to do, version alias only
        }

    }

    static class TransitionFromVer1ToVer2 extends RedXmlVersionTransition {

        TransitionFromVer1ToVer2(String version, boolean canBeAutoUpgraded) {
            super(version, canBeAutoUpgraded);
        }

        @Override
        void upgradeRedXml(final RobotProjectConfig config) {
            final List<ReferencedLibrary> refLibs = config.getReferencedLibraries();

            for (ReferencedLibrary lib : refLibs) {
                updateReferencedLibraryRecord(lib);
            }
        }

        private void updateReferencedLibraryRecord(final ReferencedLibrary lib) {
            // this method uses python search order - directory module then file
            if (LibraryType.PYTHON.name().equals(lib.getType())) {
                String oldPath = lib.getPath();
                Path newPath = new Path(oldPath + "/" + lib.getName().replaceAll("\\.", "/") + "/__init__.py");
                final File file = RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(newPath).toFile();
                if (file.exists()) {
                    lib.setPath(newPath.toString());
                } else {
                    lib.setPath(oldPath + "/" + lib.getName().replaceAll("\\.", "/") + ".py");
                }
            }
        }
    }
}
