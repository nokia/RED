/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.variables.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

/**
 * @author wypych
 */
public class MixedTablesUpdateTest extends RobotFormatParameterizedTest {

    public MixedTablesUpdateTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_givenTestCaseAndVariableTablesWithOneVariableAndTestCase_whenAddNewScalar_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_TestCase_and_VariableTableExists_addingNewVariable");
        final String outputFileName = convert("Output_TestCase_and_VariableTableExists_addingNewVariable");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        final ScalarVariable variable = new ScalarVariable("${var2}", RobotToken.create("${var2}"), VariableScope.GLOBAL);
        variable.addValue(RobotToken.create("d"));
        variableTable.addVariable(variable);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName) {
        return "variables/scalar/update/" + fileName + "." + getExtension();
    }
}
