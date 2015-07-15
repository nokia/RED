package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;

class RemoteLibraryLibdocGenerator implements ILibdocGenerator {

    private final IFile spec;
    private final String address;
    private final int port;

    RemoteLibraryLibdocGenerator(final String address, final int port, final IFile spec) {
        this.spec = spec;
        this.address = address;
        this.port = port;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment) {
        final String libName = "Remote::" + address + ":" + port;
        runtimeEnvironment.createLibdocForStdLibrary(libName, spec.getLocation().toFile());

    }

    @Override
    public String getMessage() {
        return "generating libdoc for remote library located at " + address + ":" + port;
    }

}
