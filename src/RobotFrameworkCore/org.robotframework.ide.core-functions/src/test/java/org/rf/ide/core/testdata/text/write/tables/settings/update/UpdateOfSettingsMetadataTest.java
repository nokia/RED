/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.update;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class UpdateOfSettingsMetadataTest extends RobotFormatParameterizedTest {

    public UpdateOfSettingsMetadataTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_givenThreeMetadatas_whenUpdateMetadataByMovingLastElementUpper_emptyLinesInTheEnd()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_ThreeMetadatasAndEmptyLinesThenMoveUpLast");
        final String outputFileName = convert("Output_ThreeMetadatasAndEmptyLinesThenMoveUpLast");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();

        // action
        final List<Metadata> metadatas = settingTable.getMetadatas();
        settingTable.moveUpMetadata(metadatas.get(metadatas.size() - 1));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_UpdateMetadataDeclarationWithKeyAndValueAndCommentAfter_updateMetadata_withKeyAndValue()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_MetadataDeclarationWithKeyAndValueAndCommentAfter");
        final String outputFileName = convert("Output_MetadataDeclarationWithKeyAndValueAndCommentAfter");
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
    public void test_threeMetadatasDeclarationOnly_updateMetadata_withKeyAndValue() throws Exception {
        // prepare
        final String inFileName = convert("Input_ThreeMetadatasDeclarationToUpdateKeyAndValue");
        final String outputFileName = convert("Output_ThreeMetadatasDeclarationToUpdateKeyAndValue");
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
    public void test_metadataDeclarationOnly_updateMetadata_withKeyAndValue() throws Exception {
        // prepare
        final String inFileName = convert("Input_MetadataDeclarationToUpdateKeyAndValue");
        final String outputFileName = convert("Output_MetadataDeclarationToUpdateKeyAndValue");
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
    public void test_metadataDeclarationOnly_updateMetadata_withKeyAndValue_integrationTestWithSettingsAndVariables()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_MetadataDeclarationEmptyWithSettingsAndVariablesSection");
        final String outputFileName = convert("Output_MetadataDeclarationEmptyWithSettingsAndVariablesSection");
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

    private String convert(final String fileName) {
        return "settings/metadata/update/" + fileName + "." + getExtension();
    }
}
