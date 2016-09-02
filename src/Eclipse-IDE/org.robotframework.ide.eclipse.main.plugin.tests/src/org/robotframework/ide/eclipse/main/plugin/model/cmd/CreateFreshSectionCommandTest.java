/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class CreateFreshSectionCommandTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToCreateUnknownSection() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotSuiteFile model = new RobotSuiteFileCreator().build();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshSectionCommand(model, "invalid"))
                .execute();
    }

    @Test
    public void nothingIsCreated_whenSectionAlreadyExist() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***").build();
        final RobotVariablesSection currentSection = model.findSection(RobotVariablesSection.class).get();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshSectionCommand(model, RobotVariablesSection.SECTION_NAME))
                .execute();

        assertThat(model.getChildren()).hasSize(1);
        assertThat(model.getChildren().get(0)).isSameAs(currentSection);
    }

    @Test
    public void variablesSectionIsProperlyCreated() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotSuiteFile model = new RobotSuiteFileCreator().build();
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshSectionCommand(model, RobotVariablesSection.SECTION_NAME))
                .execute();

        assertThat(model.getChildren()).hasSize(1);
        assertThat(model.getChildren().get(0)).isInstanceOf(RobotVariablesSection.class);
        assertThat(model.getChildren().get(0).getParent()).isSameAs(model);
        assertThat(model.getLinkedElement().getVariableTable()).isNotNull();
        assertThat(model.getLinkedElement().getVariableTable().getParent()).isSameAs(model.getLinkedElement());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED, model);
    }

    @Test
    public void settingsSectionIsProperlyCreated() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotSuiteFile model = new RobotSuiteFileCreator().build();
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshSectionCommand(model, RobotSettingsSection.SECTION_NAME))
                .execute();

        assertThat(model.getChildren()).hasSize(1);
        assertThat(model.getChildren().get(0)).isInstanceOf(RobotSettingsSection.class);
        assertThat(model.getChildren().get(0).getParent()).isSameAs(model);
        assertThat(model.getLinkedElement().getSettingTable()).isNotNull();
        assertThat(model.getLinkedElement().getSettingTable().getParent()).isSameAs(model.getLinkedElement());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED, model);
    }

    @Test
    public void keywordsSectionIsProperlyCreated() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotSuiteFile model = new RobotSuiteFileCreator().build();
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshSectionCommand(model, RobotKeywordsSection.SECTION_NAME))
                .execute();

        assertThat(model.getChildren()).hasSize(1);
        assertThat(model.getChildren().get(0)).isInstanceOf(RobotKeywordsSection.class);
        assertThat(model.getChildren().get(0).getParent()).isSameAs(model);
        assertThat(model.getLinkedElement().getKeywordTable()).isNotNull();
        assertThat(model.getLinkedElement().getKeywordTable().getParent()).isSameAs(model.getLinkedElement());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED, model);
    }

    @Test
    public void testCasesSectionIsProperlyCreated() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotSuiteFile model = new RobotSuiteFileCreator().build();
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshSectionCommand(model, RobotCasesSection.SECTION_NAME))
                .execute();

        assertThat(model.getChildren()).hasSize(1);
        assertThat(model.getChildren().get(0)).isInstanceOf(RobotCasesSection.class);
        assertThat(model.getChildren().get(0).getParent()).isSameAs(model);
        assertThat(model.getLinkedElement().getTestCaseTable()).isNotNull();
        assertThat(model.getLinkedElement().getTestCaseTable().getParent()).isSameAs(model.getLinkedElement());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED, model);
    }

}
