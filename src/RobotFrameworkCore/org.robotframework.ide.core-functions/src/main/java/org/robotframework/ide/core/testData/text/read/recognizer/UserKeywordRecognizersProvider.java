/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.userKeywords.KeywordArgumentsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.userKeywords.KeywordDocumentationRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.userKeywords.KeywordReturnRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.userKeywords.KeywordTagsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.userKeywords.KeywordTeardownRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.userKeywords.KeywordTimeoutRecognizer;


public class UserKeywordRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new LinkedList<>();
    static {
        recognized.add(new KeywordsTableHeaderRecognizer());
        recognized.add(new KeywordDocumentationRecognizer());
        recognized.add(new KeywordTagsRecognizer());
        recognized.add(new KeywordArgumentsRecognizer());
        recognized.add(new KeywordReturnRecognizer());
        recognized.add(new KeywordTeardownRecognizer());
        recognized.add(new KeywordTimeoutRecognizer());
    }


    public List<ATokenRecognizer> getRecognizers() {
        return recognized;
    }
}
