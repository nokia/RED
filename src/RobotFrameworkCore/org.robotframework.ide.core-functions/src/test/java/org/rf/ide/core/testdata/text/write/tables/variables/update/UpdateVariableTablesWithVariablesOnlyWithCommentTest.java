/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.variables.update;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public class UpdateVariableTablesWithVariablesOnlyWithCommentTest {

    public static final String PRETTY_NEW_DIR_LOCATION_NEW_UNITS = "variables//hashCommentCases//";

    @Test
    public void test_givenVariableTableWithHashCommentVariable_whenAddNewVariable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Input_HashInLastLine_addNewVariable.robot";
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Output_HashInLastLine_addNewVariable.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        variableTable.addVariable(
                new ScalarVariable("${var_new}", RobotToken.create("${var_new}"), VariableScope.TEST_SUITE));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenVariableTableWithHashCommentVariables_whenAddNewVariable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Input_HashLinesBetweenLastLineIsVariable.robot";
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_HashLinesBetweenLastLineIsVariable.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        variableTable.addVariable(
                new ScalarVariable("${var_new}", RobotToken.create("${var_new}"), VariableScope.TEST_SUITE));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenVariableTableWithHashCommentVariable_whenModifySecondVariable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Input_HashInLastLine_modifyValueOfVariable.robot";
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_HashInLastLine_modifyValueOfVariable.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        final ScalarVariable var2 = (ScalarVariable) variableTable.getVariables().get(1);
        var2.getValues().get(0).setText("text with space2");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenVariableTableWithHashCommentAtTheBeginning_whenModifyScalarCorrectVariables_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Input_HashIsTheFirstLineAfterVariableTable.robot";
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_HashIsTheFirstLineAfterVariableTable.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<ScalarVariable> varsC = findVariables(variableTable, "c");
        final ScalarVariable scalarC = varsC.get(0);
        scalarC.getValues().get(0).setText("a2");

        final List<ScalarVariable> varsD = findVariables(variableTable, "d");
        final ScalarVariable scalarD = varsD.get(0);
        scalarD.getValues().get(0).setText("h2");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @SuppressWarnings("unchecked")
    private <T extends AVariable> List<T> findVariables(final VariableTable variableTable, final String variableName) {
        return variableTable.getVariables()
                .stream()
                .filter(v -> variableName.equals(v.getName()))
                .map(v -> (T) v)
                .collect(Collectors.toList());
    }
}
