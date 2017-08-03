/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrameVariables.StackVariablesDelta;
import org.rf.ide.core.execution.debug.contexts.SuiteContext;
import org.rf.ide.core.testdata.model.FileRegion;

public class StackFrame {

    private final String name;

    private final FrameCategory category;

    private final int level;

    private StackFrameContext context;

    private final StackFrameVariables variables;

    private final List<VariablesChangesListener> variablesChangesListeners;

    private final List<ContextChangesListener> contextChangesListeners;

    private final EnumSet<StackFrameMarker> markers;

    private final Optional<URI> contextPath;

    private final String originalErrorMessage;

    StackFrame(final String name, final FrameCategory category, final int level, final StackFrameContext context,
            final StackFrameVariables variables) {
        this(name, category, level, context, variables, () -> null);
    }

    StackFrame(final String name, final FrameCategory category, final int level, final StackFrameContext context,
            final StackFrameVariables variables, final Supplier<URI> contextPathSupplier) {
        this.name = name;
        this.category = category;
        this.level = level;
        this.context = context;
        this.originalErrorMessage = context.isErroneous() ? context.getErrorMessage().get() : "";
        this.variables = variables;
        this.variablesChangesListeners = new ArrayList<>();
        this.contextChangesListeners = new ArrayList<>();
        this.markers = EnumSet.noneOf(StackFrameMarker.class);
        this.contextPath = Optional.ofNullable(context.getAssociatedPath().orElseGet(contextPathSupplier));
    }

    public void addVariablesChangesListener(final VariablesChangesListener listener) {
        variablesChangesListeners.add(listener);
    }

    public void removeVariablesChangesListener(final VariablesChangesListener listener) {
        variablesChangesListeners.remove(listener);
    }

    public void addContextChangesListener(final ContextChangesListener listener) {
        contextChangesListeners.add(listener);
    }

    public void removeContextChangesListener(final ContextChangesListener listener) {
        contextChangesListeners.remove(listener);
    }

    public void destroy() {
        variables.clear();
        variablesChangesListeners.clear();
        contextChangesListeners.clear();
    }

    public String getName() {
        return name;
    }

    public boolean hasCategory(final FrameCategory category) {
        return this.category == category;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Returns the path to source in which debugger currently works
     * 
     * @return
     */
    public Optional<URI> getCurrentSourcePath() {
        return context.getAssociatedPath();
    }

    public StackFrameContext getContext() {
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

    public boolean isLibraryKeywordFrame() {
        return context.isLibraryKeywordContext();
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

    public boolean isMarkedStepping() {
        return markers.contains(StackFrameMarker.STEPPING);
    }

    public boolean isMarkedError() {
        return markers.contains(StackFrameMarker.ERROR);
    }

    public void mark(final StackFrameMarker marker) {
        markers.add(marker);
    }

    public void unmark(final StackFrameMarker marker) {
        markers.remove(marker);
    }

    public Optional<RobotLineBreakpoint> getBreakpoint() {
        return context.getLineBreakpoint();
    }

    public void moveToKeyword(final RunningKeyword keyword, final RobotBreakpointSupplier breakpointSupplier) {
        final Optional<URI> previousPath = context.getAssociatedPath();
        this.context = context.moveTo(keyword, breakpointSupplier);

        if (!previousPath.equals(context.getAssociatedPath())) {
            contextChangesListeners.stream().forEach(listener -> listener.contextChanged());
        }
    }

    public void moveOutOfKeyword() {
        final Optional<URI> previousPath = context.getAssociatedPath();
        this.context = context.moveOut();

        if (!previousPath.equals(context.getAssociatedPath())) {
            contextChangesListeners.stream().forEach(listener -> listener.contextChanged());
        }
    }

    public StackVariablesDelta updateVariables(final Map<Variable, VariableTypedValue> vars) {
        final StackVariablesDelta delta = variables.update(vars);
        variablesChangesListeners.stream().forEach(listener -> listener.variablesChanged(delta));
        return delta;
    }

    public StackFrameVariables getVariables() {
        return variables;
    }

    @FunctionalInterface
    public static interface VariablesChangesListener {

        void variablesChanged(StackVariablesDelta variablesDelta);

    }

    @FunctionalInterface
    public static interface ContextChangesListener {

        void contextChanged();
    }

    public static enum FrameCategory {
        SUITE,
        TEST,
        KEYWORD,
        FOR,
        FOR_ITEM,
    }
}
