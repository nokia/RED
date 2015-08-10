package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProjectConfigurationProblem;

public class LibrariesBuilder {

    public void buildLibraries(final RobotProject robotProject, final RobotRuntimeEnvironment runtimeEnvironment,
            final RobotProjectConfig configuration, final SubMonitor monitor) {
        monitor.subTask("generating libdocs");

        final List<ILibdocGenerator> libdocGenerators = newArrayList();

        final LibspecsFolder libspecsFolder = LibspecsFolder.get(robotProject.getProject());
        libdocGenerators.addAll(getStandardLibrariesToRecreate(runtimeEnvironment, libspecsFolder));
        libdocGenerators.addAll(getReferencedPythonLibrariesToRecreate(configuration, libspecsFolder));
        libdocGenerators.addAll(getReferencedJavaLibrariesToRecreate(configuration, libspecsFolder));
        libdocGenerators.addAll(getRemoteLibrariesToRecreate(configuration, libspecsFolder));

        monitor.setWorkRemaining(libdocGenerators.size());
        
        for (final ILibdocGenerator generator : libdocGenerators) {
            if (monitor.isCanceled()) {
                return;
            }
            monitor.subTask(generator.getMessage());
            try {
                generator.generateLibdoc(runtimeEnvironment);
            } catch (final RobotEnvironmentException e) {
                final RobotProblem problem = RobotProblem.causedBy(
                        ProjectConfigurationProblem.LIBRARY_SPEC_CANNOT_BE_GENERATED).formatMessageWith(e.getMessage());
                new ProblemsReportingStrategy().handleProblem(problem, robotProject.getFile(".project"), 1);
            }
            monitor.worked(1);
        }
        monitor.done();
    }

    private List<ILibdocGenerator> getStandardLibrariesToRecreate(final RobotRuntimeEnvironment runtimeEnvironment,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = newArrayList();
        final List<String> stdLibs = runtimeEnvironment.getStandardLibrariesNames();
        try {
            final List<IFile> toRecr = libspecsFolder.collectSpecsWithDifferentVersion(stdLibs,
                    runtimeEnvironment.getVersion());
            for (final IFile specToRecreate : toRecr) {
                generators.add(new StandardLibraryLibdocGenerator(specToRecreate));
            }
        } catch (final CoreException e) {
            // FIXME : handle this
            e.printStackTrace();
        }
        return generators;
    }

    private List<ILibdocGenerator> getReferencedPythonLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = newArrayList();

        for (final ReferencedLibrary lib : configuration.getLibraries()) {
            if (lib.provideType() == LibraryType.PYTHON) {
                final String libName = lib.getName();
                final IFile specFile = libspecsFolder.getSpecFile(libName);
                if (!specFile.exists()) {
                    final String libPath = lib.getPath();
                    generators.add(new PythonLibraryLibdocGenerator(libName, libPath, specFile));
                }
            }
        }
        return generators;
    }

    private List<ILibdocGenerator> getReferencedJavaLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = newArrayList();

        for (final ReferencedLibrary lib : configuration.getLibraries()) {
            if (lib.provideType() == LibraryType.JAVA) {
                final String libName = lib.getName();
                final IFile specFile = libspecsFolder.getSpecFile(libName);
                if (!specFile.exists()) {
                    final String jarPath = lib.getPath();
                    generators.add(new JavaLibraryLibdocGenerator(libName, jarPath, specFile));
                }
            }
        }
        return generators;
    }

    private Collection<? extends ILibdocGenerator> getRemoteLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = newArrayList();

        for (final RemoteLocation location : configuration.getRemoteLocations()) {
            final IFile specFile = libspecsFolder.getSpecFile(location.createLibspecFileName());

            if (!specFile.exists()) {
                generators.add(new RemoteLibraryLibdocGenerator(location.getUriAddress(), specFile));
            }
        }
        return generators;
    }
}
