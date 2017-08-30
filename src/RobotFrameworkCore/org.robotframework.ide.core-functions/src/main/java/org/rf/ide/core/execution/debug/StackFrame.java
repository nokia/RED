/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.net.URI;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrameVariables.StackVariablesDelta;
import org.rf.ide.core.execution.debug.contexts.KeywordContext;
import org.rf.ide.core.execution.debug.contexts.SuiteContext;
import org.rf.ide.core.testdata.model.FileRegion;

public class StackFrame {

    private final String name;

    private final FrameCategory category;

    private final int level;

    private StackFrameContext context;
    private final Optional<URI> contextPath;
    private final String originalErrorMessage;

    private StackFrameVariables variables;
    private StackVariablesDelta lastDelta;
    
    private final EnumSet<StackFrameMarker> markers;

    StackFrame(final String name, final FrameCategory category, final int level, final StackFrameContext context) {
        this(name, category, level, context, () -> null);
    }

    StackFrame(final String name, final FrameCategory category, final int level, final StackFrameContext context,
            final Supplier<URI> contextPathSupplier) {
        this.name = name;
        this.category = category;
        this.level = level;
        this.context = context;
        this.originalErrorMessage = context.isErroneous() ? context.getErrorMessage().get() : "";
        this.markers = EnumSet.noneOf(StackFrameMarker.class);
        this.contextPath = Optional.ofNullable(context.getAssociatedPath().orElseGet(contextPathSupplier));
    }

    public String getName() {
        return name;
    }

    int getLevel() {
        return level;
    }

    public boolean hasCategory(final FrameCategory category) {
        return this.category == category;
    }

    public boolean isSuiteContext() {
        return hasCategory(FrameCategory.SUITE) && context instanceof SuiteContext;
    }

    public boolean isSuiteDirectoryContext() {
        return hasCategory(FrameCategory.SUITE) && context instanceof SuiteContext
                && ((SuiteContext) context).isDirectory();
    }

    public boolean isSuiteFileContext() {
        return hasCategory(FrameCategory.SUITE) && !(isSuiteDirectoryContext());
    }

    public boolean isTestContext() {
        return hasCategory(FrameCategory.TEST);
    }

    /**
     * Returns the path to source in which debugger currently works
     * 
     * @return
     */
    public Optional<URI> getCurrentSourcePath() {
        return context.getAssociatedPath();
    }

    StackFrameContext getContext() {
        return context;
    }

    /**
     * Returns the path to current context source. This is different than
     * current source because e.g. debugger can currently work in __init__ file
     * but the context can be some nested suite.
     * 
     * @return
     */
    public Optional<URI> getContextPath() {
        return contextPath;
    }

    public Optional<FileRegion> getFileRegion() {
        return context.getFileRegion();
    }

    public boolean isLibraryKeywordFrame() {
        return context instanceof KeywordContext && ((KeywordContext) context).isLibraryKeywordContext();
    }

    public boolean isErroneous() {
        return !originalErrorMessage.isEmpty() || context.isErroneous();
    }

    public String getErrorMessage() {
        final String contextMessage = context.getErrorMessage().orElse("");
        if (!contextMessage.equals(originalErrorMessage)) {
            return originalErrorMessage + contextMessage;
        } else {
            return originalErrorMessage;
        }
    }

    boolean isMarkedStepping() {
        return markers.contains(StackFrameMarker.STEPPING);
    }

    boolean isMarkedError() {
        return markers.contains(StackFrameMarker.ERROR);
    }

    void mark(final StackFrameMarker marker) {
        markers.add(marker);
    }

    void unmark(final StackFrameMarker marker) {
        markers.remove(marker);
    }

    Optional<RobotLineBreakpoint> getBreakpoint() {
        return context.getLineBreakpoint();
    }

    void moveToKeyword(final RunningKeyword keyword, final RobotBreakpointSupplier breakpointSupplier) {
        this.context = context.moveTo(keyword, breakpointSupplier);
    }

    void moveOutOfKeyword() {
        this.context = context.moveOut();
    }

    void updateVariables(final Map<Variable, VariableTypedValue> vars) {
        lastDelta = variables.update(vars);
    }

    public StackFrameVariables getVariables() {
        return variables;
    }

    void setVariables(final StackFrameVariables variables) {
        this.variables = variables;
    }

    public Optional<StackVariablesDelta> getLastDelta() {
        return Optional.ofNullable(lastDelta);
    }

    public static enum FrameCategory {
        SUITE,
        TEST,
        KEYWORD,
        FOR,
        FOR_ITEM,
    }
}
