/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProjectConfigurationProblem;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Objects;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class LibrariesBuilder {

    private final BuildLogger logger;

    public LibrariesBuilder(final BuildLogger logger) {
        this.logger = logger;
    }

    public void forceLibrariesRebuild(final Multimap<IProject, LibrarySpecification> groupedSpecifications,
            final SubMonitor monitor) {
        monitor.subTask("generating libdocs");
        final Multimap<IProject, GeneratorWithSource> groupedGenerators = LinkedHashMultimap.create();
        for (final IProject project : groupedSpecifications.keySet()) {
            for (final LibrarySpecification specification : groupedSpecifications.get(project)) {
                final GeneratorWithSource generatorWithSource = new GeneratorWithSource(specification.getSourceFile(),
                        provideGenerator(specification));
                groupedGenerators.put(project, generatorWithSource);
            }
        }

        monitor.setWorkRemaining(groupedGenerators.size());
        for (final IProject project : groupedGenerators.keySet()) {
            final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();

            for (final GeneratorWithSource generatorWithSource : groupedGenerators.get(project)) {
                monitor.subTask(generatorWithSource.generator.getMessage());
                try {
                    if (project.exists()) {
                        generatorWithSource.generator.generateLibdocForcibly(runtimeEnvironment,
                                new RedEclipseProjectConfig(robotProject.getRobotProjectConfig())
                                        .createEnvironmentSearchPaths(project));
                    }
                } catch (final RobotEnvironmentException e) {
                    final IPath libspecFileLocation = generatorWithSource.sourceLibdocFile.getLocation();
                    if (libspecFileLocation != null) {
                        libspecFileLocation.toFile().delete();
                    }
                    throw e;
                }
                monitor.worked(1);
            }
        }
        monitor.done();
    }

    private ILibdocGenerator provideGenerator(final LibrarySpecification specification) {
        final IFile libspecSourceFile = specification.getSourceFile();

        if (!specification.isReferenced() && !specification.isRemote()) {
            return new StandardLibraryLibdocGenerator(libspecSourceFile);
        } else if (!specification.isReferenced()) {
            return new RemoteLibraryLibdocGenerator(specification.getRemoteLocation().getUriAddress(),
                    libspecSourceFile);
        } else {
            specification.setIsModified(false);
            final String path = specification.getSecondaryKey();
            final ReferencedLibrary refLib = specification.getReferencedLibrary();
            final LibraryType type = refLib.provideType();
            if (type == LibraryType.VIRTUAL) {
                return new VirtualLibraryLibdocGenerator(Path.fromPortableString(path), libspecSourceFile);
            } else if (type == LibraryType.PYTHON) {
                final String libPath = RedWorkspace.Paths
                        .toAbsoluteFromWorkspaceRelativeIfPossible(Path.fromPortableString(refLib.getPath()))
                        .toOSString();
                return new PythonLibraryLibdocGenerator(refLib.getName(), libPath, libspecSourceFile);
            } else if (type == LibraryType.JAVA) {
                final String libPath = RedWorkspace.Paths
                        .toAbsoluteFromWorkspaceRelativeIfPossible(Path.fromPortableString(path))
                        .toOSString();
                return new JavaLibraryLibdocGenerator(specification.getName(), libPath, libspecSourceFile);
            }
            throw new IllegalStateException("Unknown library type: " + type);
        }
    }

    public void buildLibraries(final RobotProject robotProject, final RobotRuntimeEnvironment runtimeEnvironment,
            final RobotProjectConfig configuration, final SubMonitor monitor,
            final ProblemsReportingStrategy reporter) {
        logger.log("BUILDING: generating library docs");
        monitor.subTask("generating libdocs");

        final List<ILibdocGenerator> libdocGenerators = new ArrayList<>();

        final LibspecsFolder libspecsFolder = LibspecsFolder.get(robotProject.getProject());
        libdocGenerators.addAll(getStandardLibrariesToRecreate(runtimeEnvironment, libspecsFolder));
        libdocGenerators.addAll(getReferencedVirtualLibrariesToRecreate(configuration, libspecsFolder));
        libdocGenerators.addAll(getReferencedPythonLibrariesToRecreate(configuration, libspecsFolder));
        if (runtimeEnvironment.getInterpreter() == SuiteExecutor.Jython) {
            libdocGenerators.addAll(getReferencedJavaLibrariesToRecreate(configuration, libspecsFolder));
        }
        libdocGenerators.addAll(getRemoteLibrariesToRecreate(configuration, libspecsFolder));

        monitor.setWorkRemaining(libdocGenerators.size());

        for (final ILibdocGenerator generator : libdocGenerators) {
            if (monitor.isCanceled()) {
                return;
            }

            logger.log("BUILDING: " + generator.getMessage());
            monitor.subTask(generator.getMessage());
            try {
                generator.generateLibdoc(runtimeEnvironment, new RedEclipseProjectConfig(configuration)
                        .createEnvironmentSearchPaths(robotProject.getProject()));
            } catch (final RobotEnvironmentException e) {
                final RobotProblem problem = RobotProblem
                        .causedBy(ProjectConfigurationProblem.LIBRARY_SPEC_CANNOT_BE_GENERATED)
                        .formatMessageWith(e.getMessage());
                reporter.handleProblem(problem, robotProject.getFile(".project"), 1);
            }
            monitor.worked(1);
        }

        monitor.done();
    }

    private List<ILibdocGenerator> getStandardLibrariesToRecreate(final RobotRuntimeEnvironment runtimeEnvironment,
            final LibspecsFolder libspecsFolder) {
        final List<String> stdLibs = runtimeEnvironment.getStandardLibrariesNames();
        final List<IFile> toRecr = libspecsFolder.collectSpecsWithDifferentVersion(stdLibs,
                runtimeEnvironment.getVersion());
        return toRecr.stream().map(StandardLibraryLibdocGenerator::new).collect(toList());
    }

    private List<ILibdocGenerator> getReferencedVirtualLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = new ArrayList<>();

        for (final ReferencedLibrary lib : configuration.getLibraries()) {
            if (lib.provideType() == LibraryType.VIRTUAL) {
                final Path libPath = new Path(lib.getPath());
                if (libPath.isAbsolute()) {
                    final String libName = lib.getName();
                    final IFile specFile = libspecsFolder.getSpecFile(libName);
                    if (!specFile.exists()) {
                        generators.add(new VirtualLibraryLibdocGenerator(libPath, specFile));
                    }
                }
            }
        }
        return generators;
    }

    private List<ILibdocGenerator> getReferencedPythonLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = new ArrayList<>();

        for (final ReferencedLibrary lib : configuration.getLibraries()) {
            if (lib.provideType() == LibraryType.PYTHON) {
                final String libName = lib.getName();
                final IFile specFile = libspecsFolder.getSpecFile(libName);
                if (!specFile.exists()) {
                    final String libPath = RedWorkspace.Paths
                            .toAbsoluteFromWorkspaceRelativeIfPossible(Path.fromPortableString(lib.getPath()))
                            .toOSString();
                    generators.add(new PythonLibraryLibdocGenerator(libName, libPath, specFile));
                }
            }
        }
        return generators;
    }

    private List<ILibdocGenerator> getReferencedJavaLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = new ArrayList<>();

        for (final ReferencedLibrary lib : configuration.getLibraries()) {
            if (lib.provideType() == LibraryType.JAVA) {
                final String libName = lib.getName();
                final IFile specFile = libspecsFolder.getSpecFile(libName);
                if (!specFile.exists()) {
                    final String jarPath = RedWorkspace.Paths
                            .toAbsoluteFromWorkspaceRelativeIfPossible(Path.fromPortableString(lib.getPath()))
                            .toOSString();
                    generators.add(new JavaLibraryLibdocGenerator(libName, jarPath, specFile));
                }
            }
        }
        return generators;
    }

    private Collection<? extends ILibdocGenerator> getRemoteLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = new ArrayList<>();

        for (final RemoteLocation location : configuration.getRemoteLocations()) {
            final IFile specFile = libspecsFolder.getSpecFile(location.createLibspecFileName());

            if (!specFile.exists()) {
                generators.add(new RemoteLibraryLibdocGenerator(location.getUriAddress(), specFile));
            }
        }
        return generators;
    }

    private static final class GeneratorWithSource {

        private final IFile sourceLibdocFile;

        private final ILibdocGenerator generator;

        GeneratorWithSource(final IFile source, final ILibdocGenerator generator) {
            this.sourceLibdocFile = source;
            this.generator = generator;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == GeneratorWithSource.class) {
                final GeneratorWithSource that = (GeneratorWithSource) obj;
                return Objects.equal(this.sourceLibdocFile, that.sourceLibdocFile)
                        && Objects.equal(this.getClass(), that.generator);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(sourceLibdocFile, generator);
        }
    }
}
