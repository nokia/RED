/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class AUpdateOfSettingsMetadataTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//metadata//update//";

    private final String extension;

    public AUpdateOfSettingsMetadataTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_UpdateMetadataDeclarationWithKeyAndValueAndCommentAfter_updateMetadata_withKeyAndValue()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_MetadataDeclarationWithKeyAndValueAndCommentAfter."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION
                + "Output_MetadataDeclarationWithKeyAndValueAndCommentAfter." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();

        int i = 1;
        for (final Metadata meta : settingTable.getMetadatas()) {
            meta.setKey("key" + i + "updated");
            meta.setValues(0, "value" + i + "updated");

            i++;
        }

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_threeMetdatasDeclarationOnly_updateMetadata_withKeyAndValue() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_ThreeMetadatasDeclarationToUpdateKeyAndValue."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_ThreeMetadatasDeclarationToUpdateKeyAndValue."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();

        int i = 1;
        for (final Metadata meta : settingTable.getMetadatas()) {
            meta.setKey("key" + i);
            meta.setValues(0, "value" + i);

            i++;
        }

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_metdataDeclarationOnly_updateMetadata_withKeyAndValue() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_MetadataDeclarationToUpdateKeyAndValue."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_MetadataDeclarationToUpdateKeyAndValue."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.getMetadatas().get(0);
        meta.setKey("key");
        meta.setValues(0, "value");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_metdataDeclarationOnly_updateMetadata_withKeyAndValue_integrationTestWithSettingsAndVariables()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION
                + "Input_MetadataDeclarationEmptyWithSettingsAndVariablesSection." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION
                + "Output_MetadataDeclarationEmptyWithSettingsAndVariablesSection." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final Metadata meta = settingTable.getMetadatas().get(0);
        meta.setKey("key");
        meta.setValues(0, "value");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
