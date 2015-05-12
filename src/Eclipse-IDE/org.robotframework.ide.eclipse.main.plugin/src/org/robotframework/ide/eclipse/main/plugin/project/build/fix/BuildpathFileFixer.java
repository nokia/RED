package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.File;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.project.BuildpathFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectMetadata;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;

public class BuildpathFileFixer implements IMarkerResolution {

    @Override
    public String getLabel() {
        return "Create " + RobotProjectNature.BUILDPATH_FILE + " file";
    }

    @Override
    public void run(final IMarker marker) {
        try {
            final File pythonInstallation = RobotFramework.getDefault().getActiveRobotInstallation().getFile();
            new BuildpathFile((IProject) marker.getResource()).write(RobotProjectMetadata.create(pythonInstallation));

            marker.delete();
        } catch (final CoreException e) {
            // FIXME : throw exception maybe, or present error dialog
            e.printStackTrace();
        }
    }

}
