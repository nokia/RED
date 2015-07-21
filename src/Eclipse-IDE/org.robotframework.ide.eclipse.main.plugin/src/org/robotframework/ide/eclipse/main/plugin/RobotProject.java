package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.build.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecificationReader;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecificationReader.CannotReadlibrarySpecificationException;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class RobotProject extends RobotContainer {

    private List<LibrarySpecification> stdLibsSpecs;
    private List<LibrarySpecification> refLibsSpecs;
    private RobotProjectConfig configuration;

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

    public synchronized boolean hasStandardLibraries() {
        readProjectConfigurationIfNeeded();
        if (stdLibsSpecs != null && !stdLibsSpecs.isEmpty()) {
            return true;
        }
        return configuration != null;
    }

    public synchronized List<LibrarySpecification> getStandardLibraries() {
        if (stdLibsSpecs != null) {
            return stdLibsSpecs;
        }
        readProjectConfigurationIfNeeded();
        final RobotRuntimeEnvironment env = getRuntimeEnvironment();
        if (env == null || configuration == null) {
            return newArrayList();
        }

        stdLibsSpecs = newArrayList(Iterables.filter(Iterables.transform(configuration.getRemoteLocations(),
                new Function<RemoteLocation, LibrarySpecification>() {
                    @Override
                    public LibrarySpecification apply(final RemoteLocation remoteLocation) {
                        try {
                            final IFile file = LibspecsFolder.get(getProject()).getSpecFile(
                                    remoteLocation.createLibspecFileName());
                            return LibrarySpecificationReader.readRemoteSpecification(file, remoteLocation);
                        } catch (final CannotReadlibrarySpecificationException e) {
                            return null;
                        }
                    }
                }), Predicates.<LibrarySpecification> notNull()));
        stdLibsSpecs.addAll(newArrayList(Iterables.filter(
                Iterables.transform(env.getStandardLibrariesNames(), new Function<String, LibrarySpecification>() {
                    @Override
                    public LibrarySpecification apply(final String libraryName) {
                        try {
                            final IFile file = LibspecsFolder.get(getProject()).getSpecFile(libraryName);
                            return LibrarySpecificationReader.readSpecification(file);
                        } catch (final CannotReadlibrarySpecificationException e) {
                            return null;
                        }
                    }
                }), Predicates.<LibrarySpecification> notNull())));
        return stdLibsSpecs;
    }

    public synchronized boolean hasReferencedLibraries() {
        readProjectConfigurationIfNeeded();
        if (refLibsSpecs != null && !refLibsSpecs.isEmpty()) {
            return true;
        }
        return configuration != null && configuration.hasReferencedLibraries();
    }

    public synchronized List<LibrarySpecification> getReferencedLibraries() {
        if (refLibsSpecs != null) {
            return refLibsSpecs;
        }
        readProjectConfigurationIfNeeded();
        if (configuration == null) {
            return newArrayList();
        }

        refLibsSpecs = newArrayList(Iterables.filter(
                Iterables.transform(configuration.getLibraries(),
                new Function<ReferencedLibrary, LibrarySpecification>() {
                    @Override
                    public LibrarySpecification apply(final ReferencedLibrary lib) {
                        try {
                            if (lib.provideType() == LibraryType.VIRTUAL) {
                                final IPath path = Path.fromPortableString(lib.getPath());
                                final IResource libspec = getProject().getParent().findMember(path);
                                if (libspec != null && libspec.getType() == IResource.FILE) {
                                    return LibrarySpecificationReader.readSpecification((IFile) libspec);
                                }
                                return null;
                            } else if (lib.provideType() == LibraryType.JAVA || lib.provideType() == LibraryType.PYTHON) {
                                final IFile file = LibspecsFolder.get(getProject()).getSpecFile(lib.getName());
                                return LibrarySpecificationReader.readSpecification(file);
                            } else {
                                return null;
                            }
                        } catch (final CannotReadlibrarySpecificationException e) {
                            return null;
                        }
                    }
                }), Predicates.<LibrarySpecification> notNull()));
        return refLibsSpecs;
    }

    private synchronized RobotProjectConfig readProjectConfigurationIfNeeded() {
        if (configuration == null) {
            try {
                configuration = new RobotProjectConfigReader().readConfiguration(getProject());
            } catch (final CannotReadProjectConfigurationException e) {
                // oh well...
            }
        }
        return configuration;
    }

    public synchronized void clear() {
        configuration = null;
        stdLibsSpecs = null;
        refLibsSpecs = null;
    }

    public synchronized RobotRuntimeEnvironment getRuntimeEnvironment() {
        readProjectConfigurationIfNeeded();
        if (configuration == null || configuration.usesPreferences()) {
            return RobotFramework.getDefault().getActiveRobotInstallation();
        }
        return RobotFramework.getDefault().getRobotInstallation(configuration.providePythonLocation());
    }

    public IFile getConfigurationFile() {
        return getProject().getFile(RobotProjectConfig.FILENAME);
    }

    public IFile getFile(final String filename) {
        return getProject().getFile(filename);
    }
    
    public synchronized List<String> getPythonpath() {
        readProjectConfigurationIfNeeded();
        if (configuration != null) {
            final Set<String> pp = newHashSet();
            for (final ReferencedLibrary lib : configuration.getLibraries()) {
                if (lib.provideType() == LibraryType.PYTHON) {
                    pp.add(lib.getPath());
                }
            }
            return newArrayList(pp);
        }
        return newArrayList();
    }

    public synchronized List<String> getClasspath() {
        readProjectConfigurationIfNeeded();
        if (configuration != null) {
            final Set<String> cp = newHashSet(".");
            for (final ReferencedLibrary lib : configuration.getLibraries()) {
                if (lib.provideType() == LibraryType.JAVA) {
                    cp.add(lib.getPath());
                }
            }
            return newArrayList(cp);
        }
        return newArrayList(".");
    }
}
