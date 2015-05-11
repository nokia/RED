package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class ProjectsFixesGenerator implements IMarkerResolutionGenerator2 {

    @Override
    public boolean hasResolutions(final IMarker marker) {
        return marker.getAttribute("isRobotProblem", false);
    }

    @Override
    public IMarkerResolution[] getResolutions(final IMarker marker) {
        if (marker.getResource() instanceof IProject) {
            return new IMarkerResolution[] { new BuildpathFileFixer() };
        }
        return new IMarkerResolution[0];
    }

}
