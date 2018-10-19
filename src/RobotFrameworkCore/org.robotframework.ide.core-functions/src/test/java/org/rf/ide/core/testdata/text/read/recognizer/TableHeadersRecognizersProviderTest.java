/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.header.CommentsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.MetadataTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.SettingsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.UserKeywordsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.VariablesTableHeaderRecognizer;

public class TableHeadersRecognizersProviderTest {

    @Test
    public void getRecognizers_providesProperVariableRecognizersForRF30() {
        final List<ATokenRecognizer> recognizers = new TableHeadersRecognizersProvider()
                .getRecognizers(new RobotVersion(3, 0));

        assertThat(recognizers).hasSize(6);
        assertThat(recognizers).hasAtLeastOneElementOfType(SettingsTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(MetadataTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(TestCasesTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(KeywordsTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(UserKeywordsTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(VariablesTableHeaderRecognizer.class);
    }

    @Test
    public void getRecognizers_providesProperVariableRecognizersForRF31() {
        final List<ATokenRecognizer> recognizers = new TableHeadersRecognizersProvider()
                .getRecognizers(new RobotVersion(3, 1));

        assertThat(recognizers).hasSize(5);
        assertThat(recognizers).hasAtLeastOneElementOfType(SettingsTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(TestCasesTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(KeywordsTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(VariablesTableHeaderRecognizer.class);
        assertThat(recognizers).hasAtLeastOneElementOfType(CommentsTableHeaderRecognizer.class);
    }

}
