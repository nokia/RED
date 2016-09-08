/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class DeleteSectionCommandTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenNullSectionIsGivenForRemoval() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteSectionCommand(null))
                .execute();

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void settingsSectionIsProperlyRemoved() {
        final RobotSuiteFile model = createModel();
        testSectionRemoval(model, RobotSettingsSection.class);
        assertThat(model.getLinkedElement().getSettingTable().isEmpty()).isTrue();
    }

    @Test
    public void variablesSectionIsProperlyRemoved() {
        final RobotSuiteFile model = createModel();
        testSectionRemoval(model, RobotVariablesSection.class);
        assertThat(model.getLinkedElement().getVariableTable().isEmpty()).isTrue();
    }

    @Test
    public void casesSectionIsProperlyRemoved() {
        final RobotSuiteFile model = createModel();
        testSectionRemoval(model, RobotCasesSection.class);
        assertThat(model.getLinkedElement().getTestCaseTable().isEmpty()).isTrue();
    }

    @Test
    public void keywordsSectionIsProperlyRemoved() {
        final RobotSuiteFile model = createModel();
        testSectionRemoval(model, RobotKeywordsSection.class);
        assertThat(model.getLinkedElement().getKeywordTable().isEmpty()).isTrue();
    }

    private void testSectionRemoval(final RobotSuiteFile model,
            final Class<? extends RobotSuiteFileSection> sectionClass) {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteSectionCommand(model.findSection(sectionClass).get()))
                .execute();

        assertThat(model.findSection(sectionClass).isPresent()).isFalse();
    }

    private static RobotSuiteFile createModel() {
        return new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0")
                .appendLine("${scalar_as_list}  0  1  2")
                .appendLine("*** Settings ***")
                .appendLine("Library  foo")
                .appendLine("Library  bar")
                .appendLine("*** Keywords ***")
                .appendLine("myKeyword")
                .appendLine("  [Return]  10")
                .appendLine("*** Test Cases ***")
                .appendLine("case1")
                .appendLine("  Log  123")
                .appendLine("case2")
                .appendLine("  myKeyword")
                .build();
    }
}
