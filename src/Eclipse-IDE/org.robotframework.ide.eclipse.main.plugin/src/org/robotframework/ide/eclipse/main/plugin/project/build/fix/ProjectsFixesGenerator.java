package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem.Cause;

public class ProjectsFixesGenerator implements IMarkerResolutionGenerator2 {

    @Override
    public boolean hasResolutions(final IMarker marker) {
        return marker.getAttribute(RobotProblem.CAUSE_ATTRIBUTE, null) != null;
    }

    @Override
    public IMarkerResolution[] getResolutions(final IMarker marker) {
        final List<IMarkerResolution> resolutions = newArrayList();

        final Cause problemCause = Cause.valueOf(marker.getAttribute(RobotProblem.CAUSE_ATTRIBUTE, ""));
        if (Cause.MISSING_BUILDPATHS_FILE == problemCause) {
            resolutions.add(new BuildpathFileFixer());
        }

        return resolutions.toArray(new IMarkerResolution[0]);
    }

}
