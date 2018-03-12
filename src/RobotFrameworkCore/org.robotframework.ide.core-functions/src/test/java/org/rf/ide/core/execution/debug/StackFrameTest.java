/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrame.FrameCategory;
import org.rf.ide.core.execution.debug.StackFrameVariables.StackVariablesDelta;
import org.rf.ide.core.execution.debug.contexts.KeywordContext;
import org.rf.ide.core.execution.debug.contexts.SuiteContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

public class StackFrameTest {

    @Test
    public void frameNameIsTakenFromConstructor() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final StackFrame frame1 = new StackFrame("frame1", FrameCategory.SUITE, 42, context);
        final StackFrame frame2 = new StackFrame("frame2", FrameCategory.SUITE, 42, context,
                newHashSet(URI.create("file:///res.robot")));
        final StackFrame frame3 = new StackFrame("frame3", FrameCategory.SUITE, 42, context,
                () -> URI.create("file:///res.robot"));

        assertThat(frame1.getName()).isEqualTo("frame1");
        assertThat(frame2.getName()).isEqualTo("frame2");
        assertThat(frame3.getName()).isEqualTo("frame3");
    }

    @Test
    public void frameCategoryIsTakenFromConstructor() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final StackFrame frame1 = new StackFrame("frame", FrameCategory.SUITE, 42, context);
        final StackFrame frame2 = new StackFrame("frame", FrameCategory.TEST, 42, context,
                newHashSet(URI.create("file:///res.robot")));
        final StackFrame frame3 = new StackFrame("frame", FrameCategory.KEYWORD, 42, context,
                () -> URI.create("file:///res.robot"));

        assertThat(frame1.hasCategory(FrameCategory.SUITE)).isTrue();
        assertThat(frame1.hasCategory(FrameCategory.TEST)).isFalse();

        assertThat(frame2.hasCategory(FrameCategory.TEST)).isTrue();
        assertThat(frame2.hasCategory(FrameCategory.FOR)).isFalse();

        assertThat(frame3.hasCategory(FrameCategory.KEYWORD)).isTrue();
        assertThat(frame3.hasCategory(FrameCategory.TEST)).isFalse();
    }

    @Test
    public void frameLevelIsTakenFromConstructor() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final StackFrame frame1 = new StackFrame("frame", FrameCategory.SUITE, 42, context);
        final StackFrame frame2 = new StackFrame("frame", FrameCategory.SUITE, 1729, context,
                newHashSet(URI.create("file:///res.robot")));
        final StackFrame frame3 = new StackFrame("frame", FrameCategory.SUITE, 3, context,
                () -> URI.create("file:///res.robot"));

        assertThat(frame1.getLevel()).isEqualTo(42);
        assertThat(frame2.getLevel()).isEqualTo(1729);
        assertThat(frame3.getLevel()).isEqualTo(3);
    }

    @Test
    public void contextIsTakenFromConstructor() {
        final StackFrameContext context1 = mock(StackFrameContext.class);
        final StackFrameContext context2 = mock(StackFrameContext.class);
        final StackFrameContext context3 = mock(StackFrameContext.class);

        final StackFrame frame1 = new StackFrame("frame", FrameCategory.SUITE, 42, context1);
        final StackFrame frame2 = new StackFrame("frame", FrameCategory.SUITE, 42, context2,
                newHashSet(URI.create("file:///res.robot")));
        final StackFrame frame3 = new StackFrame("frame", FrameCategory.SUITE, 42, context3,
                () -> URI.create("file:///res.robot"));

        assertThat(frame1.getContext()).isSameAs(context1);
        assertThat(frame2.getContext()).isSameAs(context2);
        assertThat(frame3.getContext()).isSameAs(context3);
    }

    @Test
    public void loadedResourcesAreCopiedFromConstructor() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final Set<URI> resources = newHashSet(URI.create("file:///res.robot"));

        final StackFrame frame1 = new StackFrame("frame", FrameCategory.SUITE, 42, context);
        final StackFrame frame2 = new StackFrame("frame", FrameCategory.SUITE, 42, context, resources);
        final StackFrame frame3 = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                () -> URI.create("file:///res.robot"));

        assertThat(frame1.getLoadedResources()).isEmpty();
        assertThat(frame2.getLoadedResources()).containsExactly(URI.create("file:///res.robot"));
        assertThat(frame3.getLoadedResources()).isEmpty();

        // modification of original set does not influence those stored in frame
        resources.add(URI.create("file:///other_res.robot"));
        assertThat(frame2.getLoadedResources()).containsExactly(URI.create("file:///res.robot"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void loadedResourcesAreNotModifiableDirectly() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final Set<URI> resources = newHashSet(URI.create("file:///res.robot"));

        final StackFrame frame = new StackFrame("frame", FrameCategory.SUITE, 42, context, resources);
        frame.getLoadedResources().add(URI.create("file:///other_res.robot"));
    }

    @Test
    public void itIsOnlyPossibleToAddLoadedResourceToSuiteFrame() {
        final StackFrameContext context = mock(StackFrameContext.class);

        for (final FrameCategory category : EnumSet.of(FrameCategory.TEST, FrameCategory.KEYWORD, FrameCategory.FOR,
                FrameCategory.FOR_ITEM)) {

            final StackFrame frame = new StackFrame("frame", category, 42, context);

            assertThatIllegalStateException().isThrownBy(() -> frame.addLoadedResource(URI.create("file:///res.robot")))
                    .withMessage("Cannot store resource in non-suite frame")
                    .withNoCause();
        }

        final StackFrame frame = new StackFrame("frame", FrameCategory.SUITE, 42, context);
        frame.addLoadedResource(URI.create("file:///res.robot"));

        assertThat(frame.getLoadedResources()).containsExactly(URI.create("file:///res.robot"));
    }

    @Test
    public void thereIsNoContextPath_whenContextDoesNotHaveItAndSupplierDoesNotSupplyAnything() {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.getAssociatedPath()).thenReturn(Optional.empty());

        final StackFrame frame1 = new StackFrame("frame", FrameCategory.SUITE, 42, context);
        final StackFrame frame2 = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                newHashSet(URI.create("file:///res.robot")));
        final StackFrame frame3 = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                () -> null);

        assertThat(frame1.getContextPath()).isEmpty();
        assertThat(frame2.getContextPath()).isEmpty();
        assertThat(frame3.getContextPath()).isEmpty();
    }

    @Test
    public void thereIsAContextPath_whenContextHaveIt() {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.getAssociatedPath()).thenReturn(Optional.of(URI.create("file:///suite.robot")));

        final StackFrame frame1 = new StackFrame("frame", FrameCategory.SUITE, 42, context);
        final StackFrame frame2 = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                newHashSet(URI.create("file:///res.robot")));
        final StackFrame frame3 = new StackFrame("frame", FrameCategory.SUITE, 42, context, () -> null);

        assertThat(frame1.getContextPath()).contains(URI.create("file:///suite.robot"));
        assertThat(frame2.getContextPath()).contains(URI.create("file:///suite.robot"));
        assertThat(frame3.getContextPath()).contains(URI.create("file:///suite.robot"));
    }

    @Test
    public void thereIsAContextPath_whenContextDoesNotHaveItButSupplierSupplies() {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.getAssociatedPath()).thenReturn(Optional.empty());

        final StackFrame frame = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                () -> URI.create("file:///suite.robot"));

        assertThat(frame.getContextPath()).contains(URI.create("file:///suite.robot"));
    }

    @Test
    public void currentPathIsAlwaysTakenFromContext() {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.getAssociatedPath()).thenReturn(Optional.of(URI.create("file:///context_suite.robot")));

        final StackFrame frame1 = new StackFrame("frame", FrameCategory.SUITE, 42, context);
        final StackFrame frame2 = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                newHashSet(URI.create("file:///res.robot")));
        final StackFrame frame3 = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                () -> URI.create("file:///suite.robot"));

        assertThat(frame1.getCurrentSourcePath()).contains(URI.create("file:///context_suite.robot"));
        assertThat(frame2.getCurrentSourcePath()).contains(URI.create("file:///context_suite.robot"));
        assertThat(frame3.getCurrentSourcePath()).contains(URI.create("file:///context_suite.robot"));
    }

    @Test
    public void errorMessageIsEmpty_whenContextIsNotErroneous() {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.isErroneous()).thenReturn(false);

        final StackFrame frame = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                () -> URI.create("file:///suite.robot"));

        assertThat(frame.isErroneous()).isFalse();
        assertThat(frame.getErrorMessage()).isEmpty();
    }

    @Test
    public void errorMessageIsTakenFromContext_whenContextIsErroneous() {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.isErroneous()).thenReturn(true);
        when(context.getErrorMessage()).thenReturn(Optional.of("error"));

        final StackFrame frame = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                () -> URI.create("file:///suite.robot"));

        assertThat(frame.isErroneous()).isTrue();
        assertThat(frame.getErrorMessage()).isEqualTo("error");
    }

    @Test
    public void errorMessageIsConcatenatedFromOriginalMessageAndCurrentContextMsg_whenContextIsErroneous() {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.isErroneous()).thenReturn(true);
        when(context.getErrorMessage()).thenReturn(Optional.of("error")).thenReturn(Optional.of(": other error"));

        final StackFrame frame = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                () -> URI.create("file:///suite.robot"));

        assertThat(frame.isErroneous()).isTrue();
        assertThat(frame.getErrorMessage()).isEqualTo("error: other error");
    }

    @Test
    public void newlyConstructedFrameHasNoMarkers() {
        final StackFrameContext context = mock(StackFrameContext.class);

        final StackFrame frame1 = new StackFrame("frame", FrameCategory.SUITE, 42, context);
        final StackFrame frame2 = new StackFrame("frame", FrameCategory.SUITE, 42, context,
                newHashSet(URI.create("file:///res.robot")));
        final StackFrame frame3 = new StackFrame("frame", FrameCategory.SUITE, 42, context, () -> null);

        assertThat(frame1.isMarkedError()).isFalse();
        assertThat(frame1.isMarkedStepping()).isFalse();

        assertThat(frame2.isMarkedError()).isFalse();
        assertThat(frame2.isMarkedStepping()).isFalse();

        assertThat(frame3.isMarkedError()).isFalse();
        assertThat(frame3.isMarkedStepping()).isFalse();
    }

    @Test
    public void markingAndUnmarkingFrames_worksProperly() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final StackFrame frame = new StackFrame("frame", FrameCategory.SUITE, 42, context);

        frame.mark(StackFrameMarker.ERROR);
        assertThat(frame.isMarkedError()).isTrue();
        assertThat(frame.isMarkedStepping()).isFalse();

        frame.mark(StackFrameMarker.ERROR);
        assertThat(frame.isMarkedError()).isTrue();
        assertThat(frame.isMarkedStepping()).isFalse();

        frame.mark(StackFrameMarker.STEPPING);
        assertThat(frame.isMarkedError()).isTrue();
        assertThat(frame.isMarkedStepping()).isTrue();

        frame.unmark(StackFrameMarker.ERROR);
        assertThat(frame.isMarkedError()).isFalse();
        assertThat(frame.isMarkedStepping()).isTrue();

        frame.unmark(StackFrameMarker.STEPPING);
        assertThat(frame.isMarkedError()).isFalse();
        assertThat(frame.isMarkedStepping()).isFalse();

        frame.unmark(StackFrameMarker.STEPPING);
        assertThat(frame.isMarkedError()).isFalse();
        assertThat(frame.isMarkedStepping()).isFalse();
    }

    @Test
    public void frameIsASuiteFrame_whenItHasSuiteCategory() {
        final StackFrameContext context = mock(StackFrameContext.class);

        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, context).isSuiteContext()).isTrue();
        assertThat(new StackFrame("frame", FrameCategory.TEST, 42, context).isSuiteContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, context).isSuiteContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.FOR, 42, context).isSuiteContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.FOR_ITEM, 42, context).isSuiteContext()).isFalse();
    }

    @Test
    public void frameIsASuiteDirectoryOrFileFrame_whenContextIsDirectorySuiteContextOrOtherTranslatedFromIt() {
        final StackFrameContext context = mock(StackFrameContext.class);

        final SuiteContext dirContext = mock(SuiteContext.class);
        when(dirContext.isDirectory()).thenReturn(true);
        final SuiteContext fileContext = mock(SuiteContext.class);
        when(fileContext.isDirectory()).thenReturn(false);

        final StackFrameContext dirTranslatedContext = mock(StackFrameContext.class);
        when(dirTranslatedContext.previousContext()).thenReturn(dirContext);
        final StackFrameContext fileTranslatedContext = mock(StackFrameContext.class);
        when(fileTranslatedContext.previousContext()).thenReturn(fileContext);

        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, context).isSuiteFileContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, context).isSuiteDirectoryContext()).isFalse();

        assertThat(new StackFrame("frame", FrameCategory.TEST, 42, dirContext).isSuiteFileContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.TEST, 42, dirContext).isSuiteDirectoryContext()).isFalse();

        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, dirContext).isSuiteFileContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, dirContext).isSuiteDirectoryContext()).isTrue();

        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, fileContext).isSuiteFileContext()).isTrue();
        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, fileContext).isSuiteDirectoryContext()).isFalse();

        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, dirTranslatedContext).isSuiteFileContext())
                .isFalse();
        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, dirTranslatedContext).isSuiteDirectoryContext())
                .isTrue();

        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, fileTranslatedContext).isSuiteFileContext())
                .isTrue();
        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, fileTranslatedContext).isSuiteDirectoryContext())
                .isFalse();
    }

    @Test
    public void frameIsATestFrame_whenItHasTestCategory() {
        final StackFrameContext context = mock(StackFrameContext.class);

        assertThat(new StackFrame("frame", FrameCategory.SUITE, 42, context).isTestContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.TEST, 42, context).isTestContext()).isTrue();
        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, context).isTestContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.FOR, 42, context).isTestContext()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.FOR_ITEM, 42, context).isTestContext()).isFalse();
    }

    @Test
    public void frameIsALibraryKeywordFrame_whenItLibraryKeywordContext() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final KeywordContext nonLibraryKeywordContext = mock(KeywordContext.class);
        when(nonLibraryKeywordContext.isLibraryKeywordContext()).thenReturn(false);
        final KeywordContext libraryKeywordContext = mock(KeywordContext.class);
        when(libraryKeywordContext.isLibraryKeywordContext()).thenReturn(true);

        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, context).isLibraryKeywordFrame()).isFalse();
        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, nonLibraryKeywordContext).isLibraryKeywordFrame())
                .isFalse();
        assertThat(new StackFrame("frame", FrameCategory.TEST, 42, libraryKeywordContext).isLibraryKeywordFrame())
                .isFalse();
        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, libraryKeywordContext).isLibraryKeywordFrame())
                .isTrue();
    }

    @Test
    public void frameFileRegionIsTakenDirectlyFromContext() {
        final StackFrameContext contextWithoutRegion = mock(StackFrameContext.class);
        when(contextWithoutRegion.getFileRegion()).thenReturn(Optional.empty());

        final StackFrameContext contextWithRegion = mock(StackFrameContext.class);
        when(contextWithRegion.getFileRegion())
                .thenReturn(Optional.of(new FileRegion(new FilePosition(3, 2, 42), new FilePosition(3, 5, 99))));

        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, contextWithoutRegion).getFileRegion()).isEmpty();
        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, contextWithRegion).getFileRegion())
                .contains(new FileRegion(new FilePosition(3, 2, 42), new FilePosition(3, 5, 99)));
    }

    @Test
    public void associatedLineBreakpointIsTakenDirectlyFromContext() {
        final StackFrameContext contextWithoutBp = mock(StackFrameContext.class);
        when(contextWithoutBp.getLineBreakpoint()).thenReturn(Optional.empty());

        final RobotLineBreakpoint bp = mock(RobotLineBreakpoint.class);
        final StackFrameContext contextWithBp = mock(StackFrameContext.class);
        when(contextWithBp.getLineBreakpoint()).thenReturn(Optional.of(bp));

        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, contextWithoutBp).getBreakpoint()).isEmpty();
        assertThat(new StackFrame("frame", FrameCategory.KEYWORD, 42, contextWithBp).getBreakpoint()).contains(bp);
    }

    @Test
    public void contextIsSwitched_whenMovingFrameToDifferentKeyword() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final StackFrameContext nextContext = mock(StackFrameContext.class);
        when(context.moveTo(any(RunningKeyword.class), any(RobotBreakpointSupplier.class))).thenReturn(nextContext);

        final StackFrame frame = new StackFrame("frame", FrameCategory.KEYWORD, 42, context);
        assertThat(frame.getContext()).isSameAs(context);

        frame.moveToKeyword(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL),
                (location, line) -> Optional.empty());

        assertThat(frame.getContext()).isSameAs(nextContext);
    }

    @Test
    public void previousContextIsSwitched_whenMovingFrameOutOfKeyword() {
        final StackFrameContext context = mock(StackFrameContext.class);
        final StackFrameContext previousContext = mock(StackFrameContext.class);
        when(context.previousContext()).thenReturn(previousContext);

        final StackFrame frame = new StackFrame("frame", FrameCategory.KEYWORD, 42, context);
        assertThat(frame.getContext()).isSameAs(context);

        frame.moveOutOfKeyword();

        assertThat(frame.getContext()).isSameAs(previousContext);
    }

    @Test
    public void newlyCreatedFrameHasNoVariablesDelta() {
        final StackFrame frame = new StackFrame("frame", FrameCategory.KEYWORD, 42, mock(StackFrameContext.class));

        assertThat(frame.getLastDelta()).isEmpty();
    }

    @Test
    public void frameWhichHadVariablesUpdatedHasVariablesDelta() {
        final StackVariablesDelta delta = new StackVariablesDelta();

        final Map<Variable, VariableTypedValue> vars = new HashMap<>();
        final StackFrameVariables variables = mock(StackFrameVariables.class);
        when(variables.update(vars)).thenReturn(delta);

        final StackFrame frame = new StackFrame("frame", FrameCategory.KEYWORD, 42, mock(StackFrameContext.class));

        frame.setVariables(variables);
        frame.updateVariables(vars);

        assertThat(frame.getLastDelta()).contains(delta);
    }

    @Test
    public void frameVariablesUpdateTest() {
        final Map<Variable, VariableTypedValue> vars = new HashMap<>();
        vars.put(new Variable("var1", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));
        vars.put(new Variable("var2", VariableScope.TEST_SUITE), new VariableTypedValue("string", "xyz"));

        final StackFrameVariables variables = StackFrameVariables.newNonLocalVariables(vars);

        final Map<Variable, VariableTypedValue> newVars = new HashMap<>();
        newVars.put(new Variable("var1", VariableScope.TEST_SUITE), new VariableTypedValue("int", 4));
        newVars.put(new Variable("var3", VariableScope.TEST_SUITE), new VariableTypedValue("double", 4.2d));

        final StackFrame frame = new StackFrame("frame", FrameCategory.SUITE, 42, mock(StackFrameContext.class));
        frame.setVariables(variables);
        frame.updateVariables(newVars);

        final StackFrameVariables updatedVariables = frame.getVariables();
        assertThat(updatedVariables.getVariables().keySet()).containsOnly("var1", "var3");

        final StackVariablesDelta delta = frame.getLastDelta().get();
        assertThat(delta.isChanged("var1")).isTrue();
        assertThat(delta.isAdded("var3")).isTrue();
        assertThat(delta.isRemoved("var2")).isTrue();
    }

}
