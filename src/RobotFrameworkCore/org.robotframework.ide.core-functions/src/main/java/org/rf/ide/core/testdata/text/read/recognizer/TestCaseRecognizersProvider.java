/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseSetupRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseTagsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseTeardownRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseTemplateRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseTimeoutRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestDocumentationRecognizer;


public class TestCaseRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new ArrayList<>();
    static {
        recognized.add(new TestCasesTableHeaderRecognizer());
        recognized.add(new TestDocumentationRecognizer());
        recognized.add(new TestCaseTagsRecognizer());
        recognized.add(new TestCaseSetupRecognizer());
        recognized.add(new TestCaseTeardownRecognizer());
        recognized.add(new TestCaseTemplateRecognizer());
        recognized.add(new TestCaseTimeoutRecognizer());
    }


    public List<ATokenRecognizer> getRecognizers(final RobotVersion robotVersion) {
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
