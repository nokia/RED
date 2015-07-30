package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.core.resources.IProject;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;

class RedProjectEditorInput {

    private final IProject project;
    private final boolean isEditable;
    private final RobotProjectConfig projectConfiguration;

    RedProjectEditorInput(final boolean isEditable, final RobotProjectConfig projectConfig,
            final IProject project) {
        this.project = project;
        this.isEditable = isEditable;
        this.projectConfiguration = projectConfig;
    }

    RobotProject getRobotProject() {
        return RedPlugin.getModelManager().getModel().createRobotProject(project);
    }

    RobotProjectConfig getProjectConfiguration() {
        return projectConfiguration;
    }

    boolean isEditable() {
        return isEditable;
    }
}
