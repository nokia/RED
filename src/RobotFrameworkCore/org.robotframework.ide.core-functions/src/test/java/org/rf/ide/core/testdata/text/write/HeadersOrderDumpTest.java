/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

/**
 * @author wypych
 */
public class HeadersOrderDumpTest extends RobotFormatParameterizedTest {

    public HeadersOrderDumpTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_givenTestCase_whenAddSettings_thenSettingsShouldBeTheFirst() throws Exception {
        // prepare
        final String inFileName = convert("Input_TestCaseExists_addingSettings");
        final String outputFileName = convert("Output_TestCaseExists_addingSettings");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getLazyParser());

        // action
        modelFile.includeSettingTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenTestCase_whenAddSettings_andVariables_thenSettings_andVariablesShouldBeTheFirst()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_TestCaseExists_addingSettingsAndVariables");
        final String outputFileName = convert("Output_TestCaseExists_addingSettingsAndVariables");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getLazyParser());

        // action
        modelFile.includeVariableTableSection();
        modelFile.includeSettingTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName) {
        return "headers/update/" + fileName + "." + getExtension();
    }
}
