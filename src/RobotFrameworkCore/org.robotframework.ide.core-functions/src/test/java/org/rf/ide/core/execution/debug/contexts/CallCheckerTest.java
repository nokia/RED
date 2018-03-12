/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class CallCheckerTest {

    @Test
    public void positiveMatchingCallsExamples() {
        assertThat(CallChecker.isCallOf("log", new RunningKeyword("lib", "log", null))).isTrue();
        assertThat(CallChecker.isCallOf("log", new RunningKeyword("lib", "Log", null))).isTrue();
        assertThat(CallChecker.isCallOf("lib.Log", new RunningKeyword("lib", "log", null))).isTrue();

        assertThat(CallChecker.isCallOf("keyword with ${x} params",
                new RunningKeyword("lib", "keyword with some params", null))).isTrue();
    }

    @Test
    public void negativeMatchingCallsExamples() {
        assertThat(CallChecker.isCallOf("lag", new RunningKeyword("lib", "log", null))).isFalse();
        assertThat(CallChecker.isCallOf("other.Log", new RunningKeyword("lib", "log", null))).isFalse();

        assertThat(CallChecker.isCallOf("keyword with ${x:\\d+} params",
                new RunningKeyword("lib", "keyword with some params", null))).isFalse();
    }

    @Test
    public void forKeywordNamesGenerationTests_1() {
        final RobotExecutableRow<?> row = new RobotExecutableRow<>();
        row.setAction(RobotToken.create(":FOR"));
        row.addArgument(RobotToken.create("${x}"));
        row.addArgument(RobotToken.create("IN"));
        row.addArgument(RobotToken.create("${XS}"));

        final ForLoopDeclarationRowDescriptor<?> descriptor = new ForLoopDeclarationRowDescriptor<>(row);

        final VariableDeclaration variable = new VariableDeclaration(new TextPosition("${x}", 0, 3),
                new TextPosition("${x}", 0, 3));
        variable.setTypeIdentificator(new TextPosition("${x}", 0, 3));
        variable.setRobotTokenPosition(new FilePosition(5, 0, 60));
        descriptor.addCreatedVariable(variable);

        descriptor.setInAction(new RobotAction(RobotToken.create("IN"), newArrayList()));

        assertThat(CallChecker.createName(descriptor)).isEqualTo("${x} IN [ ${XS} ]");
    }

    @Test
    public void forKeywordNamesGenerationTests_2() {
        final RobotExecutableRow<?> row = new RobotExecutableRow<>();
        row.setAction(RobotToken.create(":FOR"));
        row.addArgument(RobotToken.create("${x}"));
        row.addArgument(RobotToken.create("${y}"));
        row.addArgument(RobotToken.create("IN ZIP"));
        row.addArgument(RobotToken.create("${XS}"));
        row.addArgument(RobotToken.create("${YS}"));

        final ForLoopDeclarationRowDescriptor<?> descriptor = new ForLoopDeclarationRowDescriptor<>(row);

        final VariableDeclaration variable1 = new VariableDeclaration(new TextPosition("${x}", 0, 3), new TextPosition("${x}", 0, 3));
        variable1.setTypeIdentificator(new TextPosition("${x}", 0, 3));
        variable1.setRobotTokenPosition(new FilePosition(5, 0, 60));
        descriptor.addCreatedVariable(variable1);

        final VariableDeclaration variable2 = new VariableDeclaration(new TextPosition("${y}", 0, 3),
                new TextPosition("${y}", 0, 3));
        variable2.setTypeIdentificator(new TextPosition("${x}", 0, 3));
        variable2.setRobotTokenPosition(new FilePosition(5, 0, 67));
        descriptor.addCreatedVariable(variable2);

        descriptor.setInAction(new RobotAction(RobotToken.create("IN ZIP"), newArrayList()));

        assertThat(CallChecker.createName(descriptor)).isEqualTo("${x} | ${y} IN ZIP [ ${XS} | ${YS} ]");
    }

    @Test
    public void positiveMatchingForCallsExample() {
        final RobotExecutableRow<?> row = new RobotExecutableRow<>();
        row.setAction(RobotToken.create(":FOR"));
        row.addArgument(RobotToken.create("${x}"));
        row.addArgument(RobotToken.create("IN"));
        row.addArgument(RobotToken.create("${XS}"));

        final ForLoopDeclarationRowDescriptor<?> descriptor = new ForLoopDeclarationRowDescriptor<>(row);

        final VariableDeclaration variable = new VariableDeclaration(new TextPosition("${x}", 0, 3),
                new TextPosition("${x}", 0, 3));
        variable.setTypeIdentificator(new TextPosition("${x}", 0, 3));
        variable.setRobotTokenPosition(new FilePosition(5, 0, 60));
        descriptor.addCreatedVariable(variable);

        descriptor.setInAction(new RobotAction(RobotToken.create("IN"), newArrayList()));

        assertThat(CallChecker.isSameForLoop(descriptor, new RunningKeyword(null, "${x} IN [ ${XS} ]", null))).isTrue();
    }

    @Test
    public void negativeMatchingForCallsExample() {
        final RobotExecutableRow<?> row = new RobotExecutableRow<>();
        row.setAction(RobotToken.create(":FOR"));
        row.addArgument(RobotToken.create("${x}"));
        row.addArgument(RobotToken.create("IN"));
        row.addArgument(RobotToken.create("${XS}"));

        final ForLoopDeclarationRowDescriptor<?> descriptor = new ForLoopDeclarationRowDescriptor<>(row);

        final VariableDeclaration variable = new VariableDeclaration(new TextPosition("${x}", 0, 3),
                new TextPosition("${x}", 0, 3));
        variable.setTypeIdentificator(new TextPosition("${x}", 0, 3));
        variable.setRobotTokenPosition(new FilePosition(5, 0, 60));
        descriptor.addCreatedVariable(variable);

        descriptor.setInAction(new RobotAction(RobotToken.create("IN"), newArrayList()));

        assertThat(CallChecker.isSameForLoop(descriptor, new RunningKeyword(null, "${y} IN [ ${YS} ]", null)))
                .isFalse();
    }
}
