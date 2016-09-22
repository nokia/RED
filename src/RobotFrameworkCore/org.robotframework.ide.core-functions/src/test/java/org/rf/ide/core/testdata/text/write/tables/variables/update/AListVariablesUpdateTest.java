/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.variables.update;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public abstract class AListVariablesUpdateTest {

    public static final String PRETTY_NEW_DIR_LOCATION_NEW_UNITS = "variables//list//update//";

    private final String extension;

    public AListVariablesUpdateTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_givenTestCaseAndSettingsAndVariableTablesWithOneScalarVariableAndOneCommentAsList_whenAddCorrectNameForList_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Input_LastVariableIsCommentOnlyListBeforeOnlyOneVariable_updateNameToBeCorrect." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_LastVariableIsCommentOnlyListBeforeOnlyOneVariable_updateNameToBeCorrect." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        List<AVariable> variables = variableTable.getVariables();
        ((ListVariable) variables.get(1)).setName("list");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenTestCaseAndSettingsAndVariableTablesWithOneScalarVariableAndOneCommentAsList_whenAddCorrectNameForList_andAddValueForFirstScalar_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Input_LastVariableIsCommentOnlyListBeforeOnlyOneVariable_updateNameToBeCorrect_andPutValueToScalar."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_LastVariableIsCommentOnlyListBeforeOnlyOneVariable_updateNameToBeCorrect_andPutValueToScalar."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        List<AVariable> variables = variableTable.getVariables();
        ((ScalarVariable) variables.get(0)).addValue(RobotToken.create("ok"));
        ((ListVariable) variables.get(1)).setName("list");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    public String getExtension() {
        return this.extension;
    }
}
