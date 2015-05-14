package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public class BuildpathFile {

    private static final String ROOT_NODE = "buildPaths";

    private static final String EXEC_ENVIRONMENT_ROOT = "robotExecEnvironment";
    private static final String VERSION_ATTR = "version";
    private static final String PATH_ATTR = "path";

    private static final String STDLIB_NODE = "standardLibrary";

    private final IProject project;
    
    public BuildpathFile(final IProject project) {
        this.project = project;
    }
    
    public void write(final RobotProjectMetadata metadata) throws CoreException {
        final XMLMemento root = XMLMemento.createWriteRoot(ROOT_NODE);

        final IMemento execEnv = root.createChild(EXEC_ENVIRONMENT_ROOT);
        final File pythonLocation = metadata.getPythonLocation();
        execEnv.putString(PATH_ATTR, pythonLocation == null ? null : pythonLocation.getAbsolutePath());
        execEnv.putString(VERSION_ATTR, metadata.getVersion());

        for (final String libName : metadata.getStdLibrariesNames()) {
            execEnv.createChild(STDLIB_NODE).putTextData(libName);
        }

        writeToFile(root);
    }

    private void writeToFile(final XMLMemento root) throws CoreException {
        final IFile initFile = project.getFile(RobotProjectNature.BUILDPATH_FILE);
        final IProgressMonitor monitor = new NullProgressMonitor();
        final ByteArrayInputStream source = new ByteArrayInputStream(root.toString().getBytes(Charset.forName("UTF-8")));
        if (initFile.exists()) {
            initFile.setContents(source, true, true, monitor);
        } else {
            initFile.create(source, IResource.FORCE | IResource.DERIVED, monitor);
        }
    }

    public RobotProjectMetadata read() {
        XMLMemento rootNode;
        try {
            rootNode = XMLMemento.createReadRoot(new FileReader(project.getFile(RobotProjectNature.BUILDPATH_FILE)
                    .getLocation().toFile()));
            final IMemento[] children = rootNode.getChildren(EXEC_ENVIRONMENT_ROOT);
            if (children.length == 1) {
                final String pathStr = children[0].getString(PATH_ATTR);
                final File path = pathStr == null ? null : new File(pathStr);
                final String version = children[0].getString(VERSION_ATTR);

                final List<String> stdLibs = newArrayList();
                for (final IMemento lib : children[0].getChildren(STDLIB_NODE)) {
                    stdLibs.add(lib.getTextData());
                }
                return new RobotProjectMetadata(path, version, stdLibs);
            }
            return null;
        } catch (WorkbenchException | FileNotFoundException e) {
            return null;
        }
    }
}
