/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.variables.update;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public class ScalarVariablesUpdateTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenTestCaseAndVariableTablesWithOneVariableAndTestCase_whenAddNewScalar_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_TestCase_and_VariableTableExists_addingNewVariable", format);
        final String outputFileName = convert("Output_TestCase_and_VariableTableExists_addingNewVariable", format);
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

    private String convert(final String fileName, final FileFormat format) {
        return "variables/scalar/update/" + fileName + "." + format.getExtension();
    }
}
