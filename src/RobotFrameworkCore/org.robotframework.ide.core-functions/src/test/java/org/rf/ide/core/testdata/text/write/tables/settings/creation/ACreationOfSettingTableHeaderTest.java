/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfSettingTableHeaderTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//header//new//";

    private final String extension;

    public ACreationOfSettingTableHeaderTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateSettingHeaderOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SettingsHeaderOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateSettingHeader_withTwoNamedColumns() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SettingsHeaderWithColumns." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getSettingTable().getHeaders().get(0);
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
    public void test_emptyFile_and_thanCreateSettingHeader_withTwoCommentTokens() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SettingsHeaderWithComments." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getSettingTable().getHeaders().get(0);
        RobotToken commentOne = new RobotToken();
        commentOne.setText("comment");
        tableHeader.addCommentPart(commentOne);
        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("comment2");
        tableHeader.addCommentPart(commentTwo);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateSettingHeader_withTwoNamedColumns_and_withTwoCommentTokens()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SettingsHeaderWithColumnsAndComments." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getSettingTable().getHeaders().get(0);

        RobotToken columnOne = new RobotToken();
        columnOne.setText("*** col1 ***");
        tableHeader.addColumnName(columnOne);

        RobotToken commentOne = new RobotToken();
        commentOne.setText("comment");
        tableHeader.addCommentPart(commentOne);

        RobotToken columnTwo = new RobotToken();
        columnTwo.setText("*** col2 ***");
        tableHeader.addColumnName(columnTwo);

        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("comment2");
        tableHeader.addCommentPart(commentTwo);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
