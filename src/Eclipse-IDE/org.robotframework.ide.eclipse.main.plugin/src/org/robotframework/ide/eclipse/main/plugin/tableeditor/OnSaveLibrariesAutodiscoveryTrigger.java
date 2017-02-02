/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.GeneralSettingsLibrariesImportValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;

class OnSaveLibrariesAutodiscoveryTrigger {

    private final RobotSuiteFile suite;

    private final Supplier<LibrariesAutoDiscoverer> discoverer;

    static OnSaveLibrariesAutodiscoveryTrigger createFor(final RobotSuiteFile suite) {
        return new OnSaveLibrariesAutodiscoveryTrigger(suite, new Supplier<LibrariesAutoDiscoverer>() {

            @Override
            public LibrariesAutoDiscoverer get() {
                final RobotProject robotProject = suite.getProject();
                final List<IFile> suites = newArrayList(suite.getFile());
                final boolean showSummary = robotProject.getRobotProjectConfig()
                        .isLibrariesAutoDiscoveringSummaryWindowEnabled();
                return new LibrariesAutoDiscoverer(robotProject, suites, showSummary);
            }
        });
    }

    @VisibleForTesting
    OnSaveLibrariesAutodiscoveryTrigger(final RobotSuiteFile suite,
            final Supplier<LibrariesAutoDiscoverer> discoverer) {
        this.suite = suite;
        this.discoverer = discoverer;
    }

    void startLibrariesAutoDiscoveryIfRequired() {
        if (shouldStartAutoDiscovering()) {
            discoverer.get().start();
        }
    }

    private boolean shouldStartAutoDiscovering() {
        final RobotProjectConfig projectConfig = suite.getProject().getRobotProjectConfig();
        final boolean isAutodiscoveryEnabled = projectConfig != null
                && projectConfig.isReferencedLibrariesAutoDiscoveringEnabled();
        return isAutodiscoveryEnabled && currentModelHaveUnknownLibrary(suite);
    }

    private boolean currentModelHaveUnknownLibrary(final RobotSuiteFile currentModel) {
        final List<LibraryImport> imports = collectLibraryImports(currentModel);

        final UnknownLibraryDetectingReportingStrategy reporter = new UnknownLibraryDetectingReportingStrategy();

        final ValidationContext generalContext = new ValidationContext(currentModel.getProject(),
                new BuildLogger());
        final FileValidationContext fileContext = new FileValidationContext(generalContext, currentModel.getFile());
        final GeneralSettingsLibrariesImportValidator importsValidator = new GeneralSettingsLibrariesImportValidator(
                fileContext, currentModel, imports, reporter);
        try {
            importsValidator.validate(new NullProgressMonitor());
            return reporter.unknownLibraryWasDetected();
        } catch (final CoreException e) {
            return false;
        }
    }

    private List<LibraryImport> collectLibraryImports(final RobotSuiteFile currentModel) {
        final List<LibraryImport> imports = new ArrayList<>();
        final Optional<RobotSettingsSection> settingsSection = currentModel.findSection(RobotSettingsSection.class);
        if (settingsSection.isPresent()) {
            for (final RobotKeywordCall setting : settingsSection.get().getLibrariesSettings()) {
                imports.add((LibraryImport) setting.getLinkedElement());
            }
        }
        return imports;
    }

    private static class UnknownLibraryDetectingReportingStrategy extends ProblemsReportingStrategy {

        private boolean detectedLibraryProblem = false;

        UnknownLibraryDetectingReportingStrategy() {
            super(false);
        }

        @Override
        public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition,
                final Map<String, Object> additionalAttributes) {
            if (problem.getCause() == GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT) {
                this.detectedLibraryProblem = true;
            }
        }

        boolean unknownLibraryWasDetected() {
            return detectedLibraryProblem;
        }
    }
}
