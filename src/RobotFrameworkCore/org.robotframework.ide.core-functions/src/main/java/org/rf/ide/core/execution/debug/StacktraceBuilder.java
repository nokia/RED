/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.net.URI;
import java.util.Optional;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.KeywordEndedEvent;
import org.rf.ide.core.execution.agent.event.KeywordStartedEvent;
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
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

public class StacktraceBuilder extends RobotDefaultAgentEventListener {

    private final Stacktrace stacktrace;

    private final ElementsLocator locator;

    private final RobotBreakpointSupplier breakpointSupplier;

    private KeywordsTypesFixer keywordsTypesFixer;

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

        final StackFrameContext context = locator.findContextForSuite(suiteName, suitePath, suiteIsDirectory,
                currentPath);
        stacktrace.push(new StackFrame(suiteName, FrameCategory.SUITE, stacktrace.size(), context));
    }

    @Override
    public void handleTestStarted(final TestStartedEvent event) {
        final String testName = event.getName();
        final Optional<String> template = event.getTemplate();

        final URI path = stacktrace.getContextPath().orElse(null);
        final StackFrameContext context = locator.findContextForTestCase(testName, path, template);
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
        if (keyword.isForLoop()) {
            final StackFrameContext currentContext = stacktrace.peekCurrentFrame().get().getContext();
            context = ForLoopContext.findContextForLoop(currentContext);
            category = FrameCategory.FOR;
            level = stacktrace.peekCurrentFrame().get().getLevel();
            frameNamePrefix = ":FOR ";
        } else if (stacktrace.hasCategoryOnTop(FrameCategory.FOR)) {
            final StackFrameContext currentContext = stacktrace.peekCurrentFrame().get().getContext();
            context = ForLoopIterationContext.findContextForLoopIteration(currentContext, keywordName);
            category = FrameCategory.FOR_ITEM;
            level = stacktrace.peekCurrentFrame().get().getLevel();
            frameNamePrefix = ":FOR iteration ";
        } else {
            context = locator.findContextForKeyword(libraryName, keywordName, currentSuitePath);
            category = FrameCategory.KEYWORD;
            // library keyword have to point to variables taken from lowest test or suite if test
            // does not exist
            level = context.isLibraryKeywordContext()
                    ? stacktrace.getFirstTestOrSuiteFrame().map(StackFrame::getLevel).get()
                    : stacktrace.peekCurrentFrame().get().getLevel() + 1;
            
            frameNamePrefix = "";
        }

        final String frameName = frameNamePrefix + QualifiedKeywordName.asCall(keywordName, libraryName);
        final StackFrame frame = context.isLibraryKeywordContext()
                ? new StackFrame(frameName, category, level, context, () -> currentSuitePath)
                : new StackFrame(frameName, category, level, context);

        stacktrace.push(frame);
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
    public void handleVariables(final VariablesEvent event) {
        stacktrace.updateVariables(event.getVariables());
    }

    @Override
    public void handleClosed() {
        stacktrace.destroy();
    }
}
