/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.unmodifiableSet;

import java.net.URI;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrameVariables.StackVariablesDelta;
import org.rf.ide.core.execution.debug.contexts.KeywordContext;
import org.rf.ide.core.execution.debug.contexts.SuiteContext;
import org.rf.ide.core.testdata.model.FileRegion;

import com.google.common.base.Preconditions;

public class StackFrame {

    private final String name;

    private final FrameCategory category;

    private final int level;

    private StackFrameContext context;
    private final Optional<URI> contextPath;
    private final String originalErrorMessage;

    private StackFrameVariables variables;
    private StackVariablesDelta lastDelta;

    private final LinkedHashSet<URI> loadedResources;
    
    private final EnumSet<StackFrameMarker> markers;

    StackFrame(final String name, final FrameCategory category, final int level, final StackFrameContext context,
            final Set<URI> loadedResources) {
        this(name, category, level, context, () -> null, loadedResources);
    }

    StackFrame(final String name, final FrameCategory category, final int level, final StackFrameContext context) {
        this(name, category, level, context, () -> null, new LinkedHashSet<>());
    }

    StackFrame(final String name, final FrameCategory category, final int level, final StackFrameContext context,
            final Supplier<URI> contextPathSupplier) {
        this(name, category, level, context, contextPathSupplier, new LinkedHashSet<>());
    }

    private StackFrame(final String name, final FrameCategory category, final int level,
            final StackFrameContext context, final Supplier<URI> contextPathSupplier, final Set<URI> loadedResources) {
        this.name = name;
        this.category = category;
        this.level = level;
        this.context = context;
        this.contextPath = Optional.ofNullable(context.getAssociatedPath().orElseGet(contextPathSupplier));
        this.originalErrorMessage = context.isErroneous() ? context.getErrorMessage().get() : "";
        this.loadedResources = newLinkedHashSet(loadedResources);
        this.markers = EnumSet.noneOf(StackFrameMarker.class);
    }

    public String getName() {
        return name;
    }

    int getLevel() {
        return level;
    }

    Set<URI> getLoadedResources() {
        return unmodifiableSet(loadedResources);
    }

    void addLoadedResource(final URI resourceUri) {
        Preconditions.checkState(isSuiteContext());
        loadedResources.add(resourceUri);
    }

    public boolean hasCategory(final FrameCategory category) {
        return this.category == category;
    }

    public boolean isSuiteContext() {
        return hasCategory(FrameCategory.SUITE);
    }

    public boolean isSuiteDirectoryContext() {
        if (isSuiteContext()) {
            if (context instanceof SuiteContext) {
                return ((SuiteContext) context).isDirectory();

            } else if (context.previousContext() instanceof SuiteContext) {
                // suite context may have been moved to setup/teardown context
                return ((SuiteContext) context.previousContext()).isDirectory();
            }
        }
        return false;
    }

    public boolean isSuiteFileContext() {
        if (isSuiteContext()) {
            if (context instanceof SuiteContext) {
                return !((SuiteContext) context).isDirectory();

            } else if (context.previousContext() instanceof SuiteContext) {
                // suite context may have been moved to setup/teardown context
                return !((SuiteContext) context.previousContext()).isDirectory();
            }
        }
        return false;
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
        return hasCategory(FrameCategory.KEYWORD) && context instanceof KeywordContext
                && ((KeywordContext) context).isLibraryKeywordContext();
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
        this.context = context.previousContext();
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
