package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfiguration;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigurationFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigurationFile.InvalidConfigurationFileException;
import org.robotframework.ide.eclipse.main.plugin.project.build.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecificationReader;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecificationReader.CannotReadlibrarySpecificationException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class RobotProject extends RobotContainer {

    private List<LibrarySpecification> librariesSpecs;
    private RobotProjectConfiguration configuration;

    RobotProject(final IProject project) {
        super(null, project);
    }

    public IProject getProject() {
        return (IProject) container;
    }

    public String getVersion() {
        readProjectConfigurationIfNeeded();
        final RobotRuntimeEnvironment env = getRuntimeEnvironment();
        return env == null ? "???" : env.getVersion();
    }

    public List<LibrarySpecification> getStandardLibraries() {
        readProjectConfigurationIfNeeded();
        final RobotRuntimeEnvironment env = getRuntimeEnvironment();
        if (env == null) {
            return newArrayList();
        } else if (librariesSpecs != null) {
            return librariesSpecs;
        }

        try {
            librariesSpecs = newArrayList(Iterables.transform(env.getStandardLibrariesNames(),
                    new Function<String, LibrarySpecification>() {
                        @Override
                        public LibrarySpecification apply(final String libraryName) {
                            final IFile file = LibspecsFolder.get(getProject()).getSpecFile(libraryName);
                            return LibrarySpecificationReader.readSpecification(RobotProject.this, file);
                        }
                    }));
            return librariesSpecs;
        } catch (final CannotReadlibrarySpecificationException e) {
            return newArrayList();
        }
    }

    private synchronized RobotProjectConfiguration readProjectConfigurationIfNeeded() {
        if (configuration == null) {
            try {
                configuration = new RobotProjectConfigurationFile(getProject()).read();
            } catch (final InvalidConfigurationFileException e) {
                // oh well...
            }
        }
        return configuration;
    }

    public void clear() {
        configuration = null;
        librariesSpecs = null;
    }

    public RobotRuntimeEnvironment getRuntimeEnvironment() {
        readProjectConfigurationIfNeeded();
        if (configuration == null || configuration.getPythonLocation() == null) {
            return RobotFramework.getDefault().getActiveRobotInstallation();
        }
        return RobotFramework.getDefault().getRobotInstallation(configuration.getPythonLocation());
    }

    public IFile getConfigurationFile() {
        return getProject().getFile(RobotProjectConfigurationFile.FILENAME);
    }

    public IFile getFile(final String filename) {
        return getProject().getFile(filename);
    }
}
