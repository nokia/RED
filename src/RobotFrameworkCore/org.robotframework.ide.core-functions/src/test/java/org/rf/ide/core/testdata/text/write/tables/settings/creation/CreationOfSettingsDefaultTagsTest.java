/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.tsv file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsDefaultTagsTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDefaultTags(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptyDefaultTagsDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newDefaultTag();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeTags(final FileFormat format) throws Exception {
        // prepare
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");
        final String fileName = convert("DefaultTagsWithThreeTagsOnly", format);

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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeComments(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("DefaultTagsWithThreeCommentOnly", format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeCommentsAndTags(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("DefaultTagsWithThreeTagsAndCommentsOnly", format);
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

    private String convert(final String fileName, final FileFormat format) {
        return "settings/defaultTags/new/" + fileName + "." + format.getExtension();
    }
}
