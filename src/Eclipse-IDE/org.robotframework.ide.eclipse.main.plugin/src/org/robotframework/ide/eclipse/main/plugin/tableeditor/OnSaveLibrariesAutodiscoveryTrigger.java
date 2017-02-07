/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
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

import com.google.common.base.Function;
import com.google.common.base.Optional;

class OnSaveLibrariesAutodiscoveryTrigger implements IExecutionListener {

    static final String SAVE_ALL_COMMAND_ID = "org.eclipse.ui.file.saveAll";

    private static OnSaveLibrariesAutodiscoveryTrigger globalBatchSaveResponsibleTrigger = null;

    private static final List<RobotSuiteFile> suitesForDiscover = new ArrayList<>();

    private final DiscovererFactory discovererFactory;

    OnSaveLibrariesAutodiscoveryTrigger() {
        this(new DiscovererFactory() {

            @Override
            public LibrariesAutoDiscoverer create(final RobotProject robotProject, final List<IFile> suites) {
                final boolean showSummary = robotProject.getRobotProjectConfig()
                        .isLibrariesAutoDiscoveringSummaryWindowEnabled();
                return new LibrariesAutoDiscoverer(robotProject, suites, showSummary);
            }
        });
    }

    OnSaveLibrariesAutodiscoveryTrigger(final DiscovererFactory discovererFactory) {
        this.discovererFactory = discovererFactory;
    }

    @Override
    public void preExecute(final String commandId, final ExecutionEvent event) {
        if (globalBatchSaveResponsibleTrigger == null && SAVE_ALL_COMMAND_ID.equals(commandId)) {
            globalBatchSaveResponsibleTrigger = this;
        }
    }

    @Override
    public void notHandled(final String commandId, final NotHandledException exception) {
        if (SAVE_ALL_COMMAND_ID.equals(commandId)) {
            globalBatchSaveResponsibleTrigger = null;
            suitesForDiscover.clear();
        }
    }

    @Override
    public void postExecuteFailure(final String commandId, final ExecutionException exception) {
        if (SAVE_ALL_COMMAND_ID.equals(commandId)) {
            globalBatchSaveResponsibleTrigger = null;
            suitesForDiscover.clear();
        }
    }

    @Override
    public void postExecuteSuccess(final String commandId, final Object returnValue) {
        if (globalBatchSaveResponsibleTrigger == this && SAVE_ALL_COMMAND_ID.equals(commandId)) {
            globalBatchSaveResponsibleTrigger = null;

            if (!suitesForDiscover.isEmpty()) {
                final RobotProject project = suitesForDiscover.get(0).getProject();
                startAutoDiscovering(project,
                        newArrayList(transform(suitesForDiscover, new Function<RobotSuiteFile, IFile>() {

                    @Override
                    public IFile apply(final RobotSuiteFile suite) {
                        return suite.getFile();
                    }
                })));
            }
            suitesForDiscover.clear();
        }
    }

    void startLibrariesAutoDiscoveryIfRequired(final RobotSuiteFile suite) {
        if (shouldStartAutoDiscovering(suite)) {

            if (globalBatchSaveResponsibleTrigger == null) {
                startAutoDiscovering(suite.getProject(), newArrayList(suite.getFile()));
            } else {
                suitesForDiscover.add(suite);
            }
        }
    }

    private void startAutoDiscovering(final RobotProject robotProject, final List<IFile> suites) {
        discovererFactory.create(robotProject, suites).start();
    }

    private boolean shouldStartAutoDiscovering(final RobotSuiteFile suite) {
        final RobotProjectConfig projectConfig = suite.getProject().getRobotProjectConfig();
        final boolean isAutodiscoveryEnabled = projectConfig != null
                && projectConfig.isReferencedLibrariesAutoDiscoveringEnabled();
        return isAutodiscoveryEnabled && currentModelHaveUnknownLibrary(suite);
    }

    private boolean currentModelHaveUnknownLibrary(final RobotSuiteFile suite) {
        final List<LibraryImport> imports = collectLibraryImports(suite);

        final UnknownLibraryDetectingReportingStrategy reporter = new UnknownLibraryDetectingReportingStrategy();

        final ValidationContext generalContext = new ValidationContext(suite.getProject(),
                new BuildLogger());
        final FileValidationContext fileContext = new FileValidationContext(generalContext, suite.getFile());
        final GeneralSettingsLibrariesImportValidator importsValidator = new GeneralSettingsLibrariesImportValidator(
                fileContext, suite, imports, reporter);
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

    public interface DiscovererFactory {

        LibrariesAutoDiscoverer create(RobotProject project, List<IFile> suites);
    }
}
