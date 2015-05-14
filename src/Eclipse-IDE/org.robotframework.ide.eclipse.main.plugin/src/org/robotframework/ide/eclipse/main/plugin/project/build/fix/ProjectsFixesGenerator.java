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
        final String causeStr = marker.getAttribute(RobotProblem.CAUSE_ATTRIBUTE, null);
        if (causeStr != null) {
            final Cause cause = Cause.valueOf(causeStr);
            return cause.hasResolution();
        }
        return false;
    }

    @Override
    public IMarkerResolution[] getResolutions(final IMarker marker) {
        final List<IMarkerResolution> resolutions = newArrayList();

        final Cause problemCause = Cause.valueOf(marker.getAttribute(RobotProblem.CAUSE_ATTRIBUTE, ""));
        resolutions.addAll(problemCause.createFixer());

        return resolutions.toArray(new IMarkerResolution[0]);
    }

}
