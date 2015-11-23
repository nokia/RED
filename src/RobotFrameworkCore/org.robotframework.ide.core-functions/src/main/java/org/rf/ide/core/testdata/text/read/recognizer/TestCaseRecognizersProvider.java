/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testCases.TestCaseSetupRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testCases.TestCaseTagsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testCases.TestCaseTeardownRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testCases.TestCaseTemplateRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testCases.TestCaseTimeoutRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testCases.TestDocumentationRecognizer;


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


    public List<ATokenRecognizer> getRecognizers() {
        final List<ATokenRecognizer> recognizersProvided = new ArrayList<>();
        synchronized (recognized) {
            for (final ATokenRecognizer rec : recognized) {
                recognizersProvided.add(rec.newInstance());
            }
        }
        return recognizersProvided;
    }
}
