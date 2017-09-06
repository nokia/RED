/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static com.google.common.base.Predicates.or;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.KeywordEndedEvent;
import org.rf.ide.core.execution.agent.event.KeywordStartedEvent;
import org.rf.ide.core.execution.agent.event.ResourceImportEvent;
import org.rf.ide.core.execution.agent.event.SuiteEndedEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;
import org.rf.ide.core.execution.agent.event.VariablesEvent;
import org.rf.ide.core.execution.agent.event.VersionsEvent;
import org.rf.ide.core.execution.debug.KeywordCallType.KeywordsTypesFixer;
import org.rf.ide.core.execution.debug.KeywordCallType.KeywordsTypesForRf29Fixer;
import org.rf.ide.core.execution.debug.StackFrame.FrameCategory;
import org.rf.ide.core.execution.debug.contexts.ForLoopContext;
import org.rf.ide.core.execution.debug.contexts.ForLoopIterationContext;
import org.rf.ide.core.execution.debug.contexts.KeywordContext;
import org.rf.ide.core.execution.debug.contexts.SuiteContext;
import org.rf.ide.core.execution.debug.contexts.TestCaseContext;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

public class StacktraceBuilder extends RobotDefaultAgentEventListener {

    private final Stacktrace stacktrace;

    private final ElementsLocator locator;

    private final RobotBreakpointSupplier breakpointSupplier;

    private KeywordsTypesFixer keywordsTypesFixer;

    private final Set<URI> currentlyImportedResources = new LinkedHashSet<>();

    public StacktraceBuilder(final Stacktrace stacktrace, final ElementsLocator locator,
            final RobotBreakpointSupplier breakpointSupplier) {
        this.stacktrace = stacktrace;
        this.locator = locator;
        this.breakpointSupplier = breakpointSupplier;
    }

    @Override
    public void handleVersions(final VersionsEvent event) {
        final RobotVersion version = RobotVersion.from(event.getRobotVersion());
        this.keywordsTypesFixer = version.isOlderThan(new RobotVersion(3, 0))
                ? new KeywordsTypesForRf29Fixer()
                : new KeywordsTypesFixer();
    }

    @Override
    public void handleSuiteStarted(final SuiteStartedEvent event) {
        final URI currentPath = stacktrace.getContextPath().orElse(null);
        final String suiteName = event.getName();
        final URI suitePath = event.getPath();
        final boolean suiteIsDirectory = event.isDirectory();

        final SuiteContext context = locator.findContextForSuite(suiteName, suitePath, suiteIsDirectory, currentPath);
        stacktrace.push(
                new StackFrame(suiteName, FrameCategory.SUITE, stacktrace.size(), context, currentlyImportedResources));

        currentlyImportedResources.clear();
    }

    @Override
    public void handleTestStarted(final TestStartedEvent event) {
        final String testName = event.getName();
        final Optional<String> template = event.getTemplate();

        final URI path = stacktrace.getContextPath().orElse(null);
        final TestCaseContext context = locator.findContextForTestCase(testName, path, template);
        stacktrace.push(new StackFrame(testName, FrameCategory.TEST, stacktrace.size(), context));
    }

    @Override
    public void handleKeywordAboutToStart(final KeywordStartedEvent event) {
        final StackFrameContext context = stacktrace.peekCurrentFrame().map(StackFrame::getContext).orElse(null);
        final RunningKeyword keyword = keywordsTypesFixer.keywordStarting(event.getRunningKeyword(), context);

        stacktrace.peekCurrentFrame().ifPresent(frame -> frame.moveToKeyword(keyword, breakpointSupplier));
    }

    @Override
    public void handleKeywordStarted(final KeywordStartedEvent event) {
        final String keywordName = event.getName();
        final String libraryName = event.getLibraryName();
        final RunningKeyword keyword = keywordsTypesFixer.keywordStarted(event.getRunningKeyword());

        final boolean isSuiteSetupTeardown = (keyword.isSetup() || keyword.isTeardown())
                && stacktrace.hasCategoryOnTop(FrameCategory.SUITE);
        final URI currentSuitePath = isSuiteSetupTeardown
                ? stacktrace.getCurrentPath().orElse(null)
                : stacktrace.getContextPath().orElse(null);
        
        final String frameNamePrefix;
        final FrameCategory category;
        final int level;
        final StackFrameContext context;
        final Supplier<URI> contextPathSupplier;
        if (keyword.isForLoop()) {
            final StackFrameContext currentContext = stacktrace.peekCurrentFrame().get().getContext();
            context = ForLoopContext.findContextForLoop(currentContext);
            category = FrameCategory.FOR;
            level = stacktrace.peekCurrentFrame().get().getLevel();
            frameNamePrefix = ":FOR ";
            contextPathSupplier = () -> null;

        } else if (stacktrace.hasCategoryOnTop(FrameCategory.FOR)) {
            final StackFrameContext currentContext = stacktrace.peekCurrentFrame().get().getContext();
            context = ForLoopIterationContext.findContextForLoopIteration(currentContext, keywordName);
            category = FrameCategory.FOR_ITEM;
            level = stacktrace.peekCurrentFrame().get().getLevel();
            frameNamePrefix = ":FOR iteration ";
            contextPathSupplier = () -> null;

        } else {
            final Set<URI> currentResources = stacktrace
                    .getFirstFrameSatisfying(StackFrame::isSuiteContext)
                    .get()
                    .getLoadedResources();
            final KeywordContext kwContext = locator.findContextForKeyword(libraryName, keywordName, currentSuitePath,
                    currentResources);
            context = kwContext;
            category = FrameCategory.KEYWORD;

            // library keyword have to point to variables taken from lowest test or suite if test
            // does not exist
            level = kwContext.isLibraryKeywordContext()
                    ? stacktrace.getFirstFrameSatisfying(or(StackFrame::isTestContext, StackFrame::isSuiteContext))
                            .map(StackFrame::getLevel)
                            .get()
                    : stacktrace.peekCurrentFrame().get().getLevel() + 1;
            frameNamePrefix = "";
            contextPathSupplier = () -> currentSuitePath;
        }

        final String frameName = frameNamePrefix + QualifiedKeywordName.asCall(keywordName, libraryName);
        stacktrace.push(new StackFrame(frameName, category, level, context, contextPathSupplier));
    }

    @Override
    public void handleKeywordAboutToEnd(final KeywordEndedEvent event) {
        stacktrace.pop();
    }

    @Override
    public void handleKeywordEnded(final KeywordEndedEvent event) {
        stacktrace.peekCurrentFrame().ifPresent(frame -> frame.moveOutOfKeyword());

        keywordsTypesFixer.keywordEnded();
    }

    @Override
    public void handleTestEnded(final TestEndedEvent event) {
        stacktrace.pop();
    }

    @Override
    public void handleSuiteEnded(final SuiteEndedEvent event) {
        stacktrace.pop();
    }

    @Override
    public void handleResourceImport(final ResourceImportEvent event) {
        if (event.isDynamicallyImported()) {
            stacktrace.getFirstFrameSatisfying(StackFrame::isSuiteContext).get().addLoadedResource(event.getPath());
        } else {
            // if resources is imported normally it is done prior to suite start
            currentlyImportedResources.add(event.getPath());
        }
    }

    @Override
    public void handleVariables(final VariablesEvent event) {
        stacktrace.updateVariables(event.getVariables());
    }

    @Override
    public void handleClosed() {
        stacktrace.destroy();
    }
}
