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
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsMetadataTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateMetadataDeclarationOnly(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptyMetadataDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newMetadata();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateMetadata_with_ThreeCommentTokens(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("MetadataWithThreeCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        meta.addCommentPart(cm1);
        meta.addCommentPart(cm2);
        meta.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_CreateMetadata_withKeyOnly(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("MetadataWithKeyOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken key = new RobotToken();
        key.setText("key");
        meta.setKey(key);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_CreateMetadata_withKey_andThreeValues(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("MetadataWithKeyAndThreeValuesOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken key = new RobotToken();
        key.setText("key");
        meta.setKey(key);

        final RobotToken val1 = new RobotToken();
        val1.setText("val1");
        final RobotToken val2 = new RobotToken();
        val2.setText("val2");
        final RobotToken val3 = new RobotToken();
        val3.setText("val3");
        meta.addValue(val1);
        meta.addValue(val2);
        meta.addValue(val3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_CreateMetadata_withKey_andComments(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("MetadataWithKeyAndCommentsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken key = new RobotToken();
        key.setText("key");
        meta.setKey(key);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        meta.addCommentPart(cm1);
        meta.addCommentPart(cm2);
        meta.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_CreateMetadata_withKey_andThreeValuesAndComments(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("MetadataWithKeyAndThreeValuesCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.newMetadata();

        final RobotToken key = new RobotToken();
        key.setText("key");
        meta.setKey(key);

        final RobotToken val1 = new RobotToken();
        val1.setText("val1");
        final RobotToken val2 = new RobotToken();
        val2.setText("val2");
        final RobotToken val3 = new RobotToken();
        val3.setText("val3");
        meta.addValue(val1);
        meta.addValue(val2);
        meta.addValue(val3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        meta.addCommentPart(cm1);
        meta.addCommentPart(cm2);
        meta.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "settings/metadata/new/" + fileName + "." + format.getExtension();
    }
}
