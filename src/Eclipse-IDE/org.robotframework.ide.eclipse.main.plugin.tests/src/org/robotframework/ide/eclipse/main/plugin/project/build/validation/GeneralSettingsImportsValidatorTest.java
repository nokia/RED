/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

public class GeneralSettingsImportsValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void testValidateLibraryImport_withAbsoluteLibPathAndWindowsPathSeparators() throws CoreException {
        final List<LibraryImport> imports = new ArrayList<>();
        imports.add(createNewLibraryImport("C:\\test\\ExampleLibrary.py", 2, 26));

        final FileValidationContext context = prepareContext();
        final GeneralSettingsImportsValidator validator = new GeneralSettingsImportsValidator.LibraryImportValidator(
                context, createSuiteFile(), imports, reporter, Optional.<LibrariesAutoDiscoverer> absent());

        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.NOT_ESCAPED_WINDOWS_PATH, new ProblemPosition(2, Range.closed(26, 51))));
    }

    @Test
    public void testValidateLibraryImport_withRelativeLibPathAndWindowsPathSeparators() throws CoreException {
        final List<LibraryImport> imports = new ArrayList<>();
        imports.add(createNewLibraryImport("..\\..\\ExampleLibrary.py", 2, 26));

        final FileValidationContext context = prepareContext();
        final GeneralSettingsImportsValidator validator = new GeneralSettingsImportsValidator.LibraryImportValidator(
                context, createSuiteFile(), imports, reporter, Optional.<LibrariesAutoDiscoverer> absent());

        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.NOT_ESCAPED_WINDOWS_PATH, new ProblemPosition(2, Range.closed(26, 49))));
    }

    @Test
    public void testValidateLibraryImport_withWindowsAndLinuxPathSeparator() throws CoreException {
        final List<LibraryImport> imports = new ArrayList<>();
        imports.add(createNewLibraryImport("../..\\ExampleLibrary.py", 2, 26));

        final FileValidationContext context = prepareContext();
        final GeneralSettingsImportsValidator validator = new GeneralSettingsImportsValidator.LibraryImportValidator(
                context, createSuiteFile(), imports, reporter, Optional.<LibrariesAutoDiscoverer> absent());

        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.NOT_ESCAPED_WINDOWS_PATH, new ProblemPosition(2, Range.closed(26, 49))));
    }

    @Test
    public void testValidateLibraryImport_withExistingLibName() throws CoreException {
        final String libName = "ExampleLibrary";

        final List<LibraryImport> imports = new ArrayList<>();
        imports.add(createNewLibraryImport(libName));

        final LibrarySpecification spec = createNewLibrarySpecification(libName);
        final ReferencedLibrary refLib = createNewPythonReferencedLibrary(libName, "");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(refLib, spec);
        final Map<String, LibrarySpecification> specs = new HashMap<>();
        specs.put(libName, spec);

        final FileValidationContext context = prepareContext(refLibs, specs);
        final GeneralSettingsImportsValidator validator = new GeneralSettingsImportsValidator.LibraryImportValidator(
                context, createSuiteFile(), imports, reporter, Optional.<LibrariesAutoDiscoverer> absent());

        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }

    @Test
    public void testValidateLibraryImport_withNonExistingLibName() throws CoreException {
        final String libName = "ExampleLibrary";

        final List<LibraryImport> imports = new ArrayList<>();
        imports.add(createNewLibraryImport(libName, 2, 26));

        final FileValidationContext context = prepareContext();
        final GeneralSettingsImportsValidator validator = new GeneralSettingsImportsValidator.LibraryImportValidator(
                context, createSuiteFile(), imports, reporter, Optional.<LibrariesAutoDiscoverer> absent());

        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(GeneralSettingsProblem.UNKNOWN_LIBRARY, new ProblemPosition(2, Range.closed(26, 40))));
    }

    @Test
    public void testValidateLibraryImport_withEmptyLibPathOrName() throws CoreException {
        final List<LibraryImport> imports = new ArrayList<>();
        imports.add(createNewLibraryImport("", 2, 0));

        final FileValidationContext context = prepareContext();
        final GeneralSettingsImportsValidator validator = new GeneralSettingsImportsValidator.LibraryImportValidator(
                context, createSuiteFile(), imports, reporter, Optional.<LibrariesAutoDiscoverer> absent());

        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(GeneralSettingsProblem.MISSING_LIBRARY_NAME, new ProblemPosition(2, Range.closed(0, 9))));
    }

    private LibraryImport createNewLibraryImport(final String pathOrName) {
        return createNewLibraryImport(pathOrName, 0, 0);
    }

    private LibraryImport createNewLibraryImport(final String pathOrName, final int lineNumber, final int startOffset) {
        final RobotToken declarationToken = new RobotToken();
        declarationToken.setText("Library  " + pathOrName);
        declarationToken.setLineNumber(2);
        declarationToken.setStartOffset(0);
        final LibraryImport libImport = new LibraryImport(declarationToken);
        final RobotToken pathToken = new RobotToken();
        pathToken.setText(pathOrName);
        pathToken.setLineNumber(lineNumber);
        pathToken.setStartOffset(startOffset);
        if (!pathOrName.isEmpty()) {
            libImport.setPathOrName(pathToken);
        }
        return libImport;
    }

    private LibrarySpecification createNewLibrarySpecification(final String libName) {
        final LibrarySpecification spec = new LibrarySpecification();
        spec.setName(libName);
        return spec;
    }

    private ReferencedLibrary createNewPythonReferencedLibrary(final String libName, final String libPath) {
        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        return refLib;
    }

    private RobotSuiteFile createSuiteFile() {
        RobotSuiteFile mySuiteFile = mock(RobotSuiteFile.class);

        RobotProject robotProject = mock(RobotProject.class);
        when(robotProject.getModuleSearchPaths()).thenReturn(new ArrayList<File>());
        when(mySuiteFile.getProject()).thenReturn(robotProject);

        IFile file = mock(IFile.class);
        when(file.getLocation()).thenReturn(new Path("/suite.robot"));
        when(mySuiteFile.getFile()).thenReturn(file);

        return mySuiteFile;
    }

    private static FileValidationContext prepareContext() {
        return prepareContext(new HashMap<ReferencedLibrary, LibrarySpecification>(),
                new HashMap<String, LibrarySpecification>());
    }

    private static FileValidationContext prepareContext(final Map<ReferencedLibrary, LibrarySpecification> refLibs,
            final Map<String, LibrarySpecification> specs) {
        final ValidationContext parentContext = new ValidationContext(new RobotModel(), RobotVersion.from("0.0"),
                SuiteExecutor.Python, specs, refLibs);
        final IFile file = mock(IFile.class);
        when(file.getFullPath()).thenReturn(new Path("/suite.robot"));
        final FileValidationContext context = new FileValidationContext(parentContext, file,
                createKeywordsCollector(Maps.<String, Collection<KeywordEntity>> newHashMap()), new HashSet<String>());
        return context;
    }

    private static AccessibleKeywordsCollector createKeywordsCollector(
            final Map<String, Collection<KeywordEntity>> map) {
        return new AccessibleKeywordsCollector() {

            @Override
            public Map<String, Collection<KeywordEntity>> collect() {
                return map;
            }
        };
    }
}
