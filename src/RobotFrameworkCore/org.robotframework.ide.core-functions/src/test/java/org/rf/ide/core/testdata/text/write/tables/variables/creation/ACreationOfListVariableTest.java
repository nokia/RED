package org.rf.ide.core.testdata.text.write.tables.variables.creation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfListVariableTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "variables//list//new//";

    private final String extension;

    public ACreationOfListVariableTest(final String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyListVariableDeclarationOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        variableTable.createListVariable(0, "@{list}", new ArrayList<String>());

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "ListVariableDeclarationWithCommentOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        AVariable aVariable = variableTable.createListVariable(0, "@{list}", new ArrayList<String>());
        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
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
        final String fileName = PRETTY_NEW_DIR_LOCATION + "ListVariableDeclarationAndOneValueOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");

        variableTable.createListVariable(0, "@{list}", values);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable_andOneValue_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "ListVariableDeclarationAndOneValueAndCommentOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");

        AVariable aVariable = variableTable.createListVariable(0, "@{list}", values);
        ;
        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
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
        final String fileName = PRETTY_NEW_DIR_LOCATION + "ListVariableDeclarationAnd3ValueOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");
        values.add("value2");
        values.add("value3");

        variableTable.createListVariable(0, "@{list}", values);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable_andThreeValues_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "ListVariableDeclarationAnd3ValueAndCommentOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        final List<String> values = new ArrayList<>();
        values.add("value1");
        values.add("value2");
        values.add("value3");

        AVariable aVariable = variableTable.createListVariable(0, "@{list}", values);
        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        aVariable.addCommentPart(cmTok1);
        aVariable.addCommentPart(cmTok2);
        aVariable.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }
}
