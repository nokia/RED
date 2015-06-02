package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfiguration;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigurationFile;


public class CreateConfigurationFileFixer implements IMarkerResolution {

    @Override
    public String getLabel() {
        return "Create configuration file";
    }

    @Override
    public void run(final IMarker marker) {
        final IProject project = marker.getResource().getProject();
        try {
            new RobotProjectConfigurationFile(project).write(RobotProjectConfiguration.create());
            marker.getResource().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        } catch (final CoreException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, RobotFramework.PLUGIN_ID, e.getMessage()),
                    StatusManager.SHOW);
        }
    }
}
