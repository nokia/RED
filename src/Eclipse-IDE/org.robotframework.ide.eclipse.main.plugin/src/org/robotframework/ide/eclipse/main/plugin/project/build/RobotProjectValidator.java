package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.eclipse.main.plugin.FileSectionsParser;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;

class RobotProjectValidator {

    public Job createValidationJob(final IProject project) {
        return new Job("Validating") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    project.accept(new IResourceVisitor() {
                        @Override
                        public boolean visit(final IResource resource) throws CoreException {
                            if (resource.getType() == IResource.FILE && resource.getFileExtension().equals("robot")) {
                                try {
                                    validateRobotFile((IFile) resource, monitor);
                                } catch (final IOException e) {
                                    throw new CoreException(Status.CANCEL_STATUS);
                                }
                            }
                            return true;
                        }

                    });
                } catch (final CoreException e) {
                    e.printStackTrace();
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
    }

    private void validateRobotFile(final IFile file, final IProgressMonitor monitor) throws CoreException, IOException {
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
