/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;

/**
 * @author wypych
 */
public class HeadersOrderDumpTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenTestCase_whenAddSettings_thenSettingsShouldBeTheFirst(final FileFormat format)
            throws Exception {
        // prepare
        final String inFileName = convert("Input_TestCaseExists_addingSettings", format);
        final String outputFileName = convert("Output_TestCaseExists_addingSettings", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // action
        modelFile.includeSettingTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenTestCase_whenAddSettings_andVariables_thenSettings_andVariablesShouldBeTheFirst(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_TestCaseExists_addingSettingsAndVariables", format);
        final String outputFileName = convert("Output_TestCaseExists_addingSettingsAndVariables", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // action
        modelFile.includeVariableTableSection();
        modelFile.includeSettingTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "headers/update/" + fileName + "." + format.getExtension();
    }
}
