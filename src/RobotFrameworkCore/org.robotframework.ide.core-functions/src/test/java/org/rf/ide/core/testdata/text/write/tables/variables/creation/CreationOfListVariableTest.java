/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.text.write.tables.variables.creation;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfListVariableTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDictionaryVariable(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptyListVariableDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        variableTable.createListVariable(0, "list", new ArrayList<>());

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDictionaryVariable_andComment(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationWithCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final AVariable aVariable = variableTable.createListVariable(0, "list", new ArrayList<>());
        aVariable.addCommentPart(RobotToken.create("cm1"));
        aVariable.addCommentPart(RobotToken.create("cm2"));
        aVariable.addCommentPart(RobotToken.create("cm3"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDictionaryVariable_andOneValue(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationAndOneValueOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");

        variableTable.createListVariable(0, "list", values);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDictionaryVariable_andOneValue_andComment(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationAndOneValueAndCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");

        final AVariable aVariable = variableTable.createListVariable(0, "list", values);
        aVariable.addCommentPart(RobotToken.create("cm1"));
        aVariable.addCommentPart(RobotToken.create("cm2"));
        aVariable.addCommentPart(RobotToken.create("cm3"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDictionaryVariable_andThreeValues(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationAnd3ValueOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");
        values.add("value2");
        values.add("value3");

        variableTable.createListVariable(0, "list", values);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDictionaryVariable_andThreeValues_andComment(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationAnd3ValueAndCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");
        values.add("value2");
        values.add("value3");

        final AVariable aVariable = variableTable.createListVariable(0, "list", values);
        aVariable.addCommentPart(RobotToken.create("cm1"));
        aVariable.addCommentPart(RobotToken.create("cm2"));
        aVariable.addCommentPart(RobotToken.create("cm3"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreationList_withThreeValues_andLastOnlyHasValue(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationWith2ValuesEmptyAndLastSet", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("");
        values.add("");
        values.add("value");

        variableTable.createListVariable(0, "list", values);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreationList_withThreeValues_andMiddleHasValue(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationWith1ValueEmptyNextSetAndLastEmpty", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("");
        values.add("value");
        values.add("");

        variableTable.createListVariable(0, "list", values);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "variables/list/new/" + fileName + "." + format.getExtension();
    }
}
