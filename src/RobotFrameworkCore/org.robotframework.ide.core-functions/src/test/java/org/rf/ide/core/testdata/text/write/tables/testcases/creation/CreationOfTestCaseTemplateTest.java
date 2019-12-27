/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfTestCaseTemplateTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplateDecOnly(
            final FileFormat format) throws Exception {
        test_templateDecOnly("EmptyTestCaseTemplateNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplateDecOnly(final FileFormat format)
            throws Exception {
        test_templateDecOnly("EmptyTestCaseTemplate", "TestCase", format);
    }

    private void test_templateDecOnly(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        test.newTemplate(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andCommentOnly(
            final FileFormat format) throws Exception {
        test_template_withCommentOnly("EmptyTestCaseTemplateWithCommentNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andCommentOnly(
            final FileFormat format) throws Exception {
        test_template_withCommentOnly("EmptyTestCaseTemplateWithComment", "TestCase", format);
    }

    private void test_template_withCommentOnly(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final LocalSetting<TestCase> testTemplate = test.newTemplate(0);

        testTemplate.addCommentPart("cm1");
        testTemplate.addCommentPart("cm2");
        testTemplate.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andKeywordOnly(
            final FileFormat format) throws Exception {
        test_template_withKeywordOnly("TestCaseTemplateWithKeywordNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andKeywordOnly(
            final FileFormat format) throws Exception {
        test_template_withKeywordOnly("TestCaseTemplateWithKeyword", "TestCase", format);
    }

    private void test_template_withKeywordOnly(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final LocalSetting<TestCase> testTemplate = test.newTemplate(0);

        testTemplate.addToken("execKey");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andKeyword_andCommentOnly(
            final FileFormat format) throws Exception {
        test_template_withKeyword_andComment("TestCaseTemplateWithKeywordAndCommentNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andKeyword_andCommentOnly(
            final FileFormat format) throws Exception {
        test_template_withKeyword_andComment("TestCaseTemplateWithKeywordAndComment", "TestCase", format);
    }

    private void test_template_withKeyword_andComment(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testTemplate = test.newTemplate(0);
        testTemplate.addToken("execKey");
        testTemplate.addCommentPart("cm1");
        testTemplate.addCommentPart("cm2");
        testTemplate.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andKeyword_and3ArgsUnwanted(
            final FileFormat format) throws Exception {
        test_template_withKeyword_and3ArgsUnwanted("TestCaseTemplateWithKeywordAnd3ArgsUnwantedNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andKeyword_and3ArgsUnwanted(
            final FileFormat format) throws Exception {
        test_template_withKeyword_and3ArgsUnwanted("TestCaseTemplateWithKeywordAnd3ArgsUnwanted", "TestCase", format);
    }

    private void test_template_withKeyword_and3ArgsUnwanted(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testTemplate = test.newTemplate(0);
        testTemplate.addToken("execKey");
        testTemplate.addToken("argU1");
        testTemplate.addToken("argU2");
        testTemplate.addToken("argU3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andKeyword_and3ArgsUnwanted_andComment(
            final FileFormat format) throws Exception {
        test_template_withKeyword_and3ArgsUnwanted_andComment(
                "TestCaseTemplateWithKeywordAnd3ArgsUnwantedAndCommentNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andKeyword_and3ArgsUnwanted_andComment(
            final FileFormat format) throws Exception {
        test_template_withKeyword_and3ArgsUnwanted_andComment("TestCaseTemplateWithKeywordAnd3ArgsUnwantedAndComment",
                "TestCase", format);
    }

    private void test_template_withKeyword_and3ArgsUnwanted_andComment(final String fileNameWithoutExt,
            final String userTestName, final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testTemplate = test.newTemplate(0);
        testTemplate.addToken("execKey");
        testTemplate.addToken("argU1");
        testTemplate.addToken("argU2");
        testTemplate.addToken("argU3");

        testTemplate.addCommentPart("cm1");
        testTemplate.addCommentPart("cm2");
        testTemplate.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "testCases/setting/template/new/" + fileName + "." + format.getExtension();
    }
}
