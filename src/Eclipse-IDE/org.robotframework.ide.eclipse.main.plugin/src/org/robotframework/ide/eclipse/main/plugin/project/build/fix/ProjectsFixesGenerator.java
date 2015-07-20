package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

public class ProjectsFixesGenerator implements IMarkerResolutionGenerator2 {

    @Override
    public boolean hasResolutions(final IMarker marker) {
        final IProblemCause cause = getCause(marker);
        if (cause != null) {
            return cause.hasResolution();
        }
        return false;
    }

    @Override
    public IMarkerResolution[] getResolutions(final IMarker marker) {
        final List<IMarkerResolution> resolutions = newArrayList();

        final IProblemCause problemCause = getCause(marker);
        if (problemCause != null) {
            resolutions.addAll(problemCause.createFixers());
        }
        return resolutions.toArray(new IMarkerResolution[0]);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IProblemCause getCause(final IMarker marker) {
        final String causeEnumClass = marker.getAttribute(RobotProblem.CAUSE_ENUM_CLASS, null);
        final String causeStr = marker.getAttribute(RobotProblem.CAUSE_ATTRIBUTE, null);
        if (causeEnumClass != null && causeStr != null) {
            try {
                return (IProblemCause) Enum.valueOf((Class<? extends Enum>) Class.forName(causeEnumClass), causeStr);
            } catch (final ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }
}
