/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.red.junit.ProjectProvider;

public class SuiteFileMarkersListenerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SuiteFileMarkersListenerTest.class);

    private static RobotSuiteFile varsSuiteModel;
    private static RobotSuiteFile settingsSuiteModel;
    private static RobotSuiteFile casesSuiteModel;
    private static RobotSuiteFile keywordsSuiteModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        switchAutobuilding(false);
        projectProvider.configure();
        projectProvider.addRobotNature();

        projectProvider.getProject().deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);

        final RobotModel robotModel = new RobotModel();

        final IFile varsFile = projectProvider.createFile(new Path("vars.robot"),
                "*** Test Cases ***",
                "*** Variables ***",
                "${var}  1",
                "${var1}  2",
                "${var1}  3  ${var}",
                "{var}  4",
                "{var}  4");
        varsSuiteModel = robotModel.createSuiteFile(varsFile);
        final IFile settingsFile = projectProvider.createFile(new Path("settings.robot"),
                "*** Test Cases ***",
                "*** Settings ***",
                "Documentation  doc",
                "Suite Setup",
                "Suite Teardown",
                "Test Template  unknown  1  2");
        settingsSuiteModel = robotModel.createSuiteFile(settingsFile);
        final IFile casesFile = projectProvider.createFile(new Path("cases.robot"),
                "*** Test Cases ***",
                "case1",
                "  Log  1",
                "case2",
                "  unknown1",
                "  unknown2  ${x}",
                "case2",
                "*** Keywords ***",
                "Log",
                "  [Arguments]  ${x}",
                "  [Return]  ${x}");
        casesSuiteModel = robotModel.createSuiteFile(casesFile);
        final IFile keywordsFile = projectProvider.createFile(new Path("keywords.robot"),
                "*** Test Cases ***",
                "*** Keywords ***",
                "kw1",
                "  Log  1",
                "kw2",
                "  [Arguments]  ${y}",
                "  unknown1",
                "  unknown2  ${x}",
                "kw2",
                "kw3",
                "  [Arguments]  1",
                "Log",
                "  [Arguments]  ${x}",
                "  [Return]  ${x}");
        keywordsSuiteModel = robotModel.createSuiteFile(keywordsFile);

        RobotArtifactsValidator.revalidate(varsSuiteModel).join();
        RobotArtifactsValidator.revalidate(settingsSuiteModel).join();
        RobotArtifactsValidator.revalidate(casesSuiteModel).join();
        RobotArtifactsValidator.revalidate(keywordsSuiteModel).join();
    }

    @AfterClass
    public static void afterSuite() throws Exception {
        switchAutobuilding(true);
    }

    private static void switchAutobuilding(final boolean enable) throws CoreException {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceDescription desc = workspace.getDescription();
        desc.setAutoBuilding(enable);
        workspace.setDescription(desc);
    }

    @Test
    public void markersAreLoaded_whenInitializationIsDone() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SuiteFileMarkersListener listener = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .inWhich(casesSuiteModel)
                .isInjectedInto(new SuiteFileMarkersListener());
        listener.init();

        assertThat(listener.getMarkers()).hasSize(6);
        verify(eventBroker).post(RobotModelEvents.MARKERS_CACHE_RELOADED, casesSuiteModel);
    }

    @Test
    public void checkMarkersOnVariables() {
        final RobotVariablesSection section = varsSuiteModel.findSection(RobotVariablesSection.class).get();
        final List<RobotVariable> variables = section.getChildren();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SuiteFileMarkersListener listener = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .inWhich(varsSuiteModel)
                .isInjectedInto(new SuiteFileMarkersListener());
        listener.init();

        assertThat(severityFor(listener, variables.get(0))).isNull();
        assertThat(messageFor(listener, variables.get(0))).isEmpty();

        assertThat(severityFor(listener, variables.get(1))).isEqualTo(Severity.WARNING);
        assertThat(messageFor(listener, variables.get(1))).containsOnly("Duplicated variable definition 'var1'");

        assertThat(severityFor(listener, variables.get(2))).isEqualTo(Severity.WARNING);
        assertThat(messageFor(listener, variables.get(2))).containsOnly("Duplicated variable definition 'var1'");

        assertThat(severityFor(listener, variables.get(3))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, variables.get(3))).containsOnly("Duplicated variable definition '{var}'",
                "Invalid variable definition '{var}'. Unable to recognize variable type");

        assertThat(severityFor(listener, variables.get(4))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, variables.get(4))).containsOnly("Duplicated variable definition '{var}'",
                "Invalid variable definition '{var}'. Unable to recognize variable type");
    }

    @Test
    public void checkMarkersOnSettings() {
        final RobotSettingsSection section = settingsSuiteModel.findSection(RobotSettingsSection.class).get();
        final List<RobotKeywordCall> settings = section.getChildren();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SuiteFileMarkersListener listener = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .inWhich(settingsSuiteModel)
                .isInjectedInto(new SuiteFileMarkersListener());
        listener.init();

        assertThat(severityFor(listener, settings.get(0))).isNull();
        assertThat(messageFor(listener, settings.get(0))).isEmpty();

        assertThat(severityFor(listener, settings.get(1))).isEqualTo(Severity.WARNING);
        assertThat(messageFor(listener, settings.get(1))).containsOnly("Empty setting 'Suite Setup'");

        assertThat(severityFor(listener, settings.get(2))).isEqualTo(Severity.WARNING);
        assertThat(messageFor(listener, settings.get(2))).containsOnly("Empty setting 'Suite Teardown'");

        assertThat(severityFor(listener, settings.get(3))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, settings.get(3))).containsOnly(
                "Setting 'Test Template' is not applicable for arguments: [1, 2]. Only keyword name should be specified for templates.",
                "Unknown keyword 'unknown'");
    }

    @Test
    public void checkMarkersOnTestCases() {
        final RobotCasesSection section = casesSuiteModel.findSection(RobotCasesSection.class).get();
        final List<RobotCase> cases = section.getChildren();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SuiteFileMarkersListener listener = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .inWhich(casesSuiteModel)
                .isInjectedInto(new SuiteFileMarkersListener());
        listener.init();

        assertThat(severityFor(listener, cases.get(0))).isNull();
        assertThat(messageFor(listener, cases.get(0))).isEmpty();

        assertThat(severityFor(listener, cases.get(1))).isEqualTo(Severity.WARNING);
        assertThat(messageFor(listener, cases.get(1))).containsOnly("Duplicated test case definition 'case2'");

        assertThat(severityFor(listener, cases.get(2))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, cases.get(2))).containsOnly("Duplicated test case definition 'case2'",
                "Test case 'case2' contains no keywords to execute");

        final List<RobotKeywordCall> calls0 = cases.get(0).getChildren();
        assertThat(severityFor(listener, calls0.get(0))).isNull();
        assertThat(messageFor(listener, calls0.get(0))).isEmpty();

        final List<RobotKeywordCall> calls1 = cases.get(1).getChildren();
        assertThat(severityFor(listener, calls1.get(0))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, calls1.get(0))).containsOnly("Unknown keyword 'unknown1'");

        assertThat(severityFor(listener, calls1.get(1))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, calls1.get(1))).containsOnly("Unknown keyword 'unknown2'",
                "Variable 'x' is used, but not defined");
    }

    @Test
    public void checkMarkersOnKeywords() {
        final RobotKeywordsSection section = keywordsSuiteModel.findSection(RobotKeywordsSection.class).get();
        final List<RobotKeywordDefinition> keywords = section.getChildren();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SuiteFileMarkersListener listener = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .inWhich(keywordsSuiteModel)
                .isInjectedInto(new SuiteFileMarkersListener());
        listener.init();

        assertThat(severityFor(listener, keywords.get(0))).isNull();
        assertThat(messageFor(listener, keywords.get(0))).isEmpty();

        assertThat(severityFor(listener, keywords.get(1))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, keywords.get(1))).containsOnly("Duplicated keyword definition 'kw2'");

        assertThat(severityFor(listener, keywords.get(2))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, keywords.get(2))).containsOnly("Duplicated keyword definition 'kw2'",
                "Keyword 'kw2' contains no keywords to execute");

        assertThat(severityFor(listener, keywords.get(3))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, keywords.get(3))).containsOnly("Keyword 'kw3' contains no keywords to execute",
                "The argument '1' has invalid syntax");

        final List<RobotKeywordCall> calls0 = keywords.get(0).getChildren();
        assertThat(severityFor(listener, calls0.get(0))).isNull();
        assertThat(messageFor(listener, calls0.get(0))).isEmpty();

        final List<RobotKeywordCall> calls1 = keywords.get(1).getChildren();
        assertThat(severityFor(listener, calls1.get(1))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, calls1.get(1))).containsOnly("Unknown keyword 'unknown1'");

        assertThat(severityFor(listener, calls1.get(2))).isEqualTo(Severity.ERROR);
        assertThat(messageFor(listener, calls1.get(2))).containsOnly("Unknown keyword 'unknown2'",
                "Variable 'x' is used, but not defined");
    }

    private static Severity severityFor(final SuiteFileMarkersListener listener,
            final RobotFileInternalElement element) {
        return listener.getHighestSeverityMarkerFor(Optional.of(element)).orElse(null);
    }

    private static List<String> messageFor(final SuiteFileMarkersListener listener,
            final RobotFileInternalElement element) {
        return listener.getMarkersMessagesFor(Optional.of(element));
    }
}

