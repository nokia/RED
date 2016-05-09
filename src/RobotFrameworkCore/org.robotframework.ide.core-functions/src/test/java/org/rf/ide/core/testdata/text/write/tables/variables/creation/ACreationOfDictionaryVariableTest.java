package org.rf.ide.core.testdata.text.write.tables.variables.creation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfDictionaryVariableTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "variables//dictionary//new//";

    private final String extension;

    public ACreationOfDictionaryVariableTest(final String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateDictionaryVariable() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyDictionaryVariableDeclarationOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        variableTable.createDictionaryVariable(0, "&{dict}", new HashMap<String, String>());

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreationDictionary_withThreeKeyValuePairs() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "DictionaryVariableDeclarationWithThreeValuePairKeysOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        Map<String, String> data = new LinkedHashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        data.put("key3", "value3");

        variableTable.createDictionaryVariable(0, "&{dict}", data);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreationDictionary_withComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "DictionaryVariableDeclarationWithCommentOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        Map<String, String> data = new LinkedHashMap<>();

        AVariable aVariable = variableTable.createDictionaryVariable(0, "&{dict}", data);
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
    public void test_emptyFile_and_thanCreationDictionary_withThreeKeyValuePair_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION
                + "DictionaryVariableDeclarationWithCommentAndThreeKeyValuePairOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final VariableTable variableTable = modelFile.getVariableTable();
        Map<String, String> data = new LinkedHashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        data.put("key3", "value3");

        AVariable aVariable = variableTable.createDictionaryVariable(0, "&{dict}", data);
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
