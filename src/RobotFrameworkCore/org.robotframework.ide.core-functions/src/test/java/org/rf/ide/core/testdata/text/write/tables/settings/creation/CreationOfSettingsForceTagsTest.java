/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsForceTagsTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateForceTags(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptyForceTagsDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newForceTag();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateForceTagsWithThreeTags(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("ForceTagsWithThreeTagsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final ForceTags newForceTag = settingTable.newForceTag();
        final RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        final RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        final RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        newForceTag.addTag(tagOne);
        newForceTag.addTag(tagTwo);
        newForceTag.addTag(tagThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateForceTagsWithThreeComments(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("ForceTagsWithThreeCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final ForceTags newForceTag = settingTable.newForceTag();
        final RobotToken commentOne = new RobotToken();
        commentOne.setText("tag1");
        final RobotToken commentTwo = new RobotToken();
        commentTwo.setText("tag2");
        final RobotToken commentThree = new RobotToken();
        commentThree.setText("tag3");
        newForceTag.addCommentPart(commentOne);
        newForceTag.addCommentPart(commentTwo);
        newForceTag.addCommentPart(commentThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateForceTagsWithThreeCommentsAndTags(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("ForceTagsWithThreeTagsAndCommentsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final ForceTags newForceTag = settingTable.newForceTag();
        final RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        final RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        final RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        newForceTag.addTag(tagOne);
        newForceTag.addTag(tagTwo);
        newForceTag.addTag(tagThree);

        final RobotToken commentOne = new RobotToken();
        commentOne.setText("com1");
        final RobotToken commentTwo = new RobotToken();
        commentTwo.setText("com2");
        final RobotToken commentThree = new RobotToken();
        commentThree.setText("com3");
        newForceTag.addCommentPart(commentOne);
        newForceTag.addCommentPart(commentTwo);
        newForceTag.addCommentPart(commentThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "settings/forceTags/new/" + fileName + "." + format.getExtension();
    }
}
