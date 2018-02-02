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
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfSettingsDefaultTagsTest extends RobotFormatParameterizedTest {

    public CreationOfSettingsDefaultTagsTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateDefaultTags() throws Exception {
        // prepare
        final String fileName = convert("EmptyDefaultTagsDeclarationOnly");
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
        final String fileName = convert("DefaultTagsWithThreeTagsOnly");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final DefaultTags newDefaultTag = settingTable.newDefaultTag();
        final RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        final RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        final RobotToken tagThree = new RobotToken();
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
        final String fileName = convert("DefaultTagsWithThreeCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final DefaultTags newDefaultTag = settingTable.newDefaultTag();
        final RobotToken commentOne = new RobotToken();
        commentOne.setText("tag1");
        final RobotToken commentTwo = new RobotToken();
        commentTwo.setText("tag2");
        final RobotToken commentThree = new RobotToken();
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
        final String fileName = convert("DefaultTagsWithThreeTagsAndCommentsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final DefaultTags newDefaultTag = settingTable.newDefaultTag();
        final RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        final RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        final RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        newDefaultTag.addTag(tagOne);
        newDefaultTag.addTag(tagTwo);
        newDefaultTag.addTag(tagThree);

        final RobotToken commentOne = new RobotToken();
        commentOne.setText("com1");
        final RobotToken commentTwo = new RobotToken();
        commentTwo.setText("com2");
        final RobotToken commentThree = new RobotToken();
        commentThree.setText("com3");
        newDefaultTag.addCommentPart(commentOne);
        newDefaultTag.addCommentPart(commentTwo);
        newDefaultTag.addCommentPart(commentThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "settings/defaultTags/new/" + fileName + "." + getExtension();
    }
}
