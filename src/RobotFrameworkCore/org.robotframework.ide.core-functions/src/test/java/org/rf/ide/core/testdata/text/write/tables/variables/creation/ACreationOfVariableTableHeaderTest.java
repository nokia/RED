/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.variables.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfVariableTableHeaderTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "variables//header//new//";

    private final String extension;

    public ACreationOfVariableTableHeaderTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateVariableHeaderOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "VariablesHeaderOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariableHeader_withTwoNamedColumns() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "VariablesHeaderWithColumns." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getVariableTable().getHeaders().get(0);
        RobotToken columnOne = new RobotToken();
        columnOne.setText("*** col1 ***");
        tableHeader.addColumnName(columnOne);
        RobotToken columnTwo = new RobotToken();
        columnTwo.setText("*** col2 ***");
        tableHeader.addColumnName(columnTwo);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariableHeader_withTwoCommentTokens() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "VariablesHeaderWithComments." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getVariableTable().getHeaders().get(0);
        RobotToken commentOne = new RobotToken();
        commentOne.setText("comment");
        tableHeader.addComment(commentOne);
        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("comment2");
        tableHeader.addComment(commentTwo);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariableHeader_withTwoNamedColumns_and_withTwoCommentTokens()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "VariablesHeaderWithColumnsAndComments." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getVariableTable().getHeaders().get(0);

        RobotToken columnOne = new RobotToken();
        columnOne.setText("*** col1 ***");
        tableHeader.addColumnName(columnOne);

        RobotToken commentOne = new RobotToken();
        commentOne.setText("comment");
        tableHeader.addComment(commentOne);

        RobotToken columnTwo = new RobotToken();
        columnTwo.setText("*** col2 ***");
        tableHeader.addColumnName(columnTwo);

        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("comment2");
        tableHeader.addComment(commentTwo);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
