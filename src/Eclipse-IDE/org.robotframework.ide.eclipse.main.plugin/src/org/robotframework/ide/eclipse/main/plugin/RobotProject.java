package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.robotframework.ide.eclipse.main.plugin.project.BuildpathFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectMetadata;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class RobotProject extends RobotContainer {

    private RobotProjectMetadata metadata;

    RobotProject(final IProject project) {
        super(null, project);
    }

    public IProject getProject() {
        return (IProject) container;
    }

    public String getVersion() {
        readProjectMetadata();
        return metadata == null ? "???" : metadata.getVersion();
    }

    public List<RobotLibrary> getStandardLibraries() {
        readProjectMetadata();
        if (metadata == null) {
            return newArrayList();
        }

        return newArrayList(Iterables.transform(metadata.getStdLibrariesNames(),
                new Function<String, RobotLibrary>() {
                    @Override
                    public RobotLibrary apply(final String libraryName) {
                        return new RobotLibrary(libraryName);
                    }
                }));
    }

    private synchronized RobotProjectMetadata readProjectMetadata() {
        if (metadata == null) {
            metadata = new BuildpathFile(getProject()).read();
        }
        return metadata;
    }
}
