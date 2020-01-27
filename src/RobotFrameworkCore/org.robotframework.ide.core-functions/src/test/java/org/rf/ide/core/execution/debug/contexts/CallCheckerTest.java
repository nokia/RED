/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

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
        final RobotExecutableRow<Object> row = new RobotExecutableRow<>();
        row.setParent(new TestCase(RobotToken.create("test")));
        row.setAction(RobotToken.create(":FOR", RobotTokenType.FOR_TOKEN));
        row.addArgument(RobotToken.create("${x}"));
        row.addArgument(RobotToken.create("IN", RobotTokenType.IN_TOKEN));
        row.addArgument(RobotToken.create("${XS}"));

        final IExecutableRowDescriptor<?> desc = row.buildLineDescription();

        assertThat(CallChecker.createName((ForLoopDeclarationRowDescriptor<?>) desc)).isEqualTo("${x} IN [ ${XS} ]");
    }

    @Test
    public void forKeywordNamesGenerationTests_2() {
        final RobotExecutableRow<Object> row = new RobotExecutableRow<>();
        row.setParent(new TestCase(RobotToken.create("test")));
        row.setAction(RobotToken.create(":FOR", RobotTokenType.FOR_TOKEN));
        row.addArgument(RobotToken.create("${x}"));
        row.addArgument(RobotToken.create("${y}"));
        row.addArgument(RobotToken.create("IN ZIP", RobotTokenType.IN_TOKEN));
        row.addArgument(RobotToken.create("${XS}"));
        row.addArgument(RobotToken.create("${YS}"));

        final IExecutableRowDescriptor<?> desc = row.buildLineDescription();

        assertThat(CallChecker.createName((ForLoopDeclarationRowDescriptor<?>) desc))
                .isEqualTo("${x} | ${y} IN ZIP [ ${XS} | ${YS} ]");
    }

    @Test
    public void positiveMatchingForCallsExample() {
        final RobotExecutableRow<Object> row = new RobotExecutableRow<>();
        row.setParent(new TestCase(RobotToken.create("test")));
        row.setAction(RobotToken.create(":FOR", RobotTokenType.FOR_TOKEN));
        row.addArgument(RobotToken.create("${x}"));
        row.addArgument(RobotToken.create("IN", RobotTokenType.IN_TOKEN));
        row.addArgument(RobotToken.create("${XS}"));

        final IExecutableRowDescriptor<?> desc = row.buildLineDescription();

        assertThat(CallChecker.isSameForLoop((ForLoopDeclarationRowDescriptor<?>) desc,
                new RunningKeyword(null, "${x} IN [ ${XS} ]", null))).isTrue();
    }

    @Test
    public void negativeMatchingForCallsExample() {
        final RobotExecutableRow<Object> row = new RobotExecutableRow<>();
        row.setParent(new TestCase(RobotToken.create("test")));
        row.setAction(RobotToken.create(":FOR", RobotTokenType.FOR_TOKEN));
        row.addArgument(RobotToken.create("${x}"));
        row.addArgument(RobotToken.create("IN", RobotTokenType.IN_TOKEN));
        row.addArgument(RobotToken.create("${XS}"));

        final IExecutableRowDescriptor<?> desc = row.buildLineDescription();

        assertThat(CallChecker.isSameForLoop((ForLoopDeclarationRowDescriptor<?>) desc,
                new RunningKeyword(null, "${y} IN [ ${YS} ]", null))).isFalse();
    }
}
