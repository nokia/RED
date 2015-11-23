/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.userKeywords.KeywordArgumentsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.userKeywords.KeywordDocumentationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.userKeywords.KeywordReturnRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.userKeywords.KeywordTagsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.userKeywords.KeywordTeardownRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.userKeywords.KeywordTimeoutRecognizer;


public class UserKeywordRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new ArrayList<>();
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
        final List<ATokenRecognizer> recognizersProvided = new ArrayList<>();
        synchronized (recognized) {
            for (final ATokenRecognizer rec : recognized) {
                recognizersProvided.add(rec.newInstance());
            }
        }
        return recognizersProvided;
    }
}
