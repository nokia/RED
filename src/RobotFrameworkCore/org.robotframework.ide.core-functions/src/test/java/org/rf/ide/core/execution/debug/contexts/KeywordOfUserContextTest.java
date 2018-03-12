/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class KeywordOfUserContextTest {

    @Test
    public void userKeywordContextHasNoSourceAssociatedIfNotProvided() {
        final UserKeyword keyword = new UserKeyword(RobotToken.create("keyword"));
        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, null, newArrayList());

        assertThat(context.getAssociatedPath()).isEmpty();
        assertThat(context.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
    }

    @Test
    public void userKeywordContextContextHasSourceAssociatedWhenProvided() {
        final RobotToken token = RobotToken.create("keyword");
        token.setFilePosition(new FilePosition(42, 0, 1000));
        final UserKeyword keyword = new UserKeyword(token);

        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, URI.create("file:///file.robot"),
                newArrayList());

        assertThat(context.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(context.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
    }

    @Test
    public void itIsIllegalToMoveToSetup_becauseKeywordsDontHaveSetups() {
        final UserKeyword keyword = new UserKeyword(RobotToken.create("keyword"));
        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, URI.create("file:///file.robot"),
                newArrayList());

        assertThatExceptionOfType(IllegalDebugContextStateException.class)
                .isThrownBy(() -> context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                        mock(RobotBreakpointSupplier.class)))
                .withMessage("Setup or Teardown keyword cannot be called when user keyword is about to start")
                .withNoCause();
    }

    @Test
    public void itIsIllegalToMoveToTeardown_becauseThereHasToBeSomeExecutableCalledBefore() {
        final UserKeyword keyword = new UserKeyword(RobotToken.create("keyword"));
        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, URI.create("file:///file.robot"),
                newArrayList());

        assertThatExceptionOfType(IllegalDebugContextStateException.class)
                .isThrownBy(() -> context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                        mock(RobotBreakpointSupplier.class)))
                .withMessage("Setup or Teardown keyword cannot be called when user keyword is about to start")
                .withNoCause();
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToNormalKeywordButThereAreNoExecutablesInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final UserKeyword keyword = model.getKeywordTable().getKeywords().get(0);
        
        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, modelUri, newArrayList(model));

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToForLoopButThereAreNoExecutablesInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final UserKeyword keyword = model.getKeywordTable().getKeywords().get(0);

        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, modelUri, newArrayList(model));

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("", "${x} IN [ 1 | 2 | 3 ]", KeywordCallType.FOR),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find executable call of '${x} IN [ 1 | 2 | 3 ]' keyword\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToNormalKeywordButThereIsALoopInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "log", "${x}")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final UserKeyword keyword = model.getKeywordTable().getKeywords().get(0);

        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, modelUri, newArrayList(model));

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find executable call of 'lib.kw' keyword\n:FOR loop was found instead\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToForLoopButThereIsANormalKeywordInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable("log", "20")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final UserKeyword keyword = model.getKeywordTable().getKeywords().get(0);

        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, modelUri, newArrayList(model));

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("", "${x} IN [ 1 | 2 | 3 ]", KeywordCallType.FOR),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find :FOR loop\nAn executable was found calling 'log' keyword\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToNormalKeywordButThereIsSomeOtherKeywordInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable("log", "20")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final UserKeyword keyword = model.getKeywordTable().getKeywords().get(0);

        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, modelUri, newArrayList(model));

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find executable call of 'lib.kw' keyword\n"
                        + "An executable was found but seem to call non-matching keyword 'log'\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);

    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToForLoopButThereIsNonMatchingLoopInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "log", "${x}")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final UserKeyword keyword = model.getKeywordTable().getKeywords().get(0);

        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, modelUri, newArrayList(model));

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("", "${x} IN [ 1 | 2 | 3 | 4 ]", KeywordCallType.FOR),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find matching :FOR loop\n"
                        + "':FOR ${x} IN [ 1 | 2 | 3 ]' was found but ':FOR ${x} IN [ 1 | 2 | 3 | 4 ]' is being executed\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void validExecutableContextIsReturned_whenMovingToNormalKeywordAndMatchingOneIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable("log", "20")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final UserKeyword keyword = model.getKeywordTable().getKeywords().get(0);

        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, modelUri, newArrayList(model));

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "log", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.getAssociatedPath()).contains(modelUri);

    }

    @Test
    public void validExecutableContextIsReturned_whenMovingToForLoopAndMatchingLoopIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "log", "${x}")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final UserKeyword keyword = model.getKeywordTable().getKeywords().get(0);

        final KeywordOfUserContext context = new KeywordOfUserContext(keyword, modelUri, newArrayList(model));

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("", "${x} IN [ 1 | 2 | 3 ]", KeywordCallType.FOR),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }
}
