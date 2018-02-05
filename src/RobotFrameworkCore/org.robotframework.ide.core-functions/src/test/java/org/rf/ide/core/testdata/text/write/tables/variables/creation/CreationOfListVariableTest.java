package org.rf.ide.core.testdata.text.write.tables.variables.creation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfListVariableTest extends RobotFormatParameterizedTest {

    public CreationOfListVariableTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable() throws Exception {
        // prepare
        final String fileName = convert("EmptyListVariableDeclarationOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        variableTable.createListVariable(0, "list", new ArrayList<String>());

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable_andComment() throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationWithCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final AVariable aVariable = variableTable.createListVariable(0, "list", new ArrayList<String>());
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
    public void test_emptyFile_and_thanCreateDictionaryVariable_andOneValue() throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationAndOneValueOnly");
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

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable_andOneValue_andComment() throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationAndOneValueAndCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");

        final AVariable aVariable = variableTable.createListVariable(0, "list", values);

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
    public void test_emptyFile_and_thanCreateDictionaryVariable_andThreeValues() throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationAnd3ValueOnly");
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

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable_andThreeValues_andComment() throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationAnd3ValueAndCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");
        values.add("value2");
        values.add("value3");

        final AVariable aVariable = variableTable.createListVariable(0, "list", values);
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
    public void test_emptyFile_and_thanCreationList_withThreeValues_andLastOnlyHasValue() throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationWith2ValuesEmptyAndLastSet");
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

    @Test
    public void test_emptyFile_and_thanCreationList_withThreeValues_andMiddleHasValue() throws Exception {
        // prepare
        final String fileName = convert("ListVariableDeclarationWith1ValueEmptyNextSetAndLastEmpty");
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

    private String convert(final String fileName) {
        return "variables/list/new/" + fileName + "." + getExtension();
    }
}
