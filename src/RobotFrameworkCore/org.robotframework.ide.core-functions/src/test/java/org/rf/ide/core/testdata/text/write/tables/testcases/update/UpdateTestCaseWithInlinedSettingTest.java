/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class UpdateTestCaseWithInlinedSettingTest extends RobotFormatParameterizedTest {

    public UpdateTestCaseWithInlinedSettingTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_forIssueRelatedToDump() throws Exception {
        // prepare
        final String inFileName = convert("Input_OneTestCaseWithInlinedSetting");
        final String outputFileName = convert("Output_OneTestCaseWithInlinedSetting");

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        final TestCase testCase = modelFile.getTestCaseTable().getTestCases().get(0);
        final AModelElement<TestCase> inlinedSetting = testCase.getAllElements().get(0);
        final RobotToken inlinedSettingText = inlinedSetting.getElementTokens().get(1);
        inlinedSettingText.setText("modified");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName) {
        return "testCases/setting/" + fileName + "." + getExtension();
    }
}
