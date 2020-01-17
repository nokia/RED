/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotKeywordFailBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;

public class BreakpointDetailPaneFactoryTest {

    private final BreakpointDetailPaneFactory factory = new BreakpointDetailPaneFactory();

    @Test
    public void emptyIdsSetIsProvided_whenSelectionIsNull() {
        assertThat(factory.getDetailPaneTypes(null)).isEmpty();
    }

    @Test
    public void emptyIdsSetIsProvided_whenSelectionIsEmpty() {
        assertThat(factory.getDetailPaneTypes(selection())).isEmpty();
    }

    @Test
    public void emptyIdsSetIsProvided_whenSelectionContainsNonRobotLineBreakpoints() {
        assertThat(factory.getDetailPaneTypes(selection(mock(ILineBreakpoint.class)))).isEmpty();
        assertThat(factory.getDetailPaneTypes(selection(mock(ILineBreakpoint.class), mock(ILineBreakpoint.class))))
                .isEmpty();
    }

    @Test
    public void emptyIdsSetIsProvided_whenSelectionContainsMultipleRobotLineBreakpoints() {
        assertThat(
                factory.getDetailPaneTypes(selection(mock(RobotLineBreakpoint.class), mock(RobotLineBreakpoint.class))))
                        .isEmpty();
        assertThat(factory.getDetailPaneTypes(selection(mock(RobotLineBreakpoint.class),
                mock(RobotLineBreakpoint.class), mock(RobotLineBreakpoint.class)))).isEmpty();
    }

    @Test
    public void setContainingProperIdIsProvided_whenSelectionContainsExactlyOneRobotLineBreakpoint() {
        assertThat(factory.getDetailPaneTypes(selection(mock(RobotLineBreakpoint.class))))
                .containsOnly(RobotLineBreakpointDetailPane.ID);
    }

    @Test
    public void emptyIdsSetIsProvided_whenSelectionContainsMultipleKwFailBreakpoints() {
        assertThat(factory.getDetailPaneTypes(
                selection(mock(RobotKeywordFailBreakpoint.class), mock(RobotKeywordFailBreakpoint.class)))).isEmpty();
        assertThat(factory.getDetailPaneTypes(selection(mock(RobotKeywordFailBreakpoint.class),
                mock(RobotKeywordFailBreakpoint.class), mock(RobotKeywordFailBreakpoint.class)))).isEmpty();
    }

    @Test
    public void setContainingProperIdIsProvided_whenSelectionContainsExactlyOneKwFailBreakpoint() {
        assertThat(factory.getDetailPaneTypes(selection(mock(RobotKeywordFailBreakpoint.class))))
                .containsOnly(RobotKeywordFailBreakpointDetailPane.ID);
    }

    @Test
    public void emptyIdsSetIsProvided_whenSelectionContainsBothKwFailAndLineBreakpoints() {
        assertThat(factory
                .getDetailPaneTypes(selection(mock(RobotLineBreakpoint.class), mock(RobotKeywordFailBreakpoint.class))))
                        .isEmpty();
    }

    @Test
    public void nullIsReturnedAsDefaultDetailPane_whateverTheSelectionIsProvided() {
        assertThat(factory.getDefaultDetailPane(null)).isNull();
        assertThat(factory.getDefaultDetailPane(selection())).isNull();
        assertThat(factory.getDefaultDetailPane(selection(mock(ILineBreakpoint.class)))).isNull();
        assertThat(factory.getDefaultDetailPane(selection(mock(RobotLineBreakpoint.class)))).isNull();
        assertThat(factory.getDefaultDetailPane(selection(mock(RobotKeywordFailBreakpoint.class)))).isNull();
        assertThat(factory
                .getDefaultDetailPane(selection(mock(RobotLineBreakpoint.class), mock(RobotLineBreakpoint.class))))
                        .isNull();
        assertThat(factory.getDefaultDetailPane(
                selection(mock(RobotKeywordFailBreakpoint.class), mock(RobotKeywordFailBreakpoint.class)))).isNull();
    }

    @Test
    public void properDetailPaneIsProvided_forGivenIds() {
        assertThat(factory.createDetailPane(null)).isNull();
        assertThat(factory.createDetailPane("id")).isNull();
        assertThat(factory.createDetailPane(RobotLineBreakpointDetailPane.ID))
                .isExactlyInstanceOf(RobotLineBreakpointDetailPane.class);
        assertThat(factory.createDetailPane(RobotKeywordFailBreakpointDetailPane.ID))
                .isExactlyInstanceOf(RobotKeywordFailBreakpointDetailPane.class);
    }

    @Test
    public void properDetailPaneNameIsProvided_forGivenIds() {
        assertThat(factory.getDetailPaneName(null)).isNull();
        assertThat(factory.getDetailPaneName("id")).isNull();
        assertThat(factory.getDetailPaneName(RobotLineBreakpointDetailPane.ID))
                .isEqualTo(RobotLineBreakpointDetailPane.NAME);
        assertThat(factory.getDetailPaneName(RobotKeywordFailBreakpointDetailPane.ID))
                .isEqualTo(RobotKeywordFailBreakpointDetailPane.NAME);
    }

    @Test
    public void properDetailPaneDescriptionIsProvided_forGivenIds() {
        assertThat(factory.getDetailPaneDescription(null)).isNull();
        assertThat(factory.getDetailPaneDescription("id")).isNull();
        assertThat(factory.getDetailPaneDescription(RobotLineBreakpointDetailPane.ID))
                .isEqualTo(RobotLineBreakpointDetailPane.DESCRIPTION);
        assertThat(factory.getDetailPaneDescription(RobotKeywordFailBreakpointDetailPane.ID))
                .isEqualTo(RobotKeywordFailBreakpointDetailPane.DESCRIPTION);
    }

    private static IStructuredSelection selection(final Object... elements) {
        return new StructuredSelection(elements);
    }
}
