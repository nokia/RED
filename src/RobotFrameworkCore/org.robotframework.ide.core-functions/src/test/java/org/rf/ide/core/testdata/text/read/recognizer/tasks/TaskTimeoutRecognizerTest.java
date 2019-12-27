/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TaskTimeoutRecognizerTest {

    @Test
    public void taskTimeoutIsRecognized_whenRobotVersionIs31() throws Exception {
        final TaskTimeoutRecognizer recognizer = new TaskTimeoutRecognizer();

        assertThat(recognizer.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(recognizer.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 0))).isFalse();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 0, 9))).isFalse();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 1, 5))).isTrue();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void newInstanceReturnsFreshRecognizer() {
        final TaskTimeoutRecognizer recognizer = new TaskTimeoutRecognizer();

        final ATokenRecognizer newRecognizer = recognizer.newInstance();
        assertThat(newRecognizer).isInstanceOf(TaskTimeoutRecognizer.class).isNotSameAs(recognizer);
    }

    @Test
    public void recognitionTest() {
        final TaskTimeoutRecognizer recognizer = new TaskTimeoutRecognizer();

        assertThat(recognizer.hasNext("", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("time", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("timeout", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("[time]", 1, 0)).isFalse();

        assertThat(recognizer.hasNext("[timeout]", 1, 0)).isTrue();
        final RobotToken recognizedToken = recognizer.next();
        assertThat(recognizedToken.getText()).isEqualTo("[timeout]");
        assertThat(recognizedToken.getTypes()).containsOnly(RobotTokenType.TASK_SETTING_TIMEOUT);
    }
}
