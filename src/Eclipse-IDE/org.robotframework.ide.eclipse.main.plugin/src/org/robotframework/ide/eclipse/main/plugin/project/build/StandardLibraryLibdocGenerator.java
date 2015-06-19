package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;

class StandardLibraryLibdocGenerator implements ILibdocGenerator {

    private final IFile spec;

    StandardLibraryLibdocGenerator(final IFile specFile) {
        this.spec = specFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment) {
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
