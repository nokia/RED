package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

class StandardLibraryLibdocGenerator implements ILibdocGenerator {

    private final IFile spec;

    StandardLibraryLibdocGenerator(final IFile specFile) {
        this.spec = specFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment) throws RobotEnvironmentException {
        runtimeEnvironment.createLibdocForStdLibrary(getLibraryName(), spec.getLocation().toFile());
    }

    @Override
    public String getMessage() {
        return "generating libdoc for " + getLibraryName() + " library";
    }

    private String getLibraryName() {
        return spec.getLocation().removeFileExtension().lastSegment();
    }
}
