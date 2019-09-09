/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.rf.ide.core.execution.debug.RobotBreakpoint;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;

import com.google.common.annotations.VisibleForTesting;

public class RobotBreakpoints extends RobotBreakpointSupplier {

    private final IBreakpointManager breakpointManager;

    public RobotBreakpoints() {
        this(DebugPlugin.getDefault().getBreakpointManager());
    }

    @VisibleForTesting
    RobotBreakpoints(final IBreakpointManager breakpointManager) {
        this.breakpointManager = breakpointManager;
    }

    public void enableBreakpointsDisabledByHitCounter() {
        for (final IBreakpoint breakpoint : breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)) {
            try {
                if (breakpoint instanceof RobotLineBreakpoint) {
                    ((RobotLineBreakpoint) breakpoint).enableIfDisabledByHitCounter();
                } else if (breakpoint instanceof RobotKeywordFailBreakpoint) {
                    ((RobotKeywordFailBreakpoint) breakpoint).enableIfDisabledByHitCounter();
                }
            } catch (final CoreException e) {
                // we'll look for next breakpoints
            }
        }
    }

    @Override
    public Optional<RobotBreakpoint> lineBreakpointFor(final URI fileUri,
            final int lineNumber) {
        if (!breakpointManager.isEnabled()) {
            return Optional.empty();
        }
        return Stream.of(breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID))
                .filter(RobotLineBreakpoint.class::isInstance)
                .map(RobotLineBreakpoint.class::cast)
                .filter(RobotBreakpoints::isEnabled)
                .filter(bp -> bp.getMarker().getResource().getLocationURI().equals(fileUri))
                .filter(bp -> getLineNumber(bp) == lineNumber)
                .map(RobotBreakpoint.class::cast)
                .findFirst();
    }

    @Override
    public Optional<RobotBreakpoint> keywordFailBreakpointFor(final String keywordName) {
        if (!breakpointManager.isEnabled()) {
            return Optional.empty();
        }
        return Stream.of(breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID))
                .filter(RobotKeywordFailBreakpoint.class::isInstance)
                .map(RobotKeywordFailBreakpoint.class::cast)
                .filter(RobotBreakpoints::isEnabled)
                .filter(bp -> bp.matchesKeyword(keywordName))
                .map(RobotBreakpoint.class::cast)
                .findFirst();
    }

    private static boolean isEnabled(final IBreakpoint bp) {
        try {
            return bp.isEnabled();
        } catch (final CoreException e) {
            return false;
        }
    }

    private static int getLineNumber(final ILineBreakpoint bp) {
        try {
            return bp.getLineNumber();
        } catch (final CoreException e) {
            return -1;
        }
    }
}
