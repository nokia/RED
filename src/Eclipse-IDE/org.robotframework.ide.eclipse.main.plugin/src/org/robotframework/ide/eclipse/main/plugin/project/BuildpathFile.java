package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;

public class BuildpathFile {

    private final IProject project;
    private final File environmentFile;
    
    public BuildpathFile(final IProject project) {
        this.project = project;
        environmentFile = RobotFramework.getDefault().getActiveRobotInstallation().getFile();
    }
    
    public void write() throws CoreException {
        final XMLMemento root = XMLMemento.createWriteRoot("buildPaths");

        final IMemento execEnv = root.createChild("robotExecEnvironment");
        if (environmentFile != null) {
            execEnv.putString("path", environmentFile.getAbsolutePath());
        }
        final IFile initFile = project.getFile(RobotProjectNature.BUILDPATH_FILE);
        final IProgressMonitor monitor = new NullProgressMonitor();
        final ByteArrayInputStream source = new ByteArrayInputStream(root.toString().getBytes(Charset.forName("UTF-8")));
        if (initFile.exists()) {
            initFile.setContents(source, true, true, monitor);
        } else {
            initFile.create(source, true, monitor);
        }
    }

}
