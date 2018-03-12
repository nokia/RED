/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrame.FrameCategory;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

public class StacktraceTest {

    @Test
    public void pushAndPopTests() {
        final Stacktrace stack = new Stacktrace();
        assertThat(stack.isEmpty()).isTrue();
        assertThat(stack.size()).isEqualTo(0);
        assertThat(stack).isEmpty();

        stack.push(frame());
        assertThat(stack.isEmpty()).isFalse();
        assertThat(stack).hasSize(1);
        assertThat(stack.size()).isEqualTo(1);
        stack.push(frame());
        assertThat(stack.isEmpty()).isFalse();
        assertThat(stack).hasSize(2);
        assertThat(stack.size()).isEqualTo(2);

        stack.pop();
        assertThat(stack.isEmpty()).isFalse();
        assertThat(stack).hasSize(1);
        assertThat(stack.size()).isEqualTo(1);

        stack.push(frame());
        assertThat(stack.isEmpty()).isFalse();
        assertThat(stack).hasSize(2);
        assertThat(stack.size()).isEqualTo(2);
        stack.push(frame());
        assertThat(stack.isEmpty()).isFalse();
        assertThat(stack).hasSize(3);
        assertThat(stack.size()).isEqualTo(3);
    }

    @Test
    public void allFramesAreRemoved_whenStackIsDestroyed() {
        final Stacktrace stack = new Stacktrace();

        stack.push(frame());
        stack.push(frame());
        stack.push(frame());

        assertThat(stack).hasSize(3);

        stack.destroy();
        assertThat(stack).isEmpty();
    }

    @Test
    public void iteratorIsReturned_withAllTheCurrentFramesFromTopToBottom() {
        final Stacktrace stack = new Stacktrace();

        final StackFrame f1 = frame();
        final StackFrame f2 = frame();
        final StackFrame f3 = frame();
        
        stack.push(f1);
        stack.push(f2);
        stack.push(f3);
        
        final Iterator<StackFrame> iterator = stack.iterator();
        final List<StackFrame> iteratorContent = newArrayList(iterator);
        
        assertThat(iteratorContent).containsExactly(f3, f2, f1);
    }

    @Test
    public void streamIsReturned_withAllTheCurrentFramesFromTopToBottom() {
        final Stacktrace stack = new Stacktrace();

        final StackFrame f1 = frame();
        final StackFrame f2 = frame();
        final StackFrame f3 = frame();

        stack.push(f1);
        stack.push(f2);
        stack.push(f3);

        final Stream<StackFrame> stream = stack.stream();
        final List<StackFrame> streamContent = stream.collect(toList());

        assertThat(streamContent).containsExactly(f3, f2, f1);
    }

    @Test
    public void thereIsNoCurrentFrame_whenStackIsEmpty() {
        final Stacktrace stack = new Stacktrace();

        assertThat(stack.peekCurrentFrame()).isEmpty();
    }

    @Test
    public void topFrameIsReturnedAsCurrent_whenStackContainsFrames() {
        final Stacktrace stack = new Stacktrace();

        final StackFrame f = frame();

        stack.push(frame());
        stack.push(frame());
        stack.push(f);

        assertThat(stack.peekCurrentFrame()).contains(f);

    }

    @Test
    public void noFrameIsReturned_whenThereIsNothingWhichSatisfiesGivenPredicate() {
        final Stacktrace stack = new Stacktrace();

        stack.push(frame());
        stack.push(frame());
        stack.push(frame());

        final Optional<StackFrame> frame = stack.getFirstFrameSatisfying(f -> false);
        assertThat(frame).isEmpty();
    }

    @Test
    public void theFrameIsReturned_whenItSatisfiesGivenPredicate() {
        final Stacktrace stack = new Stacktrace();

        final StackFrame f2 = frame("f2");

        stack.push(frame("f1"));
        stack.push(f2);
        stack.push(frame("f3"));

        final Optional<StackFrame> frame = stack.getFirstFrameSatisfying(f -> f.getName().equals("f2"));
        assertThat(frame).contains(f2);
    }

    @Test
    public void theFirstFrameIsReturned_whenCoupleOfThemSatisfiesGivenPredicate() {
        final Stacktrace stack = new Stacktrace();

        final StackFrame f = frame("f");

        stack.push(frame("f"));
        stack.push(frame("f1"));
        stack.push(f);
        stack.push(frame("f2"));

        final Optional<StackFrame> frame = stack.getFirstFrameSatisfying(fr -> fr.getName().equals("f"));
        assertThat(frame).contains(f);
    }

    @Test
    public void forGivenFrame_theFrameAboveIsProperlyReturnedAsParent() {
        final Stacktrace stack = new Stacktrace();

        final StackFrame f1 = frame();
        final StackFrame f2 = frame();
        final StackFrame f3 = frame();

        assertThat(stack.findParentFrame(f1)).isNull();
        assertThat(stack.findParentFrame(f2)).isNull();
        assertThat(stack.findParentFrame(f3)).isNull();

        stack.push(f1);
        stack.push(f2);
        stack.push(f3);

        assertThat(stack.findParentFrame(f1)).isNull();
        assertThat(stack.findParentFrame(f2)).isSameAs(f1);
        assertThat(stack.findParentFrame(f3)).isSameAs(f2);
    }

    @Test
    public void currentPathIsTakenFromTopFrame_nothingIsReturnedIfContextHaveNoPathAssociated() {
        final Stacktrace stack = new Stacktrace();

        final StackFrameContext context1 = mock(StackFrameContext.class);
        final StackFrameContext context2 = mock(StackFrameContext.class);

        when(context1.getAssociatedPath()).thenReturn(Optional.of(URI.create("file:///suite1.robot")));
        when(context2.getAssociatedPath()).thenReturn(Optional.empty());

        stack.push(frame(context1));
        stack.push(frame(context2));

        assertThat(stack.getCurrentPath()).isEmpty();
    }

    @Test
    public void currentPathIsTakenFromTopFrame_associatedPathIsReturned() {
        final Stacktrace stack = new Stacktrace();

        final StackFrameContext context1 = mock(StackFrameContext.class);
        final StackFrameContext context2 = mock(StackFrameContext.class);

        when(context1.getAssociatedPath()).thenReturn(Optional.of(URI.create("file:///suite1.robot")));
        when(context2.getAssociatedPath()).thenReturn(Optional.of(URI.create("file:///suite2.robot")));

        stack.push(frame(context1));
        stack.push(frame(context2));

        assertThat(stack.getCurrentPath()).contains(URI.create("file:///suite2.robot"));
    }

    @Test
    public void contextPathIsTakenFromTopFrame_nothingIsReturnedIfFrameHaveNoContextPath() {
        final Stacktrace stack = new Stacktrace();

        stack.push(frame(URI.create("file:///suite1.robot")));
        stack.push(frame());

        assertThat(stack.getContextPath()).isEmpty();
    }

    @Test
    public void contextPathIsTakenFromTopFrame_frameContextPathIsReturned() {
        final Stacktrace stack = new Stacktrace();

        stack.push(frame(URI.create("file:///suite1.robot")));
        stack.push(frame(URI.create("file:///suite1.robot")));

        assertThat(stack.getContextPath()).contains(URI.create("file:///suite1.robot"));
    }

    @Test
    public void checkingCategoryOfTopFramesTests() {
        final Stacktrace stack = new Stacktrace();

        stack.push(frame(FrameCategory.TEST));
        stack.push(frame(FrameCategory.KEYWORD));

        assertThat(stack.hasCategoryOnTop(FrameCategory.SUITE)).isFalse();
        assertThat(stack.hasCategoryOnTop(FrameCategory.TEST)).isFalse();
        assertThat(stack.hasCategoryOnTop(FrameCategory.KEYWORD)).isTrue();
        assertThat(stack.hasCategoryOnTop(FrameCategory.FOR)).isFalse();
        assertThat(stack.hasCategoryOnTop(FrameCategory.FOR_ITEM)).isFalse();
    }

    @Test
    public void stringRepresentationTest() {
        final Stacktrace stack = new Stacktrace();

        final StackFrame f1 = frame("f1");
        final StackFrame f2 = frame("f2");
        stack.push(f1);
        stack.push(f2);
        
        assertThat(stack.toString()).isEqualTo(newArrayList(f2, f1).toString());
    }

    @Test
    public void updatingVariablesOnStackTest_whenFramesHaveNoVariablesYet1() {
        final Map<Variable, VariableTypedValue> globalVars = new HashMap<>();
        globalVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        
        final Map<Variable, VariableTypedValue> suiteVars = new HashMap<>();
        suiteVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        suiteVars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));

        final Map<Variable, VariableTypedValue> testVars = new HashMap<>();
        testVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        testVars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));
        testVars.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 2));

        final Map<Variable, VariableTypedValue> kwVars = new HashMap<>();
        kwVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        kwVars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));
        kwVars.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 2));
        kwVars.put(new Variable("${local}", VariableScope.LOCAL), new VariableTypedValue("int", 3));

        final List<Map<Variable, VariableTypedValue>> variables = newArrayList(kwVars, testVars, suiteVars, globalVars);
        
        final StackFrame suiteFrame = frame(FrameCategory.SUITE, 0);
        final StackFrame testFrame = frame(FrameCategory.TEST, 1);
        final StackFrame kwFrame = frame(FrameCategory.KEYWORD, 2);
        final StackFrame forFrame = frame(FrameCategory.FOR, 2);
        final StackFrame forItemFrame = frame(FrameCategory.FOR_ITEM, 2);

        final Stacktrace stacktrace = new Stacktrace();
        stacktrace.push(suiteFrame);
        stacktrace.push(testFrame);
        stacktrace.push(kwFrame);
        stacktrace.push(forFrame);
        stacktrace.push(forItemFrame);
        
        assertThat(stacktrace.getFirstFrameSatisfying(frame -> frame.getVariables() != null)).isEmpty();

        stacktrace.updateVariables(variables);

        assertThat(suiteFrame.getVariables()).isNotNull();
        assertThat(suiteFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}");
        assertThat(suiteFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(suiteFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));

        assertThat(testFrame.getVariables()).isNotNull();
        assertThat(testFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}");
        assertThat(testFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(testFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));
        assertThat(testFrame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 2));

        assertThat(kwFrame.getVariables()).isNotNull();
        assertThat(kwFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}",
                "${local}");
        assertThat(kwFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(kwFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));
        assertThat(kwFrame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 2));
        assertThat(kwFrame.getVariables().getVariables().get("${local}"))
                .isEqualTo(new StackFrameVariable(VariableScope.LOCAL, false, "${local}", "int", 3));

        assertThat(forFrame.getVariables()).isNotNull();
        assertThat(forFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}",
                "${local}");
        assertThat(forFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(forFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));
        assertThat(forFrame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 2));
        assertThat(forFrame.getVariables().getVariables().get("${local}"))
                .isEqualTo(new StackFrameVariable(VariableScope.LOCAL, false, "${local}", "int", 3));

        assertThat(forItemFrame.getVariables()).isNotNull();
        assertThat(forItemFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}",
                "${local}");
        assertThat(forItemFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(forItemFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));
        assertThat(forItemFrame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 2));
        assertThat(forItemFrame.getVariables().getVariables().get("${local}"))
                .isEqualTo(new StackFrameVariable(VariableScope.LOCAL, false, "${local}", "int", 3));
    }

    @Test
    public void updatingVariablesOnStackTest_whenFramesHaveNoVariablesYet2() {
        final Map<Variable, VariableTypedValue> globalVars = new HashMap<>();
        globalVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));

        final Map<Variable, VariableTypedValue> suiteVars = new HashMap<>();
        suiteVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        suiteVars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));

        final Map<Variable, VariableTypedValue> testVars = new HashMap<>();
        testVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        testVars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));
        testVars.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 2));

        final Map<Variable, VariableTypedValue> kw1Vars = new HashMap<>();
        kw1Vars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        kw1Vars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));
        kw1Vars.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 2));
        kw1Vars.put(new Variable("${local}", VariableScope.LOCAL), new VariableTypedValue("int", 3));

        final Map<Variable, VariableTypedValue> kw2Vars = new HashMap<>();
        kw2Vars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        kw2Vars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));
        kw2Vars.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 2));
        kw2Vars.put(new Variable("${other_local}", VariableScope.LOCAL), new VariableTypedValue("int", 4));

        final List<Map<Variable, VariableTypedValue>> variables = newArrayList(kw2Vars, kw1Vars, testVars, suiteVars,
                globalVars);

        final StackFrame suiteFrame = frame(FrameCategory.SUITE, 0);
        final StackFrame testFrame = frame(FrameCategory.TEST, 1);
        final StackFrame kw1Frame = frame(FrameCategory.KEYWORD, 2);
        final StackFrame kw2Frame = frame(FrameCategory.KEYWORD, 3);

        final Stacktrace stacktrace = new Stacktrace();
        stacktrace.push(suiteFrame);
        stacktrace.push(testFrame);
        stacktrace.push(kw1Frame);
        stacktrace.push(kw2Frame);

        assertThat(stacktrace.getFirstFrameSatisfying(frame -> frame.getVariables() != null)).isEmpty();

        stacktrace.updateVariables(variables);

        assertThat(suiteFrame.getVariables()).isNotNull();
        assertThat(suiteFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}");
        assertThat(suiteFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(suiteFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));

        assertThat(testFrame.getVariables()).isNotNull();
        assertThat(testFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}");
        assertThat(testFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(testFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));
        assertThat(testFrame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 2));

        assertThat(kw1Frame.getVariables()).isNotNull();
        assertThat(kw1Frame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}",
                "${local}");
        assertThat(kw1Frame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(kw1Frame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));
        assertThat(kw1Frame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 2));
        assertThat(kw1Frame.getVariables().getVariables().get("${local}"))
                .isEqualTo(new StackFrameVariable(VariableScope.LOCAL, false, "${local}", "int", 3));

        assertThat(kw2Frame.getVariables()).isNotNull();
        assertThat(kw2Frame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}",
                "${other_local}");
        assertThat(kw2Frame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 0));
        assertThat(kw2Frame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 1));
        assertThat(kw2Frame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 2));
        assertThat(kw2Frame.getVariables().getVariables().get("${other_local}"))
                .isEqualTo(new StackFrameVariable(VariableScope.LOCAL, false, "${other_local}", "int", 4));
    }

    @Test
    public void updatingVariablesOnStackTest_whenFrameAlreadyHaveVariables() {
        final Map<Variable, VariableTypedValue> globalVars = new HashMap<>();
        globalVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 10));

        final Map<Variable, VariableTypedValue> suiteVars = new HashMap<>();
        suiteVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 10));
        suiteVars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 9));

        final Map<Variable, VariableTypedValue> testVars = new HashMap<>();
        testVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 10));
        testVars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 9));
        testVars.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 8));

        final Map<Variable, VariableTypedValue> kwVars = new HashMap<>();
        kwVars.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 10));
        kwVars.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 9));
        kwVars.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 8));
        kwVars.put(new Variable("${local}", VariableScope.LOCAL), new VariableTypedValue("int", 7));

        final List<Map<Variable, VariableTypedValue>> variables = newArrayList(kwVars, testVars, suiteVars,
                globalVars);

        final StackFrame suiteFrame = frame(FrameCategory.SUITE, 0);
        final StackFrame testFrame = frame(FrameCategory.TEST, 1);
        final StackFrame kwFrame = frame(FrameCategory.KEYWORD, 2);

        final Map<Variable, VariableTypedValue> originalSuiteVariables = new HashMap<>();
        originalSuiteVariables.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        originalSuiteVariables.put(new Variable("${suite}", VariableScope.TEST_SUITE),
                new VariableTypedValue("int", 1));
        suiteFrame.setVariables(StackFrameVariables.newNonLocalVariables(originalSuiteVariables));

        final Map<Variable, VariableTypedValue> originalTestVariables = new HashMap<>();
        originalTestVariables.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        originalTestVariables.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));
        originalTestVariables.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 2));
        testFrame.setVariables(StackFrameVariables.newNonLocalVariables(originalTestVariables));

        final Map<Variable, VariableTypedValue> originalKwVariables = new HashMap<>();
        originalKwVariables.put(new Variable("${global}", VariableScope.GLOBAL), new VariableTypedValue("int", 0));
        originalKwVariables.put(new Variable("${suite}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1));
        originalKwVariables.put(new Variable("${test}", VariableScope.TEST_CASE), new VariableTypedValue("int", 2));
        originalKwVariables.put(new Variable("${local}", VariableScope.LOCAL), new VariableTypedValue("int", 3));
        kwFrame.setVariables(StackFrameVariables.newNonLocalVariables(originalKwVariables));

        final Stacktrace stacktrace = new Stacktrace();
        stacktrace.push(suiteFrame);
        stacktrace.push(testFrame);
        stacktrace.push(kwFrame);

        stacktrace.updateVariables(variables);

        assertThat(suiteFrame.getVariables()).isNotNull();
        assertThat(suiteFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}");
        assertThat(suiteFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 10));
        assertThat(suiteFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 9));

        assertThat(testFrame.getVariables()).isNotNull();
        assertThat(testFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}");
        assertThat(testFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 10));
        assertThat(testFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 9));
        assertThat(testFrame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 8));

        assertThat(kwFrame.getVariables()).isNotNull();
        assertThat(kwFrame.getVariables().getVariables()).containsOnlyKeys("${global}", "${suite}", "${test}",
                "${local}");
        assertThat(kwFrame.getVariables().getVariables().get("${global}"))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "${global}", "int", 10));
        assertThat(kwFrame.getVariables().getVariables().get("${suite}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_SUITE, false, "${suite}", "int", 9));
        assertThat(kwFrame.getVariables().getVariables().get("${test}"))
                .isEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, false, "${test}", "int", 8));
        assertThat(kwFrame.getVariables().getVariables().get("${local}"))
                .isEqualTo(new StackFrameVariable(VariableScope.LOCAL, false, "${local}", "int", 7));
    }

    private static StackFrame frame() {
        return frame("frame");
    }

    private static StackFrame frame(final String name) {
        return new StackFrame(name, FrameCategory.KEYWORD, 0, mock(StackFrameContext.class));
    }

    private static StackFrame frame(final StackFrameContext context) {
        return new StackFrame("frame", FrameCategory.KEYWORD, 0, context);
    }

    private static StackFrame frame(final FrameCategory category) {
        return new StackFrame("frame", category, 0, mock(StackFrameContext.class));
    }

    private static StackFrame frame(final FrameCategory category, final int level) {
        return new StackFrame("frame", category, level, mock(StackFrameContext.class));
    }

    private static StackFrame frame(final URI contextPath) {
        return new StackFrame("frame", FrameCategory.KEYWORD, 0, mock(StackFrameContext.class), () -> contextPath);
    }
}
