package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;

public class JavaLibraryLibdocGenerator implements ILibdocGenerator {

    private final String libName;
    private final String jarPath;
    private final IFile targetSpecFile;

    public JavaLibraryLibdocGenerator(final String libName, final String path, final IFile targetSpecFile) {
        this.libName = libName;
        this.jarPath = path;
        this.targetSpecFile = targetSpecFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment) {
        runtimeEnvironment.createLibdocForJavaLibrary(libName, jarPath, targetSpecFile.getLocation().toFile());
    }

    @Override
    public String getMessage() {
        return "generating libdoc for " + libName + " library contained in " + jarPath;
    }
}
