/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseDocumentRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseDocumentationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCasePostconditionRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCasePreconditionRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseSetupRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseTagsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseTeardownRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseTemplateRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseTimeoutRecognizer;


public class TestCaseRecognizersProvider {

    private static final List<ATokenRecognizer> RECOGNIZERS = new ArrayList<>();
    static {
        RECOGNIZERS.add(new TestCaseDocumentationRecognizer());
        RECOGNIZERS.add(new TestCaseDocumentRecognizer());
        RECOGNIZERS.add(new TestCaseTagsRecognizer());
        RECOGNIZERS.add(new TestCaseSetupRecognizer());
        RECOGNIZERS.add(new TestCasePreconditionRecognizer());
        RECOGNIZERS.add(new TestCaseTeardownRecognizer());
        RECOGNIZERS.add(new TestCasePostconditionRecognizer());
        RECOGNIZERS.add(new TestCaseTemplateRecognizer());
        RECOGNIZERS.add(new TestCaseTimeoutRecognizer());
    }


    public List<ATokenRecognizer> getRecognizers(final RobotVersion robotVersion) {
        final List<ATokenRecognizer> recognizersProvided = new ArrayList<>();
        synchronized (RECOGNIZERS) {
            for (final ATokenRecognizer rec : RECOGNIZERS) {
                if (rec.isApplicableFor(robotVersion)) {
                    recognizersProvided.add(rec.newInstance());
                }
            }
        }
        return recognizersProvided;
    }
}
