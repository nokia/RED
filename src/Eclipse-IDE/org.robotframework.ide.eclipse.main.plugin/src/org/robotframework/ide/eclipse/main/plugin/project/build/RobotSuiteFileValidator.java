package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.FileSectionsParser;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;

class RobotSuiteFileValidator {

    private final IFile file;

    RobotSuiteFileValidator(final IFile file) {
        this.file = file;
    }

    void validate(final IProgressMonitor monitor) throws CoreException, IOException {
        final List<RobotElement> sections = new FileSectionsParser(file).parseRobotFileSections(null);
        for (final RobotElement section : sections) {
            validateSection(file, (RobotSuiteFileSection) section);
        }
    }

    private void validateSection(final IFile file, final RobotSuiteFileSection section) throws CoreException {
        if (!Arrays.asList("Settings", "Variables", "Test Cases", "Keywords").contains(section.getName())) {
            final int lineNumber = 1;

            final IMarker marker = file.createMarker(RobotProblem.TYPE_ID);

            marker.setAttribute(IMarker.MESSAGE, "Unrecognized section name '" + section.getName() + "'");
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            marker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
        }

        // duplicated section
    }
}
