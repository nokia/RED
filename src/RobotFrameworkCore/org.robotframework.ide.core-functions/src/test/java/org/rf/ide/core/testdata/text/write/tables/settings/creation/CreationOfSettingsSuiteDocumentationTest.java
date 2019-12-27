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
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsSuiteDocumentationTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateSuiteDoc(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptySuiteDocumentationDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newSuiteDocumentation();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteDoc_andAddComments(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("SuiteDocumentationDeclarationWithCommentsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteDocumentation suiteDoc = settingTable.newSuiteDocumentation();

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteDoc.addCommentPart(cm1);
        suiteDoc.addCommentPart(cm2);
        suiteDoc.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteDoc_withText(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("SuiteDocumentationDeclarationWithTextOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteDocumentation suiteDoc = settingTable.newSuiteDocumentation();

        final RobotToken text1 = new RobotToken();
        text1.setText("text1");
        final RobotToken text2 = new RobotToken();
        text2.setText("text2");
        final RobotToken text3 = new RobotToken();
        text3.setText("text3");
        suiteDoc.addDocumentationText(text1);
        suiteDoc.addDocumentationText(text2);
        suiteDoc.addDocumentationText(text3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteDoc_withMultipleLines(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptySuiteDocumentationThreeLines", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteDocumentation suiteDoc = settingTable.newSuiteDocumentation();

        DocumentationServiceHandler.update(suiteDoc, "doc me" + "\n" + "ok" + "\n" + "ok2");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteDoc_withTextAndComment(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("SuiteDocumentationDeclarationWithTextAndCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteDocumentation suiteDoc = settingTable.newSuiteDocumentation();

        final RobotToken text1 = new RobotToken();
        text1.setText("text1");
        final RobotToken text2 = new RobotToken();
        text2.setText("text2");
        final RobotToken text3 = new RobotToken();
        text3.setText("text3");
        suiteDoc.addDocumentationText(text1);
        suiteDoc.addDocumentationText(text2);
        suiteDoc.addDocumentationText(text3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteDoc.addCommentPart(cm1);
        suiteDoc.addCommentPart(cm2);
        suiteDoc.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "settings/suiteDoc/new/" + fileName + "." + format.getExtension();
    }
}
