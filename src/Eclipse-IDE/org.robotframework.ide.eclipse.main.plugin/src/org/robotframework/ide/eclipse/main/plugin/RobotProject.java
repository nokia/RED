package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.project.BuildpathFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectMetadata;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecificationReader;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class RobotProject extends RobotContainer {

    private RobotProjectMetadata metadata;
    private List<LibrarySpecification> librariesSpecs;

    RobotProject(final IProject project) {
        super(null, project);
    }

    public IProject getProject() {
        return (IProject) container;
    }

    public String getVersion() {
        readProjectMetadataIfNeeded();
        return metadata == null ? "???" : metadata.getVersion();
    }

    public List<LibrarySpecification> getStandardLibraries() {
        readProjectMetadataIfNeeded();
        if (metadata == null) {
            return newArrayList();
        }
        if (librariesSpecs == null) {
            librariesSpecs = newArrayList(Iterables.transform(metadata.getStdLibrariesNames(),
                    new Function<String, LibrarySpecification>() {
                        @Override
                        public LibrarySpecification apply(final String libraryName) {
                            final IFile file = getProject().getFolder("libspecs").getFile(
                                    libraryName.toLowerCase() + ".libspec");
                            return LibrarySpecificationReader.readSpecification(RobotProject.this, file);
                        }
                    }));
        }
        return librariesSpecs;
    }

    private synchronized RobotProjectMetadata readProjectMetadataIfNeeded() {
        if (metadata == null) {
            metadata = new BuildpathFile(getProject()).read();
        }
        return metadata;
    }

    public void clearMetadata() {
        metadata = null;
        librariesSpecs = null;
    }

    public RobotRuntimeEnvironment getRuntimeEnvironment() {
        readProjectMetadataIfNeeded();
        if (metadata == null || metadata.getPythonLocation() == null) {
            return RobotFramework.getDefault().getActiveRobotInstallation();
        }
        return RobotRuntimeEnvironment.create(metadata.getPythonLocation());
    }
}
