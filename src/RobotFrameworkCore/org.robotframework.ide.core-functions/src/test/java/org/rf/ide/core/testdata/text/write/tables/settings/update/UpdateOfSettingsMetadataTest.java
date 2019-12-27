/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.update;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class UpdateOfSettingsMetadataTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenThreeMetadatas_whenUpdateMetadataByMovingLastElementUpper_emptyLinesInTheEnd(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_ThreeMetadatasAndEmptyLinesThenMoveUpLast", format);
        final String outputFileName = convert("Output_ThreeMetadatasAndEmptyLinesThenMoveUpLast", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();

        // action
        final List<Metadata> metadatas = settingTable.getMetadatas();
        settingTable.moveUpElement(metadatas.get(metadatas.size() - 1));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_UpdateMetadataDeclarationWithKeyAndValueAndCommentAfter_updateMetadata_withKeyAndValue(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_MetadataDeclarationWithKeyAndValueAndCommentAfter", format);
        final String outputFileName = convert("Output_MetadataDeclarationWithKeyAndValueAndCommentAfter", format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_threeMetadatasDeclarationOnly_updateMetadata_withKeyAndValue(final FileFormat format)
            throws Exception {
        // prepare
        final String inFileName = convert("Input_ThreeMetadatasDeclarationToUpdateKeyAndValue", format);
        final String outputFileName = convert("Output_ThreeMetadatasDeclarationToUpdateKeyAndValue", format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_metadataDeclarationOnly_updateMetadata_withKeyAndValue(final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_MetadataDeclarationToUpdateKeyAndValue", format);
        final String outputFileName = convert("Output_MetadataDeclarationToUpdateKeyAndValue", format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_metadataDeclarationOnly_updateMetadata_withKeyAndValue_integrationTestWithSettingsAndVariables(final FileFormat format)
            throws Exception {
        // prepare
        final String inFileName = convert("Input_MetadataDeclarationEmptyWithSettingsAndVariablesSection", format);
        final String outputFileName = convert("Output_MetadataDeclarationEmptyWithSettingsAndVariablesSection", format);
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

    private String convert(final String fileName, final FileFormat format) {
        return "settings/metadata/update/" + fileName + "." + format.getExtension();
    }
}
