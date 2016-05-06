/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsMetadataTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//metadata//new//";

    @Test
    public void test_emptyFile_and_thanCreateMetadataDeclarationOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyMetadataDeclarationOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newMetadata();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateMetadata_with_ThreeCommentTokens() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "MetadataWithThreeCommentOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
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

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_CreateMetadata_withKeyOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "MetadataWithKeyOnly.txt";
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

    @Test
    public void test_emptyFile_and_CreateMetadata_withKey_andThreeValues() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "MetadataWithKeyAndThreeValuesOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
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

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_CreateMetadata_withKey_andComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "MetadataWithKeyAndCommentsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
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

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_CreateMetadata_withKey_andThreeValuesAndComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "MetadataWithKeyAndThreeValuesCommentOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
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

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }
}
