/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TaskTemplateRecognizerTest {

    @Test
    public void taskTemplateIsRecognized_whenRobotVersionIs31() throws Exception {
        final TaskTemplateRecognizer recognizer = new TaskTemplateRecognizer();

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
        final TaskTemplateRecognizer recognizer = new TaskTemplateRecognizer();

        final ATokenRecognizer newRecognizer = recognizer.newInstance();
        assertThat(newRecognizer).isInstanceOf(TaskTemplateRecognizer.class).isNotSameAs(recognizer);
    }

    @Test
    public void recognitionTest() {
        final TaskTemplateRecognizer recognizer = new TaskTemplateRecognizer();

        assertThat(recognizer.hasNext("", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("templ", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("template", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("[templ]", 1, 0)).isFalse();

        assertThat(recognizer.hasNext("[template]", 1, 0)).isTrue();
        final RobotToken recognizedToken = recognizer.next();
        assertThat(recognizedToken.getText()).isEqualTo("[template]");
        assertThat(recognizedToken.getTypes()).containsOnly(RobotTokenType.TASK_SETTING_TEMPLATE);
    }
}
