/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.tsv file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfSettingsDefaultTagsTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//defaultTags//new//";

    private final String extension;

    public ACreationOfSettingsDefaultTagsTest(final String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateDefaultTags() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyDefaultTagsDeclarationOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newDefaultTag();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeTags() throws Exception {
        // prepare
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");
        final String fileName = PRETTY_NEW_DIR_LOCATION + "DefaultTagsWithThreeTagsOnly." + getExtension();

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final DefaultTags newDefaultTag = settingTable.newDefaultTag();
        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        newDefaultTag.addTag(tagOne);
        newDefaultTag.addTag(tagTwo);
        newDefaultTag.addTag(tagThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "DefaultTagsWithThreeCommentOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final DefaultTags newDefaultTag = settingTable.newDefaultTag();
        RobotToken commentOne = new RobotToken();
        commentOne.setText("tag1");
        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("tag2");
        RobotToken commentThree = new RobotToken();
        commentThree.setText("tag3");
        newDefaultTag.addCommentPart(commentOne);
        newDefaultTag.addCommentPart(commentTwo);
        newDefaultTag.addCommentPart(commentThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeCommentsAndTags() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "DefaultTagsWithThreeTagsAndCommentsOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final DefaultTags newDefaultTag = settingTable.newDefaultTag();
        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        newDefaultTag.addTag(tagOne);
        newDefaultTag.addTag(tagTwo);
        newDefaultTag.addTag(tagThree);

        RobotToken commentOne = new RobotToken();
        commentOne.setText("com1");
        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("com2");
        RobotToken commentThree = new RobotToken();
        commentThree.setText("com3");
        newDefaultTag.addCommentPart(commentOne);
        newDefaultTag.addCommentPart(commentTwo);
        newDefaultTag.addCommentPart(commentThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }
}
