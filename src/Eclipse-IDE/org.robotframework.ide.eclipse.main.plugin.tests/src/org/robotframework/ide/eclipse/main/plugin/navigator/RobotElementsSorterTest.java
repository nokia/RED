/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.navigator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jface.viewers.Viewer;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.NamedElement;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.ParentElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class RobotElementsSorterTest {

    private final RobotElementsSorter sorter = new RobotElementsSorter();

    @Test
    public void categoryIsAlwaysTheSameForEveryElement() throws Exception {
        assertThat(sorter.category(mock(RobotProject.class))).isZero();
        assertThat(sorter.category(mock(RobotFolder.class))).isZero();
        assertThat(sorter.category(mock(RobotSuiteFile.class))).isZero();
        assertThat(sorter.category(mock(RobotSuiteFileSection.class))).isZero();
        assertThat(sorter.category(mock(RobotCasesSection.class))).isZero();
        assertThat(sorter.category(mock(RobotKeywordsSection.class))).isZero();
        assertThat(sorter.category(mock(RobotSettingsSection.class))).isZero();
        assertThat(sorter.category(mock(RobotVariablesSection.class))).isZero();
        assertThat(sorter.category(mock(RobotKeywordCall.class))).isZero();
        assertThat(sorter.category(mock(RobotDefinitionSetting.class))).isZero();
        assertThat(sorter.category(mock(ArtificialGroupingRobotElement.class))).isZero();
        assertThat(sorter.category(mock(RobotSetting.class))).isZero();
        assertThat(sorter.category(mock(RobotCase.class))).isZero();
        assertThat(sorter.category(mock(RobotKeywordDefinition.class))).isZero();
        assertThat(sorter.category(mock(RobotVariable.class))).isZero();
    }

    @Test(expected = ClassCastException.class)
    public void itsAnErrorToCompareObjectsOtherThanRobotElements() {
        sorter.compare(mock(Viewer.class), "abc", "def");
    }

    @Test
    public void comparingReturnsNegative_whenFirstParameterHasLesserIndex() {
        final ParentElement parent = new ParentElement();

        final RobotElement e1 = new NamedElement(parent, "e1");
        final RobotElement e2 = new NamedElement(parent, "e2");

        parent.getChildren().add(e1);
        parent.getChildren().add(e2);

        assertThat(sorter.compare(mock(Viewer.class), e1, e2)).isNegative();
    }

    @Test
    public void comparingReturnsNegative_whenFirstParameterHasGreaterIndex() {
        final ParentElement parent = new ParentElement();

        final RobotElement e1 = new NamedElement(parent, "e1");
        final RobotElement e2 = new NamedElement(parent, "e2");

        parent.getChildren().add(e2);
        parent.getChildren().add(e1);

        assertThat(sorter.compare(mock(Viewer.class), e1, e2)).isPositive();
    }

    @Test
    public void comparingReturnsZero_whenSameElementIsCompared() {
        final ParentElement parent = new ParentElement();

        final RobotElement e = new NamedElement(parent, "e1");
        parent.getChildren().add(e);

        assertThat(sorter.compare(mock(Viewer.class), e, e)).isZero();
    }
}
