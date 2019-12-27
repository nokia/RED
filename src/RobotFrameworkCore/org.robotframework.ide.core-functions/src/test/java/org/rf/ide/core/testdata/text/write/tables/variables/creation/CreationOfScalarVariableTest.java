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

public class CreationOfScalarVariableTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateScalarVariable(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptyScalarVariableDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        variableTable.createScalarVariable(0, "scalar", new ArrayList<>());

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateScalarVariable_andComment(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("ScalarVariableDeclarationWithCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final AVariable aVariable = variableTable.createScalarVariable(0, "scalar", new ArrayList<>());
        aVariable.addCommentPart(RobotToken.create("cm1"));
        aVariable.addCommentPart(RobotToken.create("cm2"));
        aVariable.addCommentPart(RobotToken.create("cm3"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateScalarVariable_andOneValue(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("ScalarVariableDeclarationAndOneValueOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");

        variableTable.createScalarVariable(0, "scalar", values);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateScalarVariable_andOneValue_andComment(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ScalarVariableDeclarationAndOneValueAndCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");

        final AVariable aVariable = variableTable.createScalarVariable(0, "scalar", values);
        aVariable.addCommentPart(RobotToken.create("cm1"));
        aVariable.addCommentPart(RobotToken.create("cm2"));
        aVariable.addCommentPart(RobotToken.create("cm3"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateScalarVariable_andThreeValues(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("ScalarVariableDeclarationAnd3ValueOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");
        values.add("value2");
        values.add("value3");

        variableTable.createScalarVariable(0, "scalar", values);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateScalarVariable_andThreeValues_andComment(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ScalarVariableDeclarationAnd3ValueAndCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");
        values.add("value2");
        values.add("value3");

        final AVariable aVariable = variableTable.createScalarVariable(0, "scalar", values);
        aVariable.addCommentPart(RobotToken.create("cm1"));
        aVariable.addCommentPart(RobotToken.create("cm2"));
        aVariable.addCommentPart(RobotToken.create("cm3"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDictionaryVariable_andEmptyValue_andComment(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ScalarVariableDeclarationAndEmptyValueAndCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("");

        final AVariable aVariable = variableTable.createScalarVariable(0, "scalar", values);
        aVariable.addCommentPart(RobotToken.create("cm1"));
        aVariable.addCommentPart(RobotToken.create("cm2"));
        aVariable.addCommentPart(RobotToken.create("cm3"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "variables/scalar/new/" + fileName + "." + format.getExtension();
    }
}
