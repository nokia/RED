package org.rf.ide.core.testdata.text.write.tables.variables.creation;

import java.util.HashMap;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.VariableTable;
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
        variableTable.createDictionaryVariable(0, "&{dict}", new HashMap<String, String>(), null);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }
}
