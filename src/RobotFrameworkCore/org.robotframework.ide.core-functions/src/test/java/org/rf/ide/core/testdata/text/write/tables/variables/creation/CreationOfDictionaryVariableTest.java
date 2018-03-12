/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.text.write.tables.variables.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfDictionaryVariableTest extends RobotFormatParameterizedTest {

    public CreationOfDictionaryVariableTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable() throws Exception {
        // prepare
        final String fileName = convert("EmptyDictionaryVariableDeclarationOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        variableTable.createDictionaryVariable(0, "dict", new ArrayList<Entry<String, String>>());

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreationDictionary_withThreeKeyValuePairs() throws Exception {
        // prepare
        final String fileName = convert("DictionaryVariableDeclarationWithThreeValuePairKeysOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<Entry<String, String>> data = new ArrayList<>();
        data.add(variableTable.createEntry("key1", "value1"));
        data.add(variableTable.createEntry("key2", "value2"));
        data.add(variableTable.createEntry("key3", "value3"));

        variableTable.createDictionaryVariable(0, "dict", data);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreationDictionary_withComment() throws Exception {
        // prepare
        final String fileName = convert("DictionaryVariableDeclarationWithCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<Entry<String, String>> data = new ArrayList<>();

        final AVariable aVariable = variableTable.createDictionaryVariable(0, "dict", data);
        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        aVariable.addCommentPart(cmTok1);
        aVariable.addCommentPart(cmTok2);
        aVariable.addCommentPart(cmTok3);
        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreationDictionary_withThreeKeyValuePair_andComment() throws Exception {
        // prepare
        final String fileName = convert("DictionaryVariableDeclarationWithCommentAndThreeKeyValuePairOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<Entry<String, String>> data = new ArrayList<>();
        data.add(variableTable.createEntry("key1", "value1"));
        data.add(variableTable.createEntry("key2", "value2"));
        data.add(variableTable.createEntry("key3", "value3"));

        final AVariable aVariable = variableTable.createDictionaryVariable(0, "dict", data);
        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        aVariable.addCommentPart(cmTok1);
        aVariable.addCommentPart(cmTok2);
        aVariable.addCommentPart(cmTok3);
        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreationDictionary_withThreeKeyValuePair_andLastOnlyHasValue() throws Exception {
        // prepare
        final String fileName = convert("DictionaryVariableDeclarationWith2ValuesEmptyAndLastSet");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<Entry<String, String>> data = new ArrayList<>();
        data.add(variableTable.createEntry("", ""));
        data.add(variableTable.createEntry("", ""));
        data.add(variableTable.createEntry("key", "value"));

        variableTable.createDictionaryVariable(0, "dict", data);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreationDictionary_withThreeKeyValuePair_andMiddleHasValue() throws Exception {
        // prepare
        final String fileName = convert("DictionaryVariableDeclarationWith1ValueEmptyNextSetAndLastEmpty");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<Entry<String, String>> data = new ArrayList<>();
        data.add(variableTable.createEntry("", ""));
        data.add(variableTable.createEntry("key", "value"));
        data.add(variableTable.createEntry("", ""));

        variableTable.createDictionaryVariable(0, "dict", data);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "variables/dictionary/new/" + fileName + "." + getExtension();
    }
}
