/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;
import org.rf.ide.core.execution.debug.RobotBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotBreakpointDetailPane.BreakpointAttributeException;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotBreakpointDetailPane.BreakpointValidationException;

import com.google.common.annotations.VisibleForTesting;

public class RobotKeywordFailBreakpoint extends Breakpoint implements RobotBreakpoint {
    
    @VisibleForTesting
    static final String MARKER_ID = "org.robotframework.ide.eclipse.main.plugin.robot.keywordFailBreakpoint.marker";

    @VisibleForTesting static final String KEYWORD_NAME_PATTERN_ATTRIBUTE = "robot.breakpoint.keyword.fail.namePattern";

    public static void validate(final String pattern) {
        validate(pattern, null);
    }

    public static void validate(final String pattern, final IBreakpoint currentBreakpoint) {
        if (pattern.isEmpty()) {
            throw new BreakpointValidationException("Keyword name cannot be empty");
        } else if (pattern.contains("  ") || pattern.contains("\t")) {
            throw new BreakpointValidationException("Keyword name cannot contain multiple spaces or tab character");
        }
        final IBreakpointManager bpManager = DebugPlugin.getDefault().getBreakpointManager();
        for (final IBreakpoint breakpoint : bpManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)) {
            if (breakpoint instanceof RobotKeywordFailBreakpoint) {
                final RobotKeywordFailBreakpoint bp = (RobotKeywordFailBreakpoint) breakpoint;

                if (bp != currentBreakpoint && pattern.equals(bp.getNamePattern())) {
                    throw new BreakpointValidationException(
                            "There is already breakpoint defined for '" + pattern + "' name");
                }
            }
        }
    }

    private long currentHitCounter = 0;
    private boolean isDisabledDueToHitCounter = false;

    public RobotKeywordFailBreakpoint() {
    }

    @VisibleForTesting
    RobotKeywordFailBreakpoint(final IMarker marker) throws CoreException {
        setMarker(marker);
    }

    /**
     * Constructs a keyword fail breakpoint with given keyword name pattern
     * 
     * @param namePattern
     *            pattern for which keyword name should match
     * @throws CoreException
     *             if unable to create the breakpoint
     */
    public RobotKeywordFailBreakpoint(final String namePattern) throws CoreException {
        final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IWorkspaceRunnable runnable = monitor -> {
            final IMarker marker = wsRoot.createMarker(MARKER_ID);
            setMarker(marker);
            marker.setAttribute(IBreakpoint.ID, RobotDebugElement.DEBUG_MODEL_ID);
            marker.setAttribute(RobotLineBreakpoint.HIT_COUNT_ENABLED_ATTRIBUTE, false);
            marker.setAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
            marker.setAttribute(IBreakpoint.ENABLED, true);
            marker.setAttribute(KEYWORD_NAME_PATTERN_ATTRIBUTE, namePattern);
        };
        run(getMarkerRule(wsRoot), runnable);
    }

    @Override
    public String getModelIdentifier() {
        return RobotDebugElement.DEBUG_MODEL_ID;
    }

    @Override
    public void setEnabled(final boolean enabled) throws CoreException {
        if (enabled != isEnabled()) {
            currentHitCounter = 0;
            isDisabledDueToHitCounter = false;
        }
        super.setEnabled(enabled);
    }

    @VisibleForTesting
    void setDisabledDueToHitCounter(final boolean disabled) {
        this.isDisabledDueToHitCounter = disabled;
    }

    @Override
    public boolean isHitCountEnabled() {
        final IMarker marker = getMarker();
        return marker == null ? false : marker.getAttribute(RobotLineBreakpoint.HIT_COUNT_ENABLED_ATTRIBUTE, false);
    }

    @Override
    public void setHitCountEnabled(final boolean enabled) {
        try {
            if (enabled != isHitCountEnabled()) {
                setAttribute(RobotLineBreakpoint.HIT_COUNT_ENABLED_ATTRIBUTE, enabled);
            }
        } catch (final CoreException e) {
            throw new BreakpointAttributeException(e);
        }
    }

    @Override
    public int getHitCount() {
        final IMarker marker = getMarker();
        return marker == null ? 1 : marker.getAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
    }

    @Override
    public void setHitCount(final int hitCount) {
        try {
            if (hitCount != getHitCount()) {
                setAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, hitCount);
                if (isDisabledDueToHitCounter) {
                    setEnabled(true);
                }
                currentHitCounter = 0;
                isDisabledDueToHitCounter = false;
            }
        } catch (final CoreException e) {
            throw new BreakpointAttributeException(e);
        }
    }

    @Override
    public boolean evaluateHitCount() {
        // TODO : the hit counter does not depend upon launches, so the counter is "global" and
        // hence two running debug sessions may change it; consider what to do with this issue, when
        // it would be possible to launch multiple debug sessions

        if (!isHitCountEnabled()) {
            return true;
        }
        currentHitCounter++;

        if (currentHitCounter == getHitCount()) {
            try {
                setEnabled(false);
                currentHitCounter = 0;
                isDisabledDueToHitCounter = true;
            } catch (final CoreException e) {
                // alright, let it stay enabled
            }
            return true;
        }
        return false;
    }

    public void enableIfDisabledByHitCounter() throws CoreException {
        if (!isEnabled() && isDisabledDueToHitCounter) {
            setEnabled(true);
            isDisabledDueToHitCounter = false;
        }
        currentHitCounter = 0;
    }

    public String getNamePattern() {
        final IMarker marker = getMarker();
        return marker == null ? "" : marker.getAttribute(KEYWORD_NAME_PATTERN_ATTRIBUTE, "");
    }

    public boolean matchesKeyword(final String keywordName) {
        final String namePattern = getNamePattern();
        final StringBuilder pattern = new StringBuilder();

        StringBuilder part = new StringBuilder();
        for (int i = 0; i < namePattern.length(); i++) {
            final char ch = namePattern.charAt(i);

            if (ch == '?' || ch == '*') {
                if (part.length() > 0) {
                    pattern.append(Pattern.quote(part.toString()));
                    part = new StringBuilder();
                }
                pattern.append(".");
                if (ch == '*') {
                    pattern.append("*");
                }
            } else if (ch != ' ' && ch != '_') {
                part.append(ch);
            }
        }
        if (part.length() > 0) {
            pattern.append(Pattern.quote(part.toString()));
        }
        return Pattern.matches("(?iu)^" + pattern.toString() + "$", keywordName);
    }

    public void setNamePattern(final String pattern) throws CoreException {
        if (!pattern.equals(getNamePattern())) {
            setAttribute(KEYWORD_NAME_PATTERN_ATTRIBUTE, pattern);
        }

    }

    public String getLabel() throws CoreException {
        String label = "Keyword '" + getNamePattern() + "' fails";

        if (isHitCountEnabled()) {
            label += " [hit count: " + getHitCount() + "]";
        }
        return label;
    }

    public static class InvalidBreakpointPatternException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public InvalidBreakpointPatternException(final String message) {
            super(message);
        }
    }
}
