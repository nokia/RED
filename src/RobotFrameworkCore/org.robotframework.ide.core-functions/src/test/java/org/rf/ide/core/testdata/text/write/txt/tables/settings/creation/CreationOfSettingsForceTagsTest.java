/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsForceTagsTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//forceTags//new//";

    @Test
    public void test_emptyFile_and_thanCreateForceTags() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyForceTagsDeclarationOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newForceTag();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateForceTagsWithThreeTags() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "ForceTagsWithThreeTagsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final ForceTags newForceTag = settingTable.newForceTag();
        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        newForceTag.addTag(tagOne);
        newForceTag.addTag(tagTwo);
        newForceTag.addTag(tagThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateForceTagsWithThreeComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "ForceTagsWithThreeCommentOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final ForceTags newForceTag = settingTable.newForceTag();
        RobotToken commentOne = new RobotToken();
        commentOne.setText("tag1");
        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("tag2");
        RobotToken commentThree = new RobotToken();
        commentThree.setText("tag3");
        newForceTag.addCommentPart(commentOne);
        newForceTag.addCommentPart(commentTwo);
        newForceTag.addCommentPart(commentThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateForceTagsWithThreeCommentsAndTags() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "ForceTagsWithThreeTagsAndCommentsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final ForceTags newForceTag = settingTable.newForceTag();
        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        newForceTag.addTag(tagOne);
        newForceTag.addTag(tagTwo);
        newForceTag.addTag(tagThree);

        RobotToken commentOne = new RobotToken();
        commentOne.setText("com1");
        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("com2");
        RobotToken commentThree = new RobotToken();
        commentThree.setText("com3");
        newForceTag.addCommentPart(commentOne);
        newForceTag.addCommentPart(commentTwo);
        newForceTag.addCommentPart(commentThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }
}
