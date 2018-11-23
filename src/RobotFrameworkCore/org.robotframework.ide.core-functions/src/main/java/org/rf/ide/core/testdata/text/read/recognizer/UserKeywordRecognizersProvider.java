/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.keywords.KeywordArgumentsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.keywords.KeywordDocumentRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.keywords.KeywordDocumentationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.keywords.KeywordPostconditionRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.keywords.KeywordReturnRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.keywords.KeywordTagsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.keywords.KeywordTeardownRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.keywords.KeywordTimeoutRecognizer;


public class UserKeywordRecognizersProvider {

    private static final List<ATokenRecognizer> RECOGNIZERS = new ArrayList<>();
    static {
        RECOGNIZERS.add(new KeywordDocumentationRecognizer());
        RECOGNIZERS.add(new KeywordDocumentRecognizer());
        RECOGNIZERS.add(new KeywordTagsRecognizer());
        RECOGNIZERS.add(new KeywordArgumentsRecognizer());
        RECOGNIZERS.add(new KeywordReturnRecognizer());
        RECOGNIZERS.add(new KeywordTeardownRecognizer());
        RECOGNIZERS.add(new KeywordPostconditionRecognizer());
        RECOGNIZERS.add(new KeywordTimeoutRecognizer());
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
