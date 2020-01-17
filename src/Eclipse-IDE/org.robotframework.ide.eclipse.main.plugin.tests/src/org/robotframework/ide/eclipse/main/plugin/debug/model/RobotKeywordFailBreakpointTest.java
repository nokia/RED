/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class RobotKeywordFailBreakpointTest {

    @AfterEach
    public void afterTest() throws CoreException {
        ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(RobotKeywordFailBreakpoint.MARKER_ID, true, 1);
    }

    @Test
    public void checkPropertiesOfNewlyCreatedBreakpoint() throws CoreException {
        final RobotKeywordFailBreakpoint breakpoint = new RobotKeywordFailBreakpoint("Keyword*");

        assertThat(breakpoint.isEnabled()).isTrue();
        assertThat(breakpoint.getNamePattern()).isEqualTo("Keyword*");
        assertThat(breakpoint.isHitCountEnabled()).isFalse();
        assertThat(breakpoint.getHitCount()).isEqualTo(1);
        assertThat(breakpoint.isConditionEnabled()).isFalse();
        assertThat(breakpoint.getConditionExpression()).isEmpty();
    }

    @Test
    public void idOfBreakpointIsEqualToRobotDebugModelId() throws CoreException {
        assertThat(new RobotKeywordFailBreakpoint().getModelIdentifier()).isEqualTo(RobotDebugElement.DEBUG_MODEL_ID);
    }

    @Test
    public void isHitCountEnabledIsTakenFromMarker() throws CoreException {
        final IMarker markerWithHitCount = mock(IMarker.class);
        when(markerWithHitCount.getAttribute(RobotLineBreakpoint.HIT_COUNT_ENABLED_ATTRIBUTE, false))
                .thenReturn(Boolean.TRUE);

        assertThat(new RobotKeywordFailBreakpoint((IMarker) null).isHitCountEnabled()).isFalse();
        assertThat(new RobotKeywordFailBreakpoint(markerWithHitCount).isHitCountEnabled()).isTrue();
    }

    @Test
    public void isHitCountEnabledIsSetProperly() throws CoreException {
        final RobotKeywordFailBreakpoint breakpoint = new RobotKeywordFailBreakpoint("Keyword*");

        breakpoint.setHitCountEnabled(false);
        assertThat(breakpoint.isHitCountEnabled()).isFalse();
        breakpoint.setHitCountEnabled(true);
        assertThat(breakpoint.isHitCountEnabled()).isTrue();
        breakpoint.setHitCountEnabled(false);
        assertThat(breakpoint.isHitCountEnabled()).isFalse();
    }

    @Test
    public void hitCountIsTakenFromMarker() throws CoreException {
        final IMarker markerWithHitCount = mock(IMarker.class);
        when(markerWithHitCount.getAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1)).thenReturn(42);

        assertThat(new RobotKeywordFailBreakpoint((IMarker) null).getHitCount()).isEqualTo(1);
        assertThat(new RobotKeywordFailBreakpoint(markerWithHitCount).getHitCount()).isEqualTo(42);
    }

    @Test
    public void hitCountIsSetProperly() throws CoreException {
        final RobotKeywordFailBreakpoint breakpoint = new RobotKeywordFailBreakpoint("Keyword*");

        breakpoint.setHitCount(10);
        assertThat(breakpoint.getHitCount()).isEqualTo(10);

        breakpoint.setHitCount(42);
        assertThat(breakpoint.getHitCount()).isEqualTo(42);

        breakpoint.setHitCount(42);
        assertThat(breakpoint.getHitCount()).isEqualTo(42);
    }

    @Test
    public void patternIsTakenFromMarker() throws CoreException {
        final IMarker markerWithLocation = mock(IMarker.class);
        when(markerWithLocation.getAttribute(RobotKeywordFailBreakpoint.KEYWORD_NAME_PATTERN_ATTRIBUTE, ""))
                .thenReturn("Keyword*");

        assertThat(new RobotKeywordFailBreakpoint((IMarker) null).getNamePattern()).isEmpty();
        assertThat(new RobotKeywordFailBreakpoint(markerWithLocation).getNamePattern()).isEqualTo("Keyword*");
    }

    @Test
    public void patternIsSetProperly() throws CoreException {
        final RobotKeywordFailBreakpoint breakpoint = new RobotKeywordFailBreakpoint("Keyword*");

        breakpoint.setNamePattern("Other");
        assertThat(breakpoint.getNamePattern()).isEqualTo("Other");

        breakpoint.setNamePattern("Different");
        assertThat(breakpoint.getNamePattern()).isEqualTo("Different");

        breakpoint.setNamePattern("Different");
        assertThat(breakpoint.getNamePattern()).isEqualTo("Different");
    }

    @Test
    public void breakpointLabelTest() throws CoreException {
        final RobotKeywordFailBreakpoint breakpoint = new RobotKeywordFailBreakpoint("Keyword*");
        assertThat(breakpoint.getLabel()).isEqualTo("Keyword 'Keyword*' fails");
    }

    @Test
    public void hitCountIsAlwaysSatisfied_whenHitCountIsDisabled() throws Exception {
        final RobotKeywordFailBreakpoint breakpoint = new RobotKeywordFailBreakpoint("Keyword*");
        breakpoint.setHitCountEnabled(false);

        for (int i = 0; i < 20; i++) {
            assertThat(breakpoint.evaluateHitCount()).isTrue();
        }
    }

    @Test
    public void hitCountIsSatisfied_whenItIsBeingHitForTheGivenTime() throws Exception {
        final RobotKeywordFailBreakpoint breakpoint = new RobotKeywordFailBreakpoint("Keyword*");
        breakpoint.setHitCountEnabled(true);
        breakpoint.setHitCount(10);

        for (int i = 0; i < 9; i++) {
            assertThat(breakpoint.evaluateHitCount()).isFalse();
            assertThat(breakpoint.isEnabled()).isTrue();
        }
        assertThat(breakpoint.evaluateHitCount()).isTrue();
        assertThat(breakpoint.isEnabled()).isFalse();
    }

    @Test
    public void breakpointDisabledBySatisfiedCountAreReenabled() throws Exception {
        final RobotKeywordFailBreakpoint breakpoint1 = new RobotKeywordFailBreakpoint("Keyword*");
        breakpoint1.setHitCountEnabled(true);
        breakpoint1.setHitCount(10);
        final RobotKeywordFailBreakpoint breakpoint2 = new RobotKeywordFailBreakpoint("Other Keyword*");
        breakpoint2.setEnabled(false);

        for (int i = 0; i < 10; i++) {
            breakpoint1.evaluateHitCount();
        }
        assertThat(breakpoint1.isEnabled()).isFalse();
        assertThat(breakpoint2.isEnabled()).isFalse();

        breakpoint1.enableIfDisabledByHitCounter();
        breakpoint2.enableIfDisabledByHitCounter();

        assertThat(breakpoint1.isEnabled()).isTrue();
        assertThat(breakpoint2.isEnabled()).isFalse();
    }
}
