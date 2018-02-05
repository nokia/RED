/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfTestCaseTemplateTest extends RobotFormatParameterizedTest {

    public CreationOfTestCaseTemplateTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplateDecOnly() throws Exception {
        test_templateDecOnly("EmptyTestCaseTemplateNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplateDecOnly() throws Exception {
        test_templateDecOnly("EmptyTestCaseTemplate", "TestCase");
    }

    private void test_templateDecOnly(final String fileNameWithoutExt, final String userTestName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
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

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andCommentOnly()
            throws Exception {
        test_template_withCommentOnly("EmptyTestCaseTemplateWithCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andCommentOnly()
            throws Exception {
        test_template_withCommentOnly("EmptyTestCaseTemplateWithComment", "TestCase");
    }

    private void test_template_withCommentOnly(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTemplate testTemplate = test.newTemplate(0);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTemplate.addCommentPart(cmTok1);
        testTemplate.addCommentPart(cmTok2);
        testTemplate.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andKeywordOnly()
            throws Exception {
        test_template_withKeywordOnly("TestCaseTemplateWithKeywordNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andKeywordOnly()
            throws Exception {
        test_template_withKeywordOnly("TestCaseTemplateWithKeyword", "TestCase");
    }

    private void test_template_withKeywordOnly(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTemplate testTemplate = test.newTemplate(0);

        final RobotToken keyword = new RobotToken();
        keyword.setText("execKey");

        testTemplate.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andKeyword_andCommentOnly()
            throws Exception {
        test_template_withKeyword_andComment("TestCaseTemplateWithKeywordAndCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andKeyword_andCommentOnly()
            throws Exception {
        test_template_withKeyword_andComment("TestCaseTemplateWithKeywordAndComment", "TestCase");
    }

    private void test_template_withKeyword_andComment(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTemplate testTemplate = test.newTemplate(0);

        final RobotToken keyword = new RobotToken();
        keyword.setText("execKey");

        testTemplate.setKeywordName(keyword);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTemplate.addCommentPart(cmTok1);
        testTemplate.addCommentPart(cmTok2);
        testTemplate.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andKeyword_and3ArgsUnwanted()
            throws Exception {
        test_template_withKeyword_and3ArgsUnwanted("TestCaseTemplateWithKeywordAnd3ArgsUnwantedNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andKeyword_and3ArgsUnwanted()
            throws Exception {
        test_template_withKeyword_and3ArgsUnwanted("TestCaseTemplateWithKeywordAnd3ArgsUnwanted", "TestCase");
    }

    private void test_template_withKeyword_and3ArgsUnwanted(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTemplate testTemplate = test.newTemplate(0);

        final RobotToken keyword = new RobotToken();
        keyword.setText("execKey");

        testTemplate.setKeywordName(keyword);

        final RobotToken argU1 = new RobotToken();
        argU1.setText("argU1");
        final RobotToken argU2 = new RobotToken();
        argU2.setText("argU2");
        final RobotToken argU3 = new RobotToken();
        argU3.setText("argU3");

        testTemplate.addUnexpectedTrashArgument(argU1);
        testTemplate.addUnexpectedTrashArgument(argU2);
        testTemplate.addUnexpectedTrashArgument(argU3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withoutTestName_andTemplate_andKeyword_and3ArgsUnwanted_andComment()
            throws Exception {
        test_template_withKeyword_and3ArgsUnwanted_andComment(
                "TestCaseTemplateWithKeywordAnd3ArgsUnwantedAndCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTemplate_withTestName_andTemplate_andKeyword_and3ArgsUnwanted_andComment()
            throws Exception {
        test_template_withKeyword_and3ArgsUnwanted_andComment("TestCaseTemplateWithKeywordAnd3ArgsUnwantedAndComment",
                "TestCase");
    }

    private void test_template_withKeyword_and3ArgsUnwanted_andComment(final String fileNameWithoutExt,
            final String userTestName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTemplate testTemplate = test.newTemplate(0);

        final RobotToken keyword = new RobotToken();
        keyword.setText("execKey");

        testTemplate.setKeywordName(keyword);

        final RobotToken argU1 = new RobotToken();
        argU1.setText("argU1");
        final RobotToken argU2 = new RobotToken();
        argU2.setText("argU2");
        final RobotToken argU3 = new RobotToken();
        argU3.setText("argU3");

        testTemplate.addUnexpectedTrashArgument(argU1);
        testTemplate.addUnexpectedTrashArgument(argU2);
        testTemplate.addUnexpectedTrashArgument(argU3);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTemplate.addCommentPart(cmTok1);
        testTemplate.addCommentPart(cmTok2);
        testTemplate.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "testCases/setting/template/new/" + fileName + "." + getExtension();
    }
}
