/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfKeywordTableHeaderTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//header//new//";

    private final String extension;

    public ACreationOfKeywordTableHeaderTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeaderOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordsHeaderOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withTwoNamedColumns() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordsHeaderWithColumns." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getKeywordTable().getHeaders().get(0);
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
    public void test_emptyFile_and_thanCreateKeywordHeader_withTwoCommentTokens() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordsHeaderWithComments." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getKeywordTable().getHeaders().get(0);
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
    public void test_emptyFile_and_thanCreateKeywordHeader_withTwoNamedColumns_and_withTwoCommentTokens()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordsHeaderWithColumnsAndComments." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getKeywordTable().getHeaders().get(0);

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
