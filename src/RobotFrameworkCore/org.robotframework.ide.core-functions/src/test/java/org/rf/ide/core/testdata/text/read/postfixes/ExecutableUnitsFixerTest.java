/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class ExecutableUnitsFixerTest extends RobotFormatParameterizedTest {

    private final ExecutableUnitsFixer execUnitFixer = new ExecutableUnitsFixer();

    /**
     * @param extension
     * @param format
     */
    public ExecutableUnitsFixerTest(String extension, FileFormat format) {
        super(extension, format);
    }

    @Test
    public void lineContinuationIsHandled_forDoubleVariableAssignment() throws Exception {
        // prepare
        final String inFileName = convert("Input_MultipleVariableAssignmentsLineContinuation");

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        execUnitFixer.applyFix(modelFile.getKeywordTable().getKeywords().get(0));

        // verify
        assertThat(modelFile.getFileContent().get(3).getLineTokens().get(0).getTypes()).containsExactly(
                RobotTokenType.KEYWORD_ACTION_NAME, RobotTokenType.VARIABLES_SCALAR_DECLARATION,
                RobotTokenType.VARIABLE_USAGE);
    }

    private String convert(final String fileName) {
        return "testCases/exec/update/" + fileName + "." + getExtension();
    }
}
