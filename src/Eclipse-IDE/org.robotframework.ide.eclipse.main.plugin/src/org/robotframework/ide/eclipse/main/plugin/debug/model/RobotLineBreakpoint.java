/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author mmarzec
 *
 */
public class RobotLineBreakpoint extends LineBreakpoint implements org.rf.ide.core.execution.debug.RobotLineBreakpoint {
    
    @VisibleForTesting static final String MARKER_ID = "org.robotframework.ide.eclipse.main.plugin.robot.lineBreakpoint.marker";

    @VisibleForTesting static final String HIT_COUNT_ENABLED_ATTRIBUTE = "robot.breakpoint.hit.count.enablement";
    @VisibleForTesting static final String HIT_COUNT_ATTRIBUTE = "robot.breakpoint.hit.count";

    @VisibleForTesting static final String CONDITION_ENABLED_ATTRIBUTE = "robot.breakpoint.conditional.enablement";
    @VisibleForTesting static final String CONDITION_ATTRIBUTE = "robot.breakpoint.conditional";

    private long currentHitCounter = 0;
    private boolean isDisabledDueToHitCounter = false;

    /**
     * Default constructor is required for the breakpoint manager
     * to re-create persisted breakpoints. After instantiating a breakpoint,
     * the <code>setMarker(...)</code> method is called to restore
     * this breakpoint's attributes.
     */
    public RobotLineBreakpoint() {
    }

    @VisibleForTesting
    RobotLineBreakpoint(final IMarker marker) throws CoreException {
        setMarker(marker);
    }

    /**
     * Constructs a line breakpoint on the given resource at the given
     * line number. The line number is 1-based.
     * 
     * @param resource
     *            file on which to set the breakpoint
     * @param lineNumber
     *            1-based line number of the breakpoint
     * @throws CoreException
     *             if unable to create the breakpoint
     */
    public RobotLineBreakpoint(final IResource resource, final int lineNumber) throws CoreException {
        final IWorkspaceRunnable runnable = monitor -> {
            final IMarker marker = resource.createMarker(MARKER_ID);
            setMarker(marker);
            marker.setAttribute(IBreakpoint.ID, RobotDebugElement.DEBUG_MODEL_ID);
            marker.setAttribute(IBreakpoint.ENABLED, true);
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            marker.setAttribute(IMarker.LOCATION, resource.getName());
            marker.setAttribute(HIT_COUNT_ENABLED_ATTRIBUTE, false);
            marker.setAttribute(HIT_COUNT_ATTRIBUTE, 1);
            marker.setAttribute(CONDITION_ENABLED_ATTRIBUTE, false);
            marker.setAttribute(CONDITION_ATTRIBUTE, "");
        };
        run(getMarkerRule(resource), runnable);
    }


    @Override
    public String getModelIdentifier() {
        return RobotDebugElement.DEBUG_MODEL_ID;
    }

    String getLocation() {
        final IMarker marker = getMarker();
        return marker == null ? "" : marker.getAttribute(IMarker.LOCATION, "");
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

    public boolean isHitCountEnabled() {
        final IMarker marker = getMarker();
        return marker == null ? false : marker.getAttribute(HIT_COUNT_ENABLED_ATTRIBUTE, false);
    }

    public void setHitCountEnabled(final boolean enabled) throws CoreException {
        if (enabled != isHitCountEnabled()) {
            setAttribute(HIT_COUNT_ENABLED_ATTRIBUTE, enabled);
        }
    }

    public int getHitCount() {
        final IMarker marker = getMarker();
        return marker == null ? 1 : marker.getAttribute(HIT_COUNT_ATTRIBUTE, 1);
    }

    public void setHitCount(final int hitCount) throws CoreException {
        if (hitCount != getHitCount()) {
            setAttribute(HIT_COUNT_ATTRIBUTE, hitCount);
            if (isDisabledDueToHitCounter) {
                setEnabled(true);
            }
            currentHitCounter = 0;
            isDisabledDueToHitCounter = false;
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

    @Override
    public boolean isConditionEnabled() {
        final IMarker marker = getMarker();
        return marker == null ? false : marker.getAttribute(CONDITION_ENABLED_ATTRIBUTE, false);
    }

    public void setConditionEnabled(final boolean enabled) throws CoreException {
        if (enabled != isConditionEnabled()) {
            setAttribute(CONDITION_ENABLED_ATTRIBUTE, enabled);
        }
    }

    @Override
    public String getCondition() {
        final IMarker marker = getMarker();
        return marker == null ? "" : marker.getAttribute(CONDITION_ATTRIBUTE, "");
    }

    public void setCondition(final String condition) throws CoreException {
        if (!condition.equals(getCondition())) {
            setAttribute(CONDITION_ATTRIBUTE, condition);
        }
    }

    public String getLabel() throws CoreException {
        String label = getLocation() + " [line: " + getLineNumber() + "]";

        if (isHitCountEnabled()) {
            label += " [hit count: " + getHitCount() + "]";
        }
        if (isConditionEnabled()) {
            label += " [conditional]";
        }
        return label;
    }
}
