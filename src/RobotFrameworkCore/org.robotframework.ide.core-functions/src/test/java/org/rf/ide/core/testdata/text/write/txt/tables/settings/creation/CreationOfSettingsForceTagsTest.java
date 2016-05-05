/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt.tables.settings.creation;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public class CreationOfSettingsForceTagsTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//forceTags//new//";

    @Test
    public void test_emptyFile_and_thanCreateDefaultTags() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "EmptyForceTagsDeclarationOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newForceTag();

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeTags() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "ForceTagsWithThreeTagsOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
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

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeComments() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "ForceTagsWithThreeCommentOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        fileContent.replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
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

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateDefaultTagsWithThreeCommentsAndTags() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "ForceTagsWithThreeTagsAndCommentsOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
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

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }
}
