/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.header.CommentsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.MetadataTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.SettingsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.UserKeywordsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.VariablesTableHeaderRecognizer;


public class TableHeadersRecognizersProvider {

    private static final List<ATokenRecognizer> RECOGNIZERS = Arrays.asList(
            new SettingsTableHeaderRecognizer(),
            new MetadataTableHeaderRecognizer(),
            new TestCasesTableHeaderRecognizer(),
            new KeywordsTableHeaderRecognizer(),
            new UserKeywordsTableHeaderRecognizer(),
            new VariablesTableHeaderRecognizer(),
            new CommentsTableHeaderRecognizer());


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
