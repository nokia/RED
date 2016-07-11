/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class RobotProjectNature implements IProjectNature {

    public static final String ROBOT_NATURE = RedPlugin.PLUGIN_ID + ".robotNature";

    private static final String ROBOT_LIBRARIES_BUILDER = RedPlugin.PLUGIN_ID + ".robotLibrariesBuilder";

    private static final String SUITE_INIT_FILE = "__init__";

	private IProject project;

    public static void addRobotNature(final IProject project, final IProgressMonitor monitor) throws CoreException {
        final IProjectDescription desc = project.getDescription();

        final ArrayList<String> natures = newArrayList(desc.getNatureIds());
        natures.add(ROBOT_NATURE);
        desc.setNatureIds(natures.toArray(new String[0]));

        project.setDescription(desc, monitor);
    }

    public static void removeRobotNature(final IProject project, final IProgressMonitor monitor,
            final boolean askForRedXmlRemoval) throws CoreException {
        final IProjectDescription desc = project.getDescription();

        final ArrayList<String> natures = newArrayList(desc.getNatureIds());
        natures.remove(ROBOT_NATURE);
        desc.setNatureIds(natures.toArray(new String[0]));

        project.setDescription(desc, monitor);

        final boolean shouldRemove = askForRedXmlRemoval ? shouldRedXmlBeRemoved(project.getName()) : true;
        if (shouldRemove) {
            final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
            if (cfgFile.exists()) {
                cfgFile.delete(true, null);
            }
        }
    }

    private static boolean shouldRedXmlBeRemoved(final String projectName) {
        return MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Confirm configuration file removal",
                "You have deconfigured the project '" + projectName
                        + "' as a Robot project. Do you want to remove project configuration file 'red.xml' too?");
    }

    public static IFile createRobotInitializationFile(final IFolder folder, final String extension)
            throws CoreException {
        final IFile initFile = folder.getFile(SUITE_INIT_FILE + "." + extension);
        initFile.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
        return initFile;
    }

    public static boolean isRobotSuiteInitializationFile(final IFile file) {
        final String name = file.getName();
        return file.exists() && (name.equals(SUITE_INIT_FILE + ".robot") || name.equals(SUITE_INIT_FILE + ".tsv"))
                && hasRobotNature(file.getProject());
    }

    public static boolean isRobotSuite(final IFolder folder) {
        final IFile robotFile = folder.getFile(SUITE_INIT_FILE + ".robot");
        final IFile txtFile = folder.getFile(SUITE_INIT_FILE + ".txt");
        final IFile tsvFile = folder.getFile(SUITE_INIT_FILE + ".tsv");
        return (robotFile.exists() || tsvFile.exists() || txtFile.exists()) && hasRobotNature(folder.getProject());
    }

    public static boolean hasRobotNature(final IProject project) {
        try {
            return project.hasNature(ROBOT_NATURE);
        } catch (final CoreException e) {
            return false;
        }
    }

	@Override
	public void configure() throws CoreException {
        addToBuildSpec(project, ROBOT_LIBRARIES_BUILDER);
        new RobotProjectConfigWriter().writeConfiguration(RobotProjectConfig.create(), project);
	}

	@Override
	public void deconfigure() throws CoreException {
        removeFromBuildSpec(project, ROBOT_LIBRARIES_BUILDER);
	}

    @Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(final IProject project) {
		this.project = project;
	}

    private void addToBuildSpec(final IProject project, final String builderId) throws CoreException {
        if (project == null) {
            return;
        }
        final IProjectDescription description = project.getDescription();
        final ICommand[] commands = description.getBuildSpec();
        for (final ICommand command : commands) {
            if (command.getBuilderName().equals(builderId)) {
                return; // builder is already installed
            }
        }
        final ICommand command = description.newCommand();
        command.setBuilderName(builderId);
        final ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);
        newCommands[newCommands.length - 1] = command;
        description.setBuildSpec(newCommands);
        project.setDescription(description, null);
    }

    private void removeFromBuildSpec(final IProject project, final String builderId) throws CoreException {
        if (project == null) {
            return;
        }

        final IProjectDescription description = project.getDescription();
        final ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(builderId)) {
                final ICommand[] newCommands = new ICommand[commands.length - 1];
                System.arraycopy(commands, 0, newCommands, 0, i);
                System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
                description.setBuildSpec(newCommands);
                project.setDescription(description, null);
                return;
            }
        }
    }
}
