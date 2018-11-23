/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.header.TasksTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.tasks.TaskDocumentationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.tasks.TaskSetupRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.tasks.TaskTagsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.tasks.TaskTeardownRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.tasks.TaskTemplateRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.tasks.TaskTimeoutRecognizer;


public class TaskRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new ArrayList<>();
    static {
        recognized.add(new TasksTableHeaderRecognizer());
        recognized.add(new TaskDocumentationRecognizer());
        recognized.add(new TaskTagsRecognizer());
        recognized.add(new TaskSetupRecognizer());
        recognized.add(new TaskTeardownRecognizer());
        recognized.add(new TaskTemplateRecognizer());
        recognized.add(new TaskTimeoutRecognizer());
    }


    public List<ATokenRecognizer> getRecognizers(final RobotVersion robotVersion) {
        if (robotVersion.isOlderThan(new RobotVersion(3, 1))) {
            return new ArrayList<>();
        }

        final List<ATokenRecognizer> recognizersProvided = new ArrayList<>();
        synchronized (recognized) {
            for (final ATokenRecognizer rec : recognized) {
                if (rec.isApplicableFor(robotVersion)) {
                    recognizersProvided.add(rec.newInstance());
                }
            }
        }
        return recognizersProvided;
    }
}
