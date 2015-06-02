package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.robotframework.ide.eclipse.main.plugin.RobotProject;

public class RobotProjectConfigurationFile {

    public static final String FILENAME = "red.xml";

    private static final String ROOT_NODE = "projectConfiguration";
    private static final String VERSION_NODE = "configVersion";

    private static final String EXEC_ENVIRONMENT_NODE = "robotExecEnvironment";
    private static final String PATH_ATTR = "path";

    private final IProject project;
    
    public RobotProjectConfigurationFile(final IProject project) {
        this.project = project;
    }
    
    public RobotProjectConfigurationFile(final RobotProject robotProject) {
        this.project = robotProject.getProject();
    }

    public void write(final RobotProjectConfiguration configuration) throws CoreException {
        final XMLMemento root = XMLMemento.createWriteRoot(ROOT_NODE);

        final IMemento version = root.createChild(VERSION_NODE);
        version.putTextData(configuration.getVersion());

        final File pythonLocation = configuration.getPythonLocation();
        if (pythonLocation != null) {
            final IMemento env = root.createChild(EXEC_ENVIRONMENT_NODE);
            env.putString(PATH_ATTR, pythonLocation.getAbsolutePath());
        }

        writeToFile(root);
    }

    private void writeToFile(final XMLMemento root) throws CoreException {
        final IFile initFile = project.getFile(FILENAME);
        final IProgressMonitor monitor = new NullProgressMonitor();
        final ByteArrayInputStream source = new ByteArrayInputStream(root.toString().getBytes(Charset.forName("UTF-8")));
        if (initFile.exists()) {
            initFile.setContents(source, true, true, monitor);
        } else {
            initFile.create(source, IResource.FORCE, monitor);
        }
    }

    public RobotProjectConfiguration read() {
        XMLMemento rootNode;
        try {
            rootNode = XMLMemento.createReadRoot(new FileReader(project.getFile(FILENAME).getLocation().toFile()));
            final IMemento[] versionChild = rootNode.getChildren(VERSION_NODE);
            final IMemento[] envChild = rootNode.getChildren(EXEC_ENVIRONMENT_NODE);

            if (versionChild.length != 1) {
                throw new InvalidConfigurationFileException("There should be only one " + VERSION_NODE
                        + " node defined");
            } else if (envChild.length > 1) {
                throw new InvalidConfigurationFileException("There should at most one " + EXEC_ENVIRONMENT_NODE
                        + " node defined");
            }

            final String version = versionChild[0].getTextData();
            final String pathStr = envChild.length == 0 ? null : envChild[0].getString(PATH_ATTR);
            final File path = pathStr == null ? null : new File(pathStr);

            return RobotProjectConfiguration.create(version, path);
        } catch (WorkbenchException | FileNotFoundException e) {
            throw new InvalidConfigurationFileException("Problem reading configuraion file", e);
        }
    }

    public static class InvalidConfigurationFileException extends RuntimeException {
        public InvalidConfigurationFileException(final String message) {
            super(message);
        }

        public InvalidConfigurationFileException(final String message, final Exception cause) {
            super(message, cause);
        }
    }
}
