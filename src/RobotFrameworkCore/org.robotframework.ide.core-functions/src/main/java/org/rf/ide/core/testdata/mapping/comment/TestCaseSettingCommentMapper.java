/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.comment;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCaseSettingCommentMapper implements IHashCommentMapper {

    public static TestCaseSettingCommentMapper forDocumentation() {
        return new TestCaseSettingCommentMapper(EnumSet.of(ParsingState.TEST_CASE_SETTING_DOCUMENTATION_DECLARATION,
                ParsingState.TEST_CASE_SETTING_DOCUMENTATION_TEXT), TestCase::getLastDocumentation);
    }

    public static TestCaseSettingCommentMapper forSetup() {
        return new TestCaseSettingCommentMapper(EnumSet.of(ParsingState.TEST_CASE_SETTING_SETUP,
                ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD, ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT),
                TestCase::getLastSetup);
    }

    public static TestCaseSettingCommentMapper forTeardown() {
        return new TestCaseSettingCommentMapper(
                EnumSet.of(ParsingState.TEST_CASE_SETTING_TEARDOWN, ParsingState.TEST_CASE_SETTING_TEARDOWN_KEYWORD,
                        ParsingState.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT),
                TestCase::getLastTeardown);
    }

    public static TestCaseSettingCommentMapper forTags() {
        return new TestCaseSettingCommentMapper(
                EnumSet.of(ParsingState.TEST_CASE_SETTING_TAGS, ParsingState.TEST_CASE_SETTING_TAGS_TAG_NAME),
                TestCase::getLastTags);
    }

    public static TestCaseSettingCommentMapper forTemplate() {
        return new TestCaseSettingCommentMapper(
                EnumSet.of(ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE,
                        ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD,
                        ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS),
                TestCase::getLastTemplate);
    }

    public static TestCaseSettingCommentMapper forTimeout() {
        return new TestCaseSettingCommentMapper(EnumSet.of(ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT,
                ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_VALUE,
                ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS),
                TestCase::getLastTimeout);
    }

    private final Set<ParsingState> applicableStates;

    private final Function<TestCase, ICommentHolder> commentHolderSupplier;

    public TestCaseSettingCommentMapper(final Set<ParsingState> applicableStates,
            final Function<TestCase, ICommentHolder> commentHolderSupplier) {
        this.applicableStates = applicableStates;
        this.commentHolderSupplier = commentHolderSupplier;
    }

    @Override
    public boolean isApplicable(final ParsingState state) {
        return applicableStates.contains(state);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        final List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);

        commentHolderSupplier.apply(testCase).addCommentPart(rt);
    }
}
