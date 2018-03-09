/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.executor.RedURI;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.GeneralSettingsLibrariesImportValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ILibraryClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ILibraryStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.IReferencedLibraryImporter;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryLocator;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryLocator.IReferencedLibraryDetector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.LibraryImportCollector;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

class ExternalLibrariesImportCollector {

    private final Set<String> standardLibraryNames;

    private final ReferencedLibraryLocator libraryLocator;

    private final Set<RobotDryRunLibraryImport> libraryImports = new HashSet<>();

    private final Multimap<RobotDryRunLibraryImport, RobotSuiteFile> libraryImporters = LinkedListMultimap.create();

    private final Multimap<String, RobotSuiteFile> unknownLibraryNames = LinkedListMultimap.create();

    private final Multimap<String, RobotDryRunLibraryImport> knownLibraryNames = LinkedListMultimap.create();

    private RobotSuiteFile currentSuite;

    ExternalLibrariesImportCollector(final RobotProject robotProject) {
        this.standardLibraryNames = robotProject.getLibraryDescriptorsStream()
                .filter(LibraryDescriptor::isStandardLibrary)
                .map(LibraryDescriptor::getName)
                .collect(toSet());
        this.libraryLocator = new ReferencedLibraryLocator(robotProject, new DiscoveringLibraryImporter(),
                new DiscoveringLibraryDetector());
    }

    void collectFromSuites(final Collection<RobotSuiteFile> suites, final IProgressMonitor monitor)
            throws CoreException {
        final SubMonitor subMonitor = SubMonitor.convert(monitor);
        subMonitor.setWorkRemaining(suites.size());

        for (final RobotSuiteFile suite : suites) {
            subMonitor.subTask("Finding libraries in suite: " + suite.getName());
            final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                    .collectLibraryImportsIncludingNestedResources(suite);
            for (final Entry<RobotSuiteFile, List<LibraryImport>> entry : imports.entrySet()) {
                findLibrariesInSuite(entry.getKey(), entry.getValue());
            }
            subMonitor.worked(1);
            if (monitor.isCanceled()) {
                return;
            }
        }
    }

    private void findLibrariesInSuite(final RobotSuiteFile currentSuite, final List<LibraryImport> imports)
            throws CoreException {
        this.currentSuite = currentSuite;

        final ValidationContext generalContext = new ValidationContext(currentSuite.getProject(), new BuildLogger());
        final FileValidationContext fileContext = new FileValidationContext(generalContext, currentSuite.getFile());
        final DiscoveringImportValidator importsValidator = new DiscoveringImportValidator(fileContext, imports,
                new DiscoveringReportingStrategy());
        importsValidator.validate(new NullProgressMonitor());
    }

    Set<RobotDryRunLibraryImport> getLibraryImports() {
        return libraryImports;
    }

    Multimap<RobotDryRunLibraryImport, RobotSuiteFile> getLibraryImporters() {
        return libraryImporters;
    }

    Multimap<String, RobotSuiteFile> getUnknownLibraryNames() {
        return unknownLibraryNames;
    }

    private class DiscoveringImportValidator extends GeneralSettingsLibrariesImportValidator {

        DiscoveringImportValidator(final FileValidationContext validationContext, final List<LibraryImport> imports,
                final ValidationReportingStrategy reporter) {
            super(validationContext, currentSuite, imports, reporter);
        }

        @Override
        protected void validateNameImport(final String name, final RobotToken nameToken,
                final List<RobotToken> arguments) throws CoreException {
            if (!name.isEmpty() && !standardLibraryNames.contains(name)) {
                if (unknownLibraryNames.containsKey(name)) {
                    unknownLibraryNames.put(name, currentSuite);
                } else if (knownLibraryNames.containsKey(name)) {
                    knownLibraryNames.get(name).forEach(libImport -> libraryImporters.put(libImport, currentSuite));
                } else if ("Remote".equals(name)) {
                    if (arguments.size() == 1) {
                        final String address = RemoteLocation.createRemoteUri(arguments.get(0).getText());
                        final String remoteLibName = RemoteLocation.createRemoteName(arguments.get(0).getText());
                        final URI uriAddress = URI.create(address);
                        final RobotDryRunLibraryImport libImport = new RobotDryRunLibraryImport(remoteLibName,
                                uriAddress);
                        if (!libraryImports.contains(libImport)) {
                            libraryImports.add(libImport);
                            libraryImporters.put(libImport, currentSuite);
                            knownLibraryNames.put(remoteLibName, libImport);
                        } else {
                            libraryImporters.put(libImport, currentSuite);
                        }
                    } else if (arguments.isEmpty()) {
                        final String remoteLibName = name + " " + RemoteLocation.DEFAULT_ADDRESS;
                        final URI uriAddress = URI.create(RemoteLocation.DEFAULT_ADDRESS);
                        final RobotDryRunLibraryImport libImport = new RobotDryRunLibraryImport(remoteLibName,
                                uriAddress);
                        if (!libraryImports.contains(libImport)) {
                            libraryImports.add(libImport);
                            libraryImporters.put(libImport, currentSuite);
                            knownLibraryNames.put(remoteLibName, libImport);
                        } else {
                            libraryImporters.put(libImport, currentSuite);
                        }
                    }
                } else {
                    libraryLocator.locateByName(currentSuite, name);
                }
            }
        }

        @Override
        protected void validatePathImport(final String path, final RobotToken pathToken, final boolean isParameterized,
                final List<RobotToken> arguments) throws CoreException {
            libraryLocator.locateByPath(currentSuite, path);
        }

    }

    private class DiscoveringReportingStrategy extends ValidationReportingStrategy {

        DiscoveringReportingStrategy() {
            super(false);
        }

        @Override
        public void handleTask(final RobotTask task, final IFile file) {
            // not interested in tasks reporting
        }

        @Override
        public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition,
                final Map<String, Object> additionalAttributes) {
            if (problem.getCause() == GeneralSettingsProblem.IMPORT_PATH_PARAMETERIZED) {
                final RobotDryRunLibraryImport libImport = new RobotDryRunLibraryImport(
                        (String) additionalAttributes.get(AdditionalMarkerAttributes.NAME));
                libImport.setStatus(RobotDryRunLibraryImport.DryRunLibraryImportStatus.NOT_ADDED);
                libImport.setAdditionalInfo(problem.getMessage());
                libraryImports.add(libImport);
                libraryImporters.put(libImport, currentSuite);
            }
        }
    }

    private static class DiscoveringLibraryImporter implements IReferencedLibraryImporter {

        @Override
        public Collection<ReferencedLibrary> importPythonLib(final RobotRuntimeEnvironment environment,
                final IProject project, final RobotProjectConfig config, final String fullLibraryPath) {
            final ILibraryStructureBuilder builder = new PythonLibStructureBuilder(environment, config, project);
            return importLib(builder, fullLibraryPath);
        }

        @Override
        public Collection<ReferencedLibrary> importJavaLib(final RobotRuntimeEnvironment environment,
                final IProject project, final RobotProjectConfig config, final String fullLibraryPath) {
            final ILibraryStructureBuilder builder = new JarStructureBuilder(environment, config, project);
            return importLib(builder, fullLibraryPath);
        }

        Collection<ReferencedLibrary> importLib(final ILibraryStructureBuilder builder, final String fullLibraryPath) {
            try {
                final Collection<ILibraryClass> libClasses = builder
                        .provideEntriesFromFile(RedURI.fromString(fullLibraryPath));
                return libClasses.stream()
                        .findFirst()
                        .map(libClass -> Collections.singletonList(libClass.toReferencedLibrary(fullLibraryPath)))
                        .orElse(Collections.emptyList());
            } catch (final URISyntaxException e) {
                return Collections.emptyList();
            }
        }
    }

    private class DiscoveringLibraryDetector implements IReferencedLibraryDetector {

        @Override
        public void libraryDetectedByName(final String name, final File libraryFile,
                final Collection<ReferencedLibrary> referenceLibraries) {
            final List<RobotDryRunLibraryImport> imports = mapToImports(libraryFile, referenceLibraries);
            imports.forEach(libImport -> {
                knownLibraryNames.put(name, libImport);
                libraryImports.add(libImport);
                libraryImporters.put(libImport, currentSuite);
            });
        }

        @Override
        public void libraryDetectedByPath(final String path, final File libraryFile,
                final Collection<ReferencedLibrary> referenceLibraries) {
            final List<RobotDryRunLibraryImport> imports = mapToImports(libraryFile, referenceLibraries);
            imports.forEach(libImport -> {
                libraryImports.add(libImport);
                libraryImporters.put(libImport, currentSuite);
            });
        }

        @Override
        public void libraryDetectingByNameFailed(final String name, final Optional<File> libraryFile,
                final String failReason) {
            unknownLibraryNames.put(name, currentSuite);
        }

        @Override
        public void libraryDetectingByPathFailed(final String path, final Optional<File> libraryFile,
                final String failReason) {
            final RobotDryRunLibraryImport libImport = libraryFile.isPresent()
                    ? new RobotDryRunLibraryImport(path, libraryFile.get().toURI())
                    : new RobotDryRunLibraryImport(path);
            libImport.setStatus(RobotDryRunLibraryImport.DryRunLibraryImportStatus.NOT_ADDED);
            libImport.setAdditionalInfo(failReason);
            libraryImports.add(libImport);
            libraryImporters.put(libImport, currentSuite);
        }

        private List<RobotDryRunLibraryImport> mapToImports(final File libraryFile,
                final Collection<ReferencedLibrary> referenceLibraries) {
            return referenceLibraries.stream()
                    .map(lib -> new RobotDryRunLibraryImport(lib.getName(), libraryFile.toURI()))
                    .collect(Collectors.toList());
        }
    }

}
