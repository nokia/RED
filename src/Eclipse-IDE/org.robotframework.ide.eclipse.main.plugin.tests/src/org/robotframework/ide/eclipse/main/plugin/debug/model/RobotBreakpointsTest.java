/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

public class RobotBreakpointsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotLineBreakpointTest.class);

    private static IFile file;

    private final IBreakpointManager breakpointManager = mock(IBreakpointManager.class);

    private final RobotBreakpoints breakpoints = new RobotBreakpoints(breakpointManager);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        file = projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                "case",
                "  Log  10");
    }

    @Before
    public void beforeTest() {
        reset(breakpointManager);
    }

    @After
    public void after() throws CoreException {
        file.deleteMarkers(RobotLineBreakpoint.MARKER_ID, true, 1);
    }

    @AfterClass
    public static void afterSuite() {
        file = null;
    }

    @Test
    public void breakpointForLineAndUriIsProperlyReturned() throws Exception {
        final IMarker marker = createMarker(file, true);
        marker.setAttribute(IMarker.LINE_NUMBER, 42);
        
        final RobotLineBreakpoint bp = new RobotLineBreakpoint(marker);
        when(breakpointManager.isEnabled()).thenReturn(true);
        when(breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)).thenReturn(new IBreakpoint[] { bp });

        final Optional<org.rf.ide.core.execution.debug.RobotLineBreakpoint> foundBreakpoint = breakpoints
                .getBreakpointAtLine(42, file.getLocationURI());
        assertThat(foundBreakpoint).contains(bp);
    }

    @Test
    public void breakpointIsNotFoundIfItIsDisabled() throws Exception {
        final IMarker marker = createMarker(file, false);
        marker.setAttribute(IMarker.LINE_NUMBER, 42);

        final RobotLineBreakpoint bp = new RobotLineBreakpoint(marker);
        when(breakpointManager.isEnabled()).thenReturn(true);
        when(breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)).thenReturn(new IBreakpoint[] { bp });

        final Optional<org.rf.ide.core.execution.debug.RobotLineBreakpoint> foundBreakpoint = breakpoints
                .getBreakpointAtLine(42, file.getLocationURI());
        assertThat(foundBreakpoint).isEmpty();
    }

    @Test
    public void breakpointIsNotFoundIfItHasOtherLine() throws Exception {
        final IMarker marker = createMarker(file, true);
        marker.setAttribute(IMarker.LINE_NUMBER, 43);

        final RobotLineBreakpoint bp = new RobotLineBreakpoint(marker);
        when(breakpointManager.isEnabled()).thenReturn(true);
        when(breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)).thenReturn(new IBreakpoint[] { bp });

        final Optional<org.rf.ide.core.execution.debug.RobotLineBreakpoint> foundBreakpoint = breakpoints
                .getBreakpointAtLine(42, file.getLocationURI());
        assertThat(foundBreakpoint).isEmpty();
    }

    @Test
    public void breakpointIsNotFoundIfItHasOtherSource() throws Exception {
        final IMarker marker = createMarker(file, true);
        marker.setAttribute(IMarker.LINE_NUMBER, 42);

        final RobotLineBreakpoint bp = new RobotLineBreakpoint(marker);
        when(breakpointManager.isEnabled()).thenReturn(true);
        when(breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)).thenReturn(new IBreakpoint[] { bp });

        final Optional<org.rf.ide.core.execution.debug.RobotLineBreakpoint> foundBreakpoint = breakpoints
                .getBreakpointAtLine(42, URI.create("file:///different.robot"));
        assertThat(foundBreakpoint).isEmpty();
    }

    @Test
    public void breakpointIsNotFoundIfBreakpointsAreGloballyDisabled() throws Exception {
        final IMarker marker = createMarker(file, true);
        marker.setAttribute(IMarker.LINE_NUMBER, 42);

        final RobotLineBreakpoint bp = new RobotLineBreakpoint(marker);
        when(breakpointManager.isEnabled()).thenReturn(false);
        when(breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)).thenReturn(new IBreakpoint[] { bp });

        final Optional<org.rf.ide.core.execution.debug.RobotLineBreakpoint> foundBreakpoint = breakpoints
                .getBreakpointAtLine(42, file.getLocationURI());
        assertThat(foundBreakpoint).isEmpty();
    }

    @Test
    public void robotLineBreakpointsAreEnabled_ifTheyWereDisabledDueToHitCountStop() throws Exception {
        final IBreakpoint bp1 = mock(IBreakpoint.class);

        final RobotLineBreakpoint bp2 = mock(RobotLineBreakpoint.class);
        doThrow(new CoreException(mock(IStatus.class))).when(bp2).enableIfDisabledByHitCounter();

        final RobotLineBreakpoint bp3 = new RobotLineBreakpoint(createMarker(false));
        bp3.setDisabledDueToHitCounter(true);

        final RobotLineBreakpoint bp4 = new RobotLineBreakpoint(createMarker(false));
        bp4.setDisabledDueToHitCounter(false);

        when(breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID))
                .thenReturn(new IBreakpoint[] { bp1, bp3, bp4 });

        breakpoints.enableBreakpointsDisabledByHitCounter();

        assertThat(bp3.isEnabled()).isTrue();
        assertThat(bp4.isEnabled()).isFalse();
    }

    private IMarker createMarker(final boolean enabled) {
        return createMarker(null, enabled);
    }

    private IMarker createMarker(final IResource resource, final boolean enabled) {
        final MockMarker marker = new MockMarker(resource);
        marker.setAttribute(IBreakpoint.ENABLED, enabled);
        return marker;
    }
}
