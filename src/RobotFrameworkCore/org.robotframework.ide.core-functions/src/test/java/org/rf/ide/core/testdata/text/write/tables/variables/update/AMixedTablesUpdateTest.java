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
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public abstract class AMixedTablesUpdateTest {

    public static final String PRETTY_NEW_DIR_LOCATION_NEW_UNITS = "variables//scalar//update//";

    private final String extension;

    public AMixedTablesUpdateTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_givenTestCaseAndVariableTablesWithOneVariableAndTestCase_whenAddNewScalar_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Input_TestCase_and_VariableTableExists_addingNewVariable." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_TestCase_and_VariableTableExists_addingNewVariable." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        ScalarVariable variable = new ScalarVariable("${var2}", RobotToken.create("${var2}"), VariableScope.GLOBAL);
        variable.addValue(RobotToken.create("d"));
        variableTable.addVariable(variable);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    public String getExtension() {
        return this.extension;
    }
}
