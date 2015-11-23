/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.hash.comment.TableHeaderCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.VariablesDeclarationCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.keyword.UserKeywordSettingArgumentsCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.keyword.UserKeywordSettingDocumentationCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.keyword.UserKeywordSettingReturnCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.keyword.UserKeywordSettingTagsCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.keyword.UserKeywordSettingTeardownCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.keyword.UserKeywordSettingTimeoutCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingDefaultTagsCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingDocumentationCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingForceTagsCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingLibraryCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingMetadataCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingResourceCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingSuiteSetupCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingSuiteTeardownCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingTestSetupCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingTestTeardownCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingTestTemplateCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingTestTimeoutCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.setting.SettingVariableCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.testcase.TestCaseSettingDocumentationCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.testcase.TestCaseSettingSetupCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.testcase.TestCaseSettingTagsCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.testcase.TestCaseSettingTeardownCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.testcase.TestCaseSettingTemplateCommentMapper;
import org.rf.ide.core.testdata.mapping.hash.comment.tables.testcase.TestCaseSettingTimeoutCommentMapper;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class HashCommentMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper;

    private static final List<IHashCommentMapper> commentMappers = new ArrayList<>();
    static {
        commentMappers.add(new TableHeaderCommentMapper());
        commentMappers.add(new SettingLibraryCommentMapper());
        commentMappers.add(new SettingVariableCommentMapper());
        commentMappers.add(new SettingResourceCommentMapper());
        commentMappers.add(new SettingDocumentationCommentMapper());
        commentMappers.add(new SettingMetadataCommentMapper());
        commentMappers.add(new SettingSuiteSetupCommentMapper());
        commentMappers.add(new SettingSuiteTeardownCommentMapper());
        commentMappers.add(new SettingForceTagsCommentMapper());
        commentMappers.add(new SettingDefaultTagsCommentMapper());
        commentMappers.add(new SettingTestSetupCommentMapper());
        commentMappers.add(new SettingTestTeardownCommentMapper());
        commentMappers.add(new SettingTestTemplateCommentMapper());
        commentMappers.add(new SettingTestTimeoutCommentMapper());
        commentMappers.add(new VariablesDeclarationCommentMapper());
        commentMappers.add(new TestCaseSettingDocumentationCommentMapper());
        commentMappers.add(new TestCaseSettingSetupCommentMapper());
        commentMappers.add(new TestCaseSettingTeardownCommentMapper());
        commentMappers.add(new TestCaseSettingTagsCommentMapper());
        commentMappers.add(new TestCaseSettingTemplateCommentMapper());
        commentMappers.add(new TestCaseSettingTimeoutCommentMapper());
        commentMappers.add(new UserKeywordSettingDocumentationCommentMapper());
        commentMappers.add(new UserKeywordSettingTagsCommentMapper());
        commentMappers.add(new UserKeywordSettingArgumentsCommentMapper());
        commentMappers.add(new UserKeywordSettingReturnCommentMapper());
        commentMappers.add(new UserKeywordSettingTeardownCommentMapper());
        commentMappers.add(new UserKeywordSettingTimeoutCommentMapper());
    }


    public HashCommentMapper() {
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        boolean addToStack = false;
        rt.setRaw(text);
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            addToStack = true;
        } else {
            rt.getTypes().remove(RobotTokenType.START_HASH_COMMENT);
            rt.getTypes().add(0, RobotTokenType.COMMENT_CONTINUE);
        }

        final ParsingState commentHolder = findNearestCommentDeclaringModelElement(processingState);
        final RobotFile fileModel = robotFileOutput.getFileModel();
        final IHashCommentMapper commentMapper = findApplicableMapper(commentHolder);
        if (commentHolder != ParsingState.TRASH || commentMapper != null) {
            commentMapper.map(rt, commentHolder, fileModel);
        }

        if (addToStack) {
            processingState.push(ParsingState.COMMENT);
        }

        return rt;
    }


    @VisibleForTesting
    public IHashCommentMapper findApplicableMapper(final ParsingState state) {
        IHashCommentMapper mapperApplicable = null;
        for (final IHashCommentMapper mapper : commentMappers) {
            if (mapper.isApplicable(state)) {
                mapperApplicable = mapper;
                break;
            }
        }

        return mapperApplicable;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;

        final ParsingState nearestState = stateHelper
                .getCurrentStatus(processingState);
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            if (isInsideTestCase(nearestState) || isInsideKeyword(nearestState)) {
                result = false;
            } else if (!processingState.isEmpty()) {
                processingState.push(ParsingState.COMMENT);
                result = true;
            }
        } else if (!processingState.isEmpty()) {
            final ParsingState state = processingState.peek();
            result = (state == ParsingState.COMMENT);
        }

        return result;
    }


    @VisibleForTesting
    protected boolean isInsideTestCase(final ParsingState state) {
        return (state == ParsingState.TEST_CASE_INSIDE_ACTION
                || state == ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT || state == ParsingState.TEST_CASE_DECLARATION);
    }


    @VisibleForTesting
    protected boolean isInsideKeyword(final ParsingState state) {
        return (state == ParsingState.KEYWORD_INSIDE_ACTION
                || state == ParsingState.KEYWORD_INSIDE_ACTION_ARGUMENT || state == ParsingState.KEYWORD_DECLARATION);
    }


    @VisibleForTesting
    protected ParsingState findNearestCommentDeclaringModelElement(
            final Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.TRASH;

        final int capacity = processingState.size();
        for (int i = capacity - 1; i >= 0; i--) {
            final ParsingState s = processingState.get(i);
            if (ParsingState.getSettingsStates().contains(s)) {
                state = s;
                break;
            }
        }

        return state;
    }
}
