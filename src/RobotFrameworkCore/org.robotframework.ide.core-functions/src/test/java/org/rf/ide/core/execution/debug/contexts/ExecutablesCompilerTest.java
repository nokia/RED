/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class ExecutablesCompilerTest {

    @Test
    public void executablesCompilationTest() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("keyword", "${x}", "${y}")
                        .executable("${z}", "call", "1", "2")
                .build();

        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();

        final List<ExecutableWithDescriptor> descriptors = ExecutablesCompiler.compileExecutables(rows, null);

        assertThat(descriptors).hasSize(3);
        assertThat(descriptors.get(0).getCalledKeywordName()).isEqualTo("log");
        assertThat(descriptors.get(0).isLoopExecutable()).isFalse();
        assertThat(descriptors.get(0).isLastExecutable()).isFalse();

        assertThat(descriptors.get(1).getCalledKeywordName()).isEqualTo("keyword");
        assertThat(descriptors.get(1).isLoopExecutable()).isFalse();
        assertThat(descriptors.get(1).isLastExecutable()).isFalse();

        assertThat(descriptors.get(2).getCalledKeywordName()).isEqualTo("call");
        assertThat(descriptors.get(2).isLoopExecutable()).isFalse();
        assertThat(descriptors.get(2).isLastExecutable()).isTrue();
    }

    @Test
    public void executablesCompilationTest_whenLoopIsUsedInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("log", "end")
                .build();

        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();

        final List<ExecutableWithDescriptor> descriptors = ExecutablesCompiler.compileExecutables(rows, null);

        assertThat(descriptors).hasSize(3);
        assertThat(descriptors.get(0).getCalledKeywordName()).isEqualTo("log");
        assertThat(descriptors.get(0).isLoopExecutable()).isFalse();
        assertThat(descriptors.get(0).isLastExecutable()).isFalse();

        assertThat(descriptors.get(1).getCalledKeywordName()).isEqualTo(":FOR");
        assertThat(descriptors.get(1).isLoopExecutable()).isTrue();
        assertThat(descriptors.get(1).isLastExecutable()).isFalse();
        assertThat(descriptors.get(1).getLoopExecutable()).isNotNull();
        assertThat(descriptors.get(1).getLoopExecutable().getInnerExecutables()).hasSize(1);
        assertThat(descriptors.get(1).getLoopExecutable().getInnerExecutables().get(0).getCalledKeywordName())
                .isEqualTo("write");

        assertThat(descriptors.get(2).getCalledKeywordName()).isEqualTo("log");
        assertThat(descriptors.get(2).isLoopExecutable()).isFalse();
        assertThat(descriptors.get(2).isLastExecutable()).isTrue();
    }

    @Test
    public void executablesCompilationTest_whenLoopIsUsedAtTheEnd() {
        final RobotFile model = ModelBuilder.modelForFile()
            .withTestCasesTable()
                .withTestCase("test")
                    .executable("log", "10")
                    .executable(":FOR", "${x}", "IN", "1", "2", "3")
                    .executable("\\", "write", "${x}")
            .build();

        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();

        final List<ExecutableWithDescriptor> descriptors = ExecutablesCompiler.compileExecutables(rows, null);

        assertThat(descriptors).hasSize(2);
        assertThat(descriptors.get(0).getCalledKeywordName()).isEqualTo("log");
        assertThat(descriptors.get(0).isLoopExecutable()).isFalse();
        assertThat(descriptors.get(0).isLastExecutable()).isFalse();

        assertThat(descriptors.get(1).getCalledKeywordName()).isEqualTo(":FOR");
        assertThat(descriptors.get(1).isLoopExecutable()).isTrue();
        assertThat(descriptors.get(1).isLastExecutable()).isFalse();
        assertThat(descriptors.get(1).getLoopExecutable()).isNotNull();
        assertThat(descriptors.get(1).getLoopExecutable().getInnerExecutables()).hasSize(1);
        assertThat(descriptors.get(1).getLoopExecutable().getInnerExecutables().get(0).getCalledKeywordName())
                .isEqualTo("write");
    }
}
