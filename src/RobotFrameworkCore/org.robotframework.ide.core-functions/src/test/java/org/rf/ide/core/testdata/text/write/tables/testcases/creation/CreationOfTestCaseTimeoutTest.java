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

public class CreationOfTestCaseTimeoutTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDecOnly(
            final FileFormat format) throws Exception {
        test_timeoutDecOnly("EmptyTestCaseTimeoutNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDecOnly(final FileFormat format)
            throws Exception {
        test_timeoutDecOnly("EmptyTestCaseTimeout", "TestCase", format);
    }

    private void test_timeoutDecOnly(final String fileNameWithoutExt, final String userKeywordName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        test.newTimeout(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withComment(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withCommentOnly("EmptyTestCaseTimeoutCommentNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withComment(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withCommentOnly("EmptyTestCaseTimeoutComment", "TestCase", format);
    }

    private void test_timeoutDec_withCommentOnly(final String fileNameWithoutExt, final String userKeywordName,
            final FileFormat format)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        final LocalSetting<TestCase> testTimeout = test.newTimeout(0);

        testTimeout.addCommentPart("cm1");
        testTimeout.addCommentPart("cm2");
        testTimeout.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withValue(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withComment_andValue("EmptyTestCaseTimeoutWithValueNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withValue(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withComment_andValue("EmptyTestCaseTimeoutWithValue", "TestCase", format);
    }

    private void test_timeoutDec_withComment_andValue(final String fileNameWithoutExt, final String userKeywordName,
            final FileFormat format)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        final LocalSetting<TestCase> testTimeout = test.newTimeout(0);

        testTimeout.addToken("1 hours");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withValue_andComment(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyTestCaseTimeoutWithValueAndCommentNoTestName", "",
                format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withValue_andComment(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyTestCaseTimeoutWithValueAndComment", "TestCase", format);
    }

    private void test_timeoutDec_withComment_andValue_andComment(final String fileNameWithoutExt,
            final String userKeywordName, final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        final LocalSetting<TestCase> testTimeout = test.newTimeout(0);

        testTimeout.addToken("1 hours");
        testTimeout.addCommentPart("cm1");
        testTimeout.addCommentPart("cm2");
        testTimeout.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("TestCaseTimeoutWithValueAnd3MsgArgsNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("TestCaseTimeoutWithValueAnd3MsgArgs", "TestCase", format);
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs(final String fileNameWithoutExt,
            final String userKeywordName, final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        final LocalSetting<TestCase> testTimeout = test.newTimeout(0);

        testTimeout.addToken("1 hours");
        testTimeout.addToken("msg1");
        testTimeout.addToken("msg2");
        testTimeout.addToken("msg3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(
                "TestCaseTimeoutWithValueAnd3MsgArgsAndCommentNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment(
            final FileFormat format)
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment("TestCaseTimeoutWithValueAnd3MsgArgsAndComment",
                "TestCase", format);
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(final String fileNameWithoutExt,
            final String userKeywordName, final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        final LocalSetting<TestCase> testTimeout = test.newTimeout(0);

        testTimeout.addToken("1 hours");
        testTimeout.addToken("msg1");
        testTimeout.addToken("msg2");
        testTimeout.addToken("msg3");

        testTimeout.addCommentPart("cm1");
        testTimeout.addCommentPart("cm2");
        testTimeout.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "testCases/setting/timeout/new/" + fileName + "." + format.getExtension();
    }
}
