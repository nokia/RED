/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.rf.ide.core.libraries.LibrarySpecificationReader;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

public class LibrariesBuilder {

    private final BuildLogger logger;

    public LibrariesBuilder(final BuildLogger logger) {
        this.logger = logger;
    }

    public IFile buildHtmlLibraryDoc(final IFile resourceFile) {
        final RobotModel model = RedPlugin.getModelManager().getModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(resourceFile);

        if (suiteFile.isResourceFile()) {
            final IProject project = resourceFile.getProject();
            final RobotProject robotProject = model.createRobotProject(project);
            final IRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            final String fileName = Files.getNameWithoutExtension(suiteFile.getName()) + "_"
                    + System.currentTimeMillis();
            final IFile htmlTargetFile = LibspecsFolder.get(project).getHtmlSpecFile(fileName);
            runtimeEnvironment.createLibdoc(resourceFile.getLocation().toFile().toString(),
                    htmlTargetFile.getLocation().toFile(), LibdocFormat.HTML, new EnvironmentSearchPaths());
            return htmlTargetFile;
        } else {
            throw new IllegalStateException("Unable to generate HTML documentation file. The file '"
                    + resourceFile.getFullPath().toString() + "' is not a resource file");
        }
    }

    public IFile buildHtmlLibraryDoc(final String projectName, final String library) {
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
        final LibraryDescriptor descriptor = robotProject.getLibraryDescriptorsStream()
                .filter(desc -> desc.getName().equals(library))
                .findFirst()
                .orElse(null);
        return buildHtmlLibraryDoc(robotProject, descriptor);
    }

    public IFile buildHtmlLibraryDoc(final RobotProject robotProject, final LibrarySpecification specification) {
        return buildHtmlLibraryDoc(robotProject, specification.getDescriptor());
    }

    private IFile buildHtmlLibraryDoc(final RobotProject robotProject, final LibraryDescriptor descriptor) {
        if (descriptor != null) {
            final IProject project = robotProject.getProject();
            final String fileName = descriptor.generateLibspecFileName();
            final IFile htmlTargetFile = LibspecsFolder.get(project).getHtmlSpecFile(fileName);

            final Map<String, String> varsMapping = robotProject.getRobotProjectHolder().getVariableMappings();

            final ILibdocGenerator generator = provideGenerator(descriptor, htmlTargetFile, varsMapping,
                    LibdocFormat.HTML);
            generator.generateLibdoc(robotProject.getRuntimeEnvironment(),
                    new RedEclipseProjectConfig(project, robotProject.getRobotProjectConfig())
                            .createAdditionalEnvironmentSearchPaths());
            return htmlTargetFile;
        } else {
            throw new IllegalStateException("Unable to generate HTML documentation file. Missing library descriptor");
        }
    }

    public void rebuildLibraries(final Multimap<IProject, LibrarySpecification> groupedSpecifications,
            final SubMonitor monitor) {
        monitor.subTask("generating libdocs");

        final Multimap<IProject, ILibdocGenerator> groupedGenerators = LinkedHashMultimap.create();
        groupedSpecifications.forEach((project, spec) -> {
            final String fileName = spec.getDescriptor().generateLibspecFileName();
            final IFile xmlTargetFile = LibspecsFolder.get(project).getXmlSpecFile(fileName);

            final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
            final Map<String, String> varsMapping = robotProject.getRobotProjectHolder().getVariableMappings();

            groupedGenerators.put(project,
                    provideGenerator(spec.getDescriptor(), xmlTargetFile, varsMapping, LibdocFormat.XML));
        });

        monitor.setWorkRemaining(groupedGenerators.size());
        for (final IProject project : groupedGenerators.keySet()) {
            if (monitor.isCanceled()) {
                return;
            }

            final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
            final IRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();

            MultiStatus multiStatus = null;
            for (final ILibdocGenerator generator : groupedGenerators.get(project)) {
                if (monitor.isCanceled()) {
                    return;
                }
                monitor.subTask(generator.getMessage());

                try {
                    if (project.exists()) {
                        generator.generateLibdoc(runtimeEnvironment,
                                new RedEclipseProjectConfig(project, robotProject.getRobotProjectConfig())
                                        .createAdditionalEnvironmentSearchPaths());
                    }
                } catch (final RuntimeEnvironmentException e) {
                    final Status status = new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "\nProblem occurred during " + generator.getMessage() + ".", e);
                    if (multiStatus == null) {
                        multiStatus = new MultiStatus(RedPlugin.PLUGIN_ID, IStatus.ERROR, new Status[] { status },
                                "Library specification generation problem", null);
                    } else {
                        multiStatus.add(status);
                    }

                    try {
                        generator.getTargetFile().delete(true, new NullProgressMonitor());
                    } catch (final CoreException e1) {
                        multiStatus.add(e1.getStatus());
                    }
                }
                monitor.worked(1);
            }

            if (multiStatus != null) {
                StatusManager.getManager().handle(multiStatus, StatusManager.BLOCK);
            }
        }
        monitor.done();
    }

    private ILibdocGenerator provideGenerator(final LibraryDescriptor libraryDescriptor, final IFile targetFile,
            final Map<String, String> varsMapping, final LibdocFormat format) {

        final List<String> resolvedArguments = libraryDescriptor.getArguments()
                .stream()
                .map(val -> RobotExpressions.resolve(varsMapping, val))
                .collect(toList());

        if (libraryDescriptor.isStandardLibrary()) {
            return new StandardLibraryLibdocGenerator(libraryDescriptor.getName(), resolvedArguments, targetFile,
                    format);

        } else {
            final String path = libraryDescriptor.getPath();

            final LibraryType type = libraryDescriptor.getLibraryType();
            if (type == LibraryType.VIRTUAL) {
                return new VirtualLibraryLibdocGenerator(Path.fromPortableString(path), targetFile, format);

            } else if (type == LibraryType.PYTHON) {
                return new PythonLibraryLibdocGenerator(libraryDescriptor.getName(), resolvedArguments,
                        toAbsolute(path), targetFile, format);

            } else if (type == LibraryType.JAVA) {
                return new JavaLibraryLibdocGenerator(libraryDescriptor.getName(), resolvedArguments, toAbsolute(path),
                        targetFile, format);
            }
            throw new IllegalStateException("Unknown library type: " + type);
        }
    }

    public void buildLibraries(final RobotProject robotProject, final IRuntimeEnvironment environment,
            final RobotProjectConfig configuration, final SubMonitor monitor) {
        logger.log("BUILDING: generating library docs");
        monitor.subTask("generating libdocs");

        final Map<String, String> varsMapping = robotProject.getRobotProjectHolder().getVariableMappings();

        final List<ILibdocGenerator> libdocGenerators = new ArrayList<>();

        final LibspecsFolder libspecsFolder = LibspecsFolder.get(robotProject.getProject());
        libdocGenerators.addAll(getStandardLibrariesToRecreate(environment, libspecsFolder));
        libdocGenerators.addAll(getStandardRemoteLibrariesToRecreate(configuration, libspecsFolder, varsMapping));
        libdocGenerators.addAll(getReferencedVirtualLibrariesToRecreate(configuration, libspecsFolder));
        libdocGenerators.addAll(getReferencedPythonLibrariesToRecreate(configuration, libspecsFolder, varsMapping));
        if (environment.getInterpreter() == SuiteExecutor.Jython) {
            libdocGenerators.addAll(getReferencedJavaLibrariesToRecreate(configuration, libspecsFolder, varsMapping));
        }

        monitor.setWorkRemaining(libdocGenerators.size());

        for (final ILibdocGenerator generator : libdocGenerators) {
            if (monitor.isCanceled()) {
                return;
            }

            logger.log("BUILDING: " + generator.getMessage());
            monitor.subTask(generator.getMessage());
            try {
                final EnvironmentSearchPaths additionalSearchPaths = new RedEclipseProjectConfig(
                        robotProject.getProject(), configuration).createAdditionalEnvironmentSearchPaths();
                generator.generateLibdoc(environment, additionalSearchPaths);

            } catch (final RuntimeEnvironmentException e) {
                // the libraries with missing libspec are reported in validation phase
            }
            monitor.worked(1);
        }

        monitor.done();
    }

    private List<ILibdocGenerator> getStandardLibrariesToRecreate(final IRuntimeEnvironment environment,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = new ArrayList<>();

        for (final String stdLib : environment.getStandardLibrariesNames()) {
            final String fileName = LibraryDescriptor.ofStandardLibrary(stdLib).generateLibspecFileName();

            final IFile xmlSpecFile = libspecsFolder.getXmlSpecFile(fileName);
            if (!fileExist(xmlSpecFile) || !hasSameVersion(xmlSpecFile, environment.getVersion())) {
                // we always want to regenerate standard libraries when RF version have changed
                // or libdoc does not exist
                generators.add(
                        new StandardLibraryLibdocGenerator(stdLib, new ArrayList<>(), xmlSpecFile, LibdocFormat.XML));
            }
        }
        return generators;
    }

    private List<ILibdocGenerator> getStandardRemoteLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder, final Map<String, String> varsMapping) {

        final List<ILibdocGenerator> generators = new ArrayList<>();

        for (final RemoteLocation location : configuration.getRemoteLocations()) {
            final String fileName = LibraryDescriptor.ofStandardRemoteLibrary(location).generateLibspecFileName();

            // we always want to regenerate remote libraries, as something may have changed
            final IFile xmlSpecFile = libspecsFolder.getXmlSpecFile(fileName);
            final List<String> resolvedArguments = newArrayList(
                    RobotExpressions.resolve(varsMapping, location.getUri()));
            generators.add(
                    new StandardLibraryLibdocGenerator("Remote", resolvedArguments, xmlSpecFile, LibdocFormat.XML));
        }
        return generators;
    }

    private List<ILibdocGenerator> getReferencedVirtualLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = new ArrayList<>();

        configuration.getReferencedLibraries()
                .stream()
                .filter(lib -> lib.provideType() == LibraryType.VIRTUAL)
                .forEach(lib -> {
                    lib.getArgsVariantsStream().forEach(argsVariant -> {
                        final Path libPath = new Path(lib.getPath());

                        // we only copy workspace-external specs to libspecs folder; those in
                        // workspace should be read directly
                        if (libPath.isAbsolute()) {
                            final String fileName = LibraryDescriptor.ofReferencedLibrary(lib, argsVariant)
                                    .generateLibspecFileName();

                            final IFile xmlSpecFile = libspecsFolder.getXmlSpecFile(fileName);
                            if (!fileExist(xmlSpecFile)) {
                                generators
                                        .add(new VirtualLibraryLibdocGenerator(libPath, xmlSpecFile, LibdocFormat.XML));
                            }
                        }
                    });
                });
        return generators;
    }

    private List<ILibdocGenerator> getReferencedPythonLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder, final Map<String, String> varsMapping) {
        final List<ILibdocGenerator> generators = new ArrayList<>();

        configuration.getReferencedLibraries()
                .stream()
                .filter(lib -> lib.provideType() == LibraryType.PYTHON)
                .forEach(lib -> {
                    lib.getArgsVariantsStream().forEach(argsVariant -> {
                        final String fileName = LibraryDescriptor.ofReferencedLibrary(lib, argsVariant)
                                .generateLibspecFileName();

                        final IFile xmlSpecFile = libspecsFolder.getXmlSpecFile(fileName);
                        if (!fileExist(xmlSpecFile)) {
                            final List<String> resolvedArguments = argsVariant.getArgsStream()
                                    .map(val -> RobotExpressions.resolve(varsMapping, val))
                                    .collect(toList());
                            generators.add(new PythonLibraryLibdocGenerator(lib.getName(), resolvedArguments,
                                    toAbsolute(lib.getPath()), xmlSpecFile, LibdocFormat.XML));
                        }
                    });
                });
        return generators;
    }

    private List<ILibdocGenerator> getReferencedJavaLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder, final Map<String, String> varsMapping) {
        final List<ILibdocGenerator> generators = new ArrayList<>();

        configuration.getReferencedLibraries()
                .stream()
                .filter(lib -> lib.provideType() == LibraryType.JAVA)
                .forEach(lib -> {
                    lib.getArgsVariantsStream().forEach(argsVariant -> {
                        final String fileName = LibraryDescriptor.ofReferencedLibrary(lib, argsVariant)
                                .generateLibspecFileName();

                        final IFile xmlSpecFile = libspecsFolder.getXmlSpecFile(fileName);
                        if (!fileExist(xmlSpecFile)) {
                            final List<String> resolvedArguments = argsVariant.getArgsStream()
                                    .map(val -> RobotExpressions.resolve(varsMapping, val))
                                    .collect(toList());
                            generators.add(new JavaLibraryLibdocGenerator(lib.getName(), resolvedArguments,
                                    toAbsolute(lib.getPath()), xmlSpecFile, LibdocFormat.XML));
                        }
                    });
                });
        return generators;
    }

    private static boolean fileExist(final IFile file) {
        return file.exists() && file.getLocation().toFile().exists();
    }

    private static boolean hasSameVersion(final IFile file, final String version) {
        final String fileVersion = RedWorkspace.getLocalFile(file)
                .flatMap(LibrarySpecificationReader::readSpecification)
                .map(LibrarySpecification::getVersion)
                .orElse("unknown");
        return version.startsWith(String.format("Robot Framework %s (", fileVersion));
    }

    private static String toAbsolute(final String path) {
        return RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(Path.fromPortableString(path)).toOSString();
    }
}
