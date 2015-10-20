/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.testCases.TestCaseSetupRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.testCases.TestCaseTagsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.testCases.TestCaseTeardownRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.testCases.TestCaseTemplateRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.testCases.TestCaseTimeoutRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.testCases.TestDocumentationRecognizer;


public class TestCaseRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new LinkedList<>();
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
        List<ATokenRecognizer> recognizersProvided = new LinkedList<>();
        synchronized (recognized) {
            for (ATokenRecognizer rec : recognized) {
                recognizersProvided.add(rec.newInstance());
            }
        }
        return recognizersProvided;
    }
}
