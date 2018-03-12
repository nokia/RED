/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ExecutableWithDescriptorTest {

    @Test
    public void executableIsLoop_whenForLoopWasProvided() {
        final ExecutableWithDescriptor exec = new ExecutableWithDescriptor(new RobotExecutableRow<>(),
                mock(IExecutableRowDescriptor.class), null);
        final ExecutableWithDescriptor forLoopExec1 = new ExecutableWithDescriptor(null, null);
        final ExecutableWithDescriptor forLoopExec2 = new ExecutableWithDescriptor(new ForLoop(null, newArrayList()),
                null);

        assertThat(exec.isLoopExecutable()).isFalse();
        assertThat(exec.getLoopExecutable()).isNull();

        assertThat(forLoopExec1.isLoopExecutable()).isFalse();
        assertThat(forLoopExec1.getLoopExecutable()).isNull();

        assertThat(forLoopExec2.isLoopExecutable()).isTrue();
        assertThat(forLoopExec2.getLoopExecutable()).isNotNull();
    }

    @Test
    public void executableIsTakenFromForLoop() {
        final RobotExecutableRow<?> executable = new RobotExecutableRow<>();
        final IExecutableRowDescriptor<?> descriptor = mock(IExecutableRowDescriptor.class);
        final ExecutableWithDescriptor forLoopExec = new ExecutableWithDescriptor(
                new ForLoop(new ExecutableWithDescriptor(executable, descriptor, null), newArrayList()), null);

        assertThat(forLoopExec.getExecutable()).isSameAs(executable);
    }

    @Test
    public void executableIsTakenDirectlyForNonLoop() {
        final RobotExecutableRow<?> executable = new RobotExecutableRow<>();
        final ExecutableWithDescriptor exec = new ExecutableWithDescriptor(executable,
                mock(IExecutableRowDescriptor.class), null);

        assertThat(exec.getExecutable()).isSameAs(executable);
    }

    @Test
    public void descriptorIsTakenFromForLoop() {
        final RobotExecutableRow<?> executable = new RobotExecutableRow<>();
        final IExecutableRowDescriptor<?> descriptor = mock(ForLoopDeclarationRowDescriptor.class);
        final ExecutableWithDescriptor forLoopExec = new ExecutableWithDescriptor(
                new ForLoop(new ExecutableWithDescriptor(executable, descriptor, null), newArrayList()), null);

        assertThat(forLoopExec.getDescriptor()).isSameAs(descriptor);
    }

    @Test
    public void descriptorIsTakenDirectlyForNonLoop() {
        final RobotExecutableRow<?> executable = new RobotExecutableRow<>();
        final IExecutableRowDescriptor<?> descriptor = mock(ForLoopDeclarationRowDescriptor.class);
        final ExecutableWithDescriptor exec = new ExecutableWithDescriptor(executable, descriptor, null);

        assertThat(exec.getDescriptor()).isSameAs(descriptor);
    }

    @Test
    public void checkingLastExecutableForTestCase() {
        final RobotExecutableRow<TestCase> firstExecutable = new RobotExecutableRow<>();
        final RobotExecutableRow<TestCase> secondExecutable = new RobotExecutableRow<>();
        final RobotExecutableRow<TestCase> thirdExecutable = new RobotExecutableRow<>();

        final TestCase testCase = new TestCase(RobotToken.create("test"));
        testCase.addElement(firstExecutable);
        testCase.addElement(secondExecutable);
        testCase.addElement(thirdExecutable);

        final ExecutableWithDescriptor firstExecWithDescriptor = new ExecutableWithDescriptor(firstExecutable,
                mock(IExecutableRowDescriptor.class), null);
        final ExecutableWithDescriptor secondExecWithDescriptor = new ExecutableWithDescriptor(secondExecutable,
                mock(IExecutableRowDescriptor.class), null);
        final ExecutableWithDescriptor thirdExecWithDescriptor = new ExecutableWithDescriptor(thirdExecutable,
                mock(IExecutableRowDescriptor.class), null);

        assertThat(firstExecWithDescriptor.isLastExecutable()).isFalse();
        assertThat(secondExecWithDescriptor.isLastExecutable()).isFalse();
        assertThat(thirdExecWithDescriptor.isLastExecutable()).isTrue();
    }

    @Test
    public void checkingLastExecutableForUserKeyword() {
        final RobotExecutableRow<UserKeyword> firstExecutable = new RobotExecutableRow<>();
        final RobotExecutableRow<UserKeyword> secondExecutable = new RobotExecutableRow<>();
        final RobotExecutableRow<UserKeyword> thirdExecutable = new RobotExecutableRow<>();

        final UserKeyword keyword = new UserKeyword(RobotToken.create("keyword"));
        keyword.addElement(firstExecutable);
        keyword.addElement(secondExecutable);
        keyword.addElement(thirdExecutable);

        final ExecutableWithDescriptor firstExecWithDescriptor = new ExecutableWithDescriptor(firstExecutable,
                mock(IExecutableRowDescriptor.class), null);
        final ExecutableWithDescriptor secondExecWithDescriptor = new ExecutableWithDescriptor(secondExecutable,
                mock(IExecutableRowDescriptor.class), null);
        final ExecutableWithDescriptor thirdExecWithDescriptor = new ExecutableWithDescriptor(thirdExecutable,
                mock(IExecutableRowDescriptor.class), null);

        assertThat(firstExecWithDescriptor.isLastExecutable()).isFalse();
        assertThat(secondExecWithDescriptor.isLastExecutable()).isFalse();
        assertThat(thirdExecWithDescriptor.isLastExecutable()).isTrue();
    }

    @Test
    public void keywordCallNameIsTakenFromTemplateIfProvided() {
        final RobotExecutableRow<UserKeyword> executable = new RobotExecutableRow<>();
        final ExecutableWithDescriptor execDescriptor = new ExecutableWithDescriptor(executable,
                mock(IExecutableRowDescriptor.class), "template");

        assertThat(execDescriptor.getCalledKeywordName()).isEqualTo("template");
    }

    @Test
    public void keywordCallNameIsTakenFromExecutableIfNoTemplateIsProvided_1() {
        final RobotToken token = RobotToken.create("keyword");

        final IExecutableRowDescriptor descriptor = mock(IExecutableRowDescriptor.class);
        when(descriptor.getAction()).thenReturn(new RobotAction(token, newArrayList()));
        final RobotExecutableRow<UserKeyword> executable = new RobotExecutableRow<>();

        final ExecutableWithDescriptor execDescriptor = new ExecutableWithDescriptor(executable, descriptor, null);

        assertThat(execDescriptor.getCalledKeywordName()).isEqualTo("keyword");
    }

    @Test
    public void keywordCallNameIsTakenFromExecutableIfNoTemplateIsProvided_2() {
        final RobotToken token = RobotToken.create("keyword_in_loop");
        final RobotExecutableRow<UserKeyword> executable = new RobotExecutableRow<>();

        final ForLoopContinueRowDescriptor<?> descriptor = new ForLoopContinueRowDescriptor<>(executable);
        descriptor.setKeywordAction(new RobotAction(token, newArrayList()));

        final ExecutableWithDescriptor execDescriptor = new ExecutableWithDescriptor(executable, descriptor, null);

        assertThat(execDescriptor.getCalledKeywordName()).isEqualTo("keyword_in_loop");
    }

    @Test
    public void lineOfKeywordCallIsTakenFromExecutable_1() {
        final RobotToken token = RobotToken.create("keyword");
        token.setFilePosition(new FilePosition(42, 0, 100));

        final IExecutableRowDescriptor descriptor = mock(IExecutableRowDescriptor.class);
        when(descriptor.getAction()).thenReturn(new RobotAction(token, newArrayList()));
        final RobotExecutableRow<UserKeyword> executable = new RobotExecutableRow<>();

        final ExecutableWithDescriptor execDescriptor = new ExecutableWithDescriptor(executable, descriptor, null);

        assertThat(execDescriptor.getLine()).isEqualTo(42);
    }

    @Test
    public void lineOfKeywordCallIsTakenFromExecutable_2() {
        final RobotToken token = RobotToken.create("keyword_in_loop");
        token.setFilePosition(new FilePosition(57, 0, 100));
        final RobotExecutableRow<UserKeyword> executable = new RobotExecutableRow<>();

        final ForLoopContinueRowDescriptor<?> descriptor = new ForLoopContinueRowDescriptor<>(executable);
        descriptor.setKeywordAction(new RobotAction(token, newArrayList()));

        final ExecutableWithDescriptor execDescriptor = new ExecutableWithDescriptor(executable, descriptor, null);

        assertThat(execDescriptor.getLine()).isEqualTo(57);
    }

    @Test
    public void variablesFromForLoopAreProperlyReturned() {
        final RobotExecutableRow executable = new RobotExecutableRow<>();

        final ForLoopDeclarationRowDescriptor<?> desc = new ForLoopDeclarationRowDescriptor<>(executable);
        final VariableDeclaration variable1 = new VariableDeclaration(new TextPosition("${x}", 0, 3),
                new TextPosition("${x}", 0, 3));
        variable1.setTypeIdentificator(new TextPosition("${x}", 0, 3));
        variable1.setRobotTokenPosition(new FilePosition(5, 0, 60));
        
        final VariableDeclaration variable2 = new VariableDeclaration(new TextPosition("${y}", 0, 3),
                new TextPosition("${y}", 0, 3));
        variable2.setTypeIdentificator(new TextPosition("${y}", 0, 3));
        variable2.setRobotTokenPosition(new FilePosition(5, 8, 68));

        desc.addCreatedVariable(variable1);
        desc.addCreatedVariable(variable2);

        final ExecutableWithDescriptor loopHeader = new ExecutableWithDescriptor(executable, desc, null);
        final ExecutableWithDescriptor descriptor = new ExecutableWithDescriptor(
                new ForLoop(loopHeader, newArrayList()), null);

        final List<RobotToken> variables = descriptor.getForVariables();
        assertThat(variables).hasSize(2);
        assertThat(variables.get(0).getText()).isEqualTo("${x}");
        assertThat(variables.get(1).getText()).isEqualTo("${y}");
    }

    @Test
    public void variablesFromForHaveProperRegionReturned() {
        final RobotExecutableRow executable = new RobotExecutableRow<>();

        final ForLoopDeclarationRowDescriptor<?> desc = new ForLoopDeclarationRowDescriptor<>(executable);
        final VariableDeclaration variable1 = new VariableDeclaration(new TextPosition("${x}", 0, 3),
                new TextPosition("${x}", 0, 3));
        variable1.setTypeIdentificator(new TextPosition("${x}", 0, 3));
        variable1.setRobotTokenPosition(new FilePosition(5, 0, 60));

        final VariableDeclaration variable2 = new VariableDeclaration(new TextPosition("${y}", 0, 3),
                new TextPosition("${y}", 0, 3));
        variable2.setTypeIdentificator(new TextPosition("${y}", 0, 3));
        variable2.setRobotTokenPosition(new FilePosition(5, 8, 68));

        desc.addCreatedVariable(variable1);
        desc.addCreatedVariable(variable2);

        final ExecutableWithDescriptor loopHeader = new ExecutableWithDescriptor(executable, desc, null);
        final ExecutableWithDescriptor descriptor = new ExecutableWithDescriptor(
                new ForLoop(loopHeader, newArrayList()), null);

        final FileRegion region = descriptor.getForVariablesRegion();
        assertThat(region.getStart().getOffset()).isEqualTo(60);
        assertThat(region.getEnd().getOffset()).isEqualTo(72);
    }
}
