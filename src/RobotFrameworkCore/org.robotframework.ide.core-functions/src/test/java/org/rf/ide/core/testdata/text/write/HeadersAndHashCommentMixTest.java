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

/**
 * @author wypych
 */
public class HeadersAndHashCommentMixTest {

    private final String HEADERS_UPDATE_DIR = "headers/update/";

    @Test
    public void test_givenTestCase_whenAddSettings_thenSettingsShouldBeTheFirst_andCommentShouldNotDisapper()
            throws Exception {
        // prepare
        final String inFileName = HEADERS_UPDATE_DIR + "Input_HeadersWithHashCommentAtTheEnd.robot";
        final String outputFileName = HEADERS_UPDATE_DIR + "Output_HeadersWithHashCommentAtTheEnd.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // action
        modelFile.includeSettingTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }
}
