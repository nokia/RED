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
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;

public class BreakpointDetailPaneFactoryTest {

    private final BreakpointDetailPaneFactory factory = new BreakpointDetailPaneFactory();

    @Test
    public void emptyIdsSetIsProvided_whenSelectionIsNull() {
        assertThat(factory.getDetailPaneTypes(null)).isEmpty();
    }

    @Test
    public void emptyIdsSetIsProvided_whenSelectionIsEmpty() {
        final IStructuredSelection selection = new StructuredSelection();
        assertThat(factory.getDetailPaneTypes(selection)).isEmpty();
    }

    @Test
    public void emptyIdsSetIsProvided_whenSelectionContainsNonRobotLineBreakpoints() {
        assertThat(factory.getDetailPaneTypes(
                new StructuredSelection(new Object[] { mock(ILineBreakpoint.class) }))).isEmpty();
        assertThat(factory.getDetailPaneTypes(
                new StructuredSelection(new Object[] { mock(ILineBreakpoint.class), mock(ILineBreakpoint.class) })))
                        .isEmpty();
    }

    @Test
    public void emptyIdsSetIsProvided_whenSelectionContainsMultipleRobotLineBreakpoints() {
        assertThat(factory.getDetailPaneTypes(new StructuredSelection(
                new Object[] { mock(RobotLineBreakpoint.class), mock(RobotLineBreakpoint.class) }))).isEmpty();
        assertThat(factory.getDetailPaneTypes(new StructuredSelection(new Object[] { mock(RobotLineBreakpoint.class),
                mock(RobotLineBreakpoint.class), mock(RobotLineBreakpoint.class) }))).isEmpty();
    }

    @Test
    public void setContainingProperIdIsProvided_whenSelectionContainsExactlyOneRobotLineBreakpoint() {
        assertThat(
                factory.getDetailPaneTypes(new StructuredSelection(new Object[] { mock(RobotLineBreakpoint.class) })))
                        .containsOnly(BreakpointDetailPane.ID);
    }

    @Test
    public void nullIsReturnedAsDefaultDetailPane_whateverTheSelectionIsProvided() {
        assertThat(factory.getDefaultDetailPane(null)).isNull();
        assertThat(factory.getDefaultDetailPane(new StructuredSelection())).isNull();
        assertThat(factory.getDefaultDetailPane(new StructuredSelection(new Object[] { mock(ILineBreakpoint.class) })))
                .isNull();
        assertThat(
                factory.getDefaultDetailPane(new StructuredSelection(new Object[] { mock(RobotLineBreakpoint.class) })))
                        .isNull();
        assertThat(factory.getDefaultDetailPane(new StructuredSelection(
                new Object[] { mock(RobotLineBreakpoint.class), mock(RobotLineBreakpoint.class) }))).isNull();
    }

    @Test
    public void properDetailPaneIsProvided_forGivenIds() {
        assertThat(factory.createDetailPane(null)).isNull();
        assertThat(factory.createDetailPane("id")).isNull();
        assertThat(factory.createDetailPane(BreakpointDetailPane.ID)).isExactlyInstanceOf(BreakpointDetailPane.class);
    }

    @Test
    public void properDetailPaneNameIsProvided_forGivenIds() {
        assertThat(factory.getDetailPaneName(null)).isNull();
        assertThat(factory.getDetailPaneName("id")).isNull();
        assertThat(factory.getDetailPaneName(BreakpointDetailPane.ID)).isEqualTo(BreakpointDetailPane.NAME);
    }

    @Test
    public void properDetailPaneDescriptionIsProvided_forGivenIds() {
        assertThat(factory.getDetailPaneDescription(null)).isNull();
        assertThat(factory.getDetailPaneDescription("id")).isNull();
        assertThat(factory.getDetailPaneDescription(BreakpointDetailPane.ID))
                .isEqualTo(BreakpointDetailPane.DESCRIPTION);
    }
}
