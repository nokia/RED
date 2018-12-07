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
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

/**
 * @author wypych
 */
public class ListVariablesUpdateTest extends RobotFormatParameterizedTest {

    public ListVariablesUpdateTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_givenTestCaseAndVariableTablesWithOneVariable_whenAddItems_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_SingleListVar_addItems");
        final String outputFileName = convert("Output_SingleListVar_addItems");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<AVariable> variables = variableTable.getVariables();
        final ListVariable listVariable = (ListVariable) variables.get(0);
        listVariable.addItem(RobotToken.create("v1"));
        listVariable.addItem(RobotToken.create("v2"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenTestCaseAndVariableTablesWithOneVariable_whenRenaming_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_SingleListVar_rename");
        final String outputFileName = convert("Output_SingleListVar_rename");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<AVariable> variables = variableTable.getVariables();
        final ListVariable listVariable = (ListVariable) variables.get(0);
        listVariable.getDeclaration().setText("@{updated}");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName) {
        return "variables/list/update/" + fileName + "." + getExtension();
    }
}
