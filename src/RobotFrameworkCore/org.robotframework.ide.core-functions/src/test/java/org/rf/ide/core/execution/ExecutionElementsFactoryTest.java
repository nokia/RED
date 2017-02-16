/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.rf.ide.core.execution.ExecutionElement.ExecutionElementType;

public class ExecutionElementsFactoryTest {

    @Test
    public void startTestExecElementProperties() {
        final ExecutionElement execElement = ExecutionElementsFactory.createStartTestExecutionElement("test");

        assertThat(execElement.getName()).isEqualTo("test");
        assertThat(execElement.getType()).isEqualTo(ExecutionElementType.TEST);
        assertThat(execElement.getSource()).isEqualTo(null);
        assertThat(execElement.getElapsedTime()).isEqualTo(-1);
        assertThat(execElement.getStatus()).isEqualTo(null);
        assertThat(execElement.getMessage()).isEqualTo(null);
    }

    @Test
    public void endTestExecElementProperties() {
        final ExecutionElement execElement = ExecutionElementsFactory.createEndTestExecutionElement("test", 10, "msg",
                Status.PASS);

        assertThat(execElement.getName()).isEqualTo("test");
        assertThat(execElement.getType()).isEqualTo(ExecutionElementType.TEST);
        assertThat(execElement.getSource()).isEqualTo(null);
        assertThat(execElement.getElapsedTime()).isEqualTo(10);
        assertThat(execElement.getStatus()).isEqualTo(Status.PASS);
        assertThat(execElement.getMessage()).isEqualTo("msg");
    }

    @Test
    public void startSuiteExecElementProperties() {
        final ExecutionElement execElement = ExecutionElementsFactory.createStartSuiteExecutionElement("suite",
                new File("file"));

        assertThat(execElement.getName()).isEqualTo("suite");
        assertThat(execElement.getType()).isEqualTo(ExecutionElementType.SUITE);
        assertThat(execElement.getSource()).isEqualTo(new File("file"));
        assertThat(execElement.getElapsedTime()).isEqualTo(-1);
        assertThat(execElement.getStatus()).isEqualTo(null);
        assertThat(execElement.getMessage()).isEqualTo(null);
    }

    @Test
    public void endSuiteExecElementProperties() {
        final ExecutionElement execElement = ExecutionElementsFactory.createEndSuiteExecutionElement("suite", 20, "msg",
                Status.PASS);

        assertThat(execElement.getName()).isEqualTo("suite");
        assertThat(execElement.getType()).isEqualTo(ExecutionElementType.SUITE);
        assertThat(execElement.getSource()).isEqualTo(null);
        assertThat(execElement.getElapsedTime()).isEqualTo(20);
        assertThat(execElement.getStatus()).isEqualTo(Status.PASS);
        assertThat(execElement.getMessage()).isEqualTo("msg");
    }

    @Test
    public void outputFileExecElementProperties() {
        final ExecutionElement execElement = ExecutionElementsFactory
                .createOutputFileExecutionElement(new File("file"));

        assertThat(execElement.getName()).isEqualTo(new File("file").getAbsolutePath());
        assertThat(execElement.getType()).isEqualTo(ExecutionElementType.OUTPUT_FILE);
        assertThat(execElement.getSource()).isEqualTo(null);
        assertThat(execElement.getElapsedTime()).isEqualTo(-1);
        assertThat(execElement.getStatus()).isEqualTo(null);
        assertThat(execElement.getMessage()).isEqualTo(null);
    }
}
