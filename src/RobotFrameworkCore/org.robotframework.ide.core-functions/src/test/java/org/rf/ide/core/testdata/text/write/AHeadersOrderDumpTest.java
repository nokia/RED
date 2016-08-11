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
public abstract class AHeadersOrderDumpTest {

    private final String HEADERS_UPDATE_DIR = "headers/update/";

    private final String extension;

    private final FileFormat format;

    public AHeadersOrderDumpTest(final String extension, final FileFormat format) {
        this.extension = extension;
        this.format = format;
    }

    public String getExtension() {
        return extension;
    }

    public FileFormat getFormat() {
        return format;
    }

    @Test
    public void test_givenTestCase_whenAddSettings_thenSettingsShouldBeTheFirst() throws Exception {
        // prepare
        final String inFileName = HEADERS_UPDATE_DIR + "Input_TestCaseExists_addingSettings." + getExtension();
        final String outputFileName = HEADERS_UPDATE_DIR + "Output_TestCaseExists_addingSettings." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // action
        modelFile.includeSettingTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenTestCase_whenAddSettings_andVariables_thenSettings_andVariablesShouldBeTheFirst()
            throws Exception {
        // prepare
        final String inFileName = HEADERS_UPDATE_DIR + "Input_TestCaseExists_addingSettingsAndVariables."
                + getExtension();
        final String outputFileName = HEADERS_UPDATE_DIR + "Output_TestCaseExists_addingSettingsAndVariables."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // action
        modelFile.includeVariableTableSection();
        modelFile.includeSettingTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }
}
