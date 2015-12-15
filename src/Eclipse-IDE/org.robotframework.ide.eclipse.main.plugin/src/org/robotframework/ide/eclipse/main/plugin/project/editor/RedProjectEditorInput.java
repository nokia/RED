/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.core.resources.IProject;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;

public class RedProjectEditorInput {

    private final IProject project;
    private final boolean isEditable;
    private RobotProjectConfig projectConfiguration;

    public RedProjectEditorInput(final boolean isEditable, final RobotProjectConfig projectConfig,
            final IProject project) {
        this.project = project;
        this.isEditable = isEditable;
        this.projectConfiguration = projectConfig;
    }

    public RobotProject getRobotProject() {
        return RedPlugin.getModelManager().getModel().createRobotProject(project);
    }

    public RobotProjectConfig getProjectConfiguration() {
        return projectConfiguration;
    }

    public boolean isEditable() {
        return isEditable;
    }
    
    public void refreshProjectConfiguration() {
        projectConfiguration = RedPlugin.getModelManager().getModel().createRobotProject(project).getRobotProjectConfig();
    }
}
