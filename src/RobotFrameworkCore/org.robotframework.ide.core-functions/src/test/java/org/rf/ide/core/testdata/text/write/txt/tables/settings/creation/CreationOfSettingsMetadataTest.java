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
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public class CreationOfSettingsMetadataTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//metadata//new//";

    @Test
    public void test_emptyFile_and_thanCreateMetadataDeclarationOnly() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "EmptyMetadataDeclarationOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newMetadata();

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateMetadata_with_ThreeCommentTokens() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "MetadataWithThreeCommentOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        meta.addCommentPart(cm1);
        meta.addCommentPart(cm2);
        meta.addCommentPart(cm3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_CreateMetadata_withKeyOnly() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "MetadataWithKeyOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken key = new RobotToken();
        key.setText("key");
        meta.setKey(key);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_CreateMetadata_withKey_andThreeValues() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "MetadataWithKeyAndThreeValuesOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken key = new RobotToken();
        key.setText("key");
        meta.setKey(key);

        RobotToken val1 = new RobotToken();
        val1.setText("val1");
        RobotToken val2 = new RobotToken();
        val2.setText("val2");
        RobotToken val3 = new RobotToken();
        val3.setText("val3");
        meta.addValue(val1);
        meta.addValue(val2);
        meta.addValue(val3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_CreateMetadata_withKey_andComments() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "MetadataWithKeyAndCommentsOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken key = new RobotToken();
        key.setText("key");
        meta.setKey(key);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        meta.addCommentPart(cm1);
        meta.addCommentPart(cm2);
        meta.addCommentPart(cm3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_CreateMetadata_withKey_andThreeValuesAndComments() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "MetadataWithKeyAndThreeValuesCommentOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken key = new RobotToken();
        key.setText("key");
        meta.setKey(key);

        RobotToken val1 = new RobotToken();
        val1.setText("val1");
        RobotToken val2 = new RobotToken();
        val2.setText("val2");
        RobotToken val3 = new RobotToken();
        val3.setText("val3");
        meta.addValue(val1);
        meta.addValue(val2);
        meta.addValue(val3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        meta.addCommentPart(cm1);
        meta.addCommentPart(cm2);
        meta.addCommentPart(cm3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }
}
