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
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class HashCommentMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper;

    private static final List<IHashCommentMapper> COMMENT_MAPPERS = new ArrayList<>();

    static {
        COMMENT_MAPPERS.add(new TableHeaderCommentMapper());
        COMMENT_MAPPERS.add(new SettingLibraryCommentMapper());
        COMMENT_MAPPERS.add(new SettingVariableCommentMapper());
        COMMENT_MAPPERS.add(new SettingResourceCommentMapper());
        COMMENT_MAPPERS.add(new SettingDocumentationCommentMapper());
        COMMENT_MAPPERS.add(new SettingMetadataCommentMapper());
        COMMENT_MAPPERS.add(new SettingSuiteSetupCommentMapper());
        COMMENT_MAPPERS.add(new SettingSuiteTeardownCommentMapper());
        COMMENT_MAPPERS.add(new SettingForceTagsCommentMapper());
        COMMENT_MAPPERS.add(new SettingDefaultTagsCommentMapper());
        COMMENT_MAPPERS.add(new SettingTestSetupCommentMapper());
        COMMENT_MAPPERS.add(new SettingTestTeardownCommentMapper());
        COMMENT_MAPPERS.add(new SettingTestTemplateCommentMapper());
        COMMENT_MAPPERS.add(new SettingTestTimeoutCommentMapper());
        COMMENT_MAPPERS.add(new VariablesDeclarationCommentMapper());
        COMMENT_MAPPERS.add(new TestCaseSettingDocumentationCommentMapper());
        COMMENT_MAPPERS.add(new TestCaseSettingSetupCommentMapper());
        COMMENT_MAPPERS.add(new TestCaseSettingTeardownCommentMapper());
        COMMENT_MAPPERS.add(new TestCaseSettingTagsCommentMapper());
        COMMENT_MAPPERS.add(new TestCaseSettingTemplateCommentMapper());
        COMMENT_MAPPERS.add(new TestCaseSettingTimeoutCommentMapper());
        COMMENT_MAPPERS.add(new UserKeywordSettingDocumentationCommentMapper());
        COMMENT_MAPPERS.add(new UserKeywordSettingTagsCommentMapper());
        COMMENT_MAPPERS.add(new UserKeywordSettingArgumentsCommentMapper());
        COMMENT_MAPPERS.add(new UserKeywordSettingReturnCommentMapper());
        COMMENT_MAPPERS.add(new UserKeywordSettingTeardownCommentMapper());
        COMMENT_MAPPERS.add(new UserKeywordSettingTimeoutCommentMapper());
    }

    public HashCommentMapper() {
        this.stateHelper = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        boolean addToStack = false;
        // FIXME: raw != text
        // rt.setRaw(text);
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            addToStack = true;
        } else if (RobotExecutableRow.isTsvComment(rt.getText(), robotFileOutput.getFileFormat())) {
            rt.getTypes().add(0, RobotTokenType.START_HASH_COMMENT);
            addToStack = true;
        } else {
            rt.getTypes().remove(RobotTokenType.START_HASH_COMMENT);
            rt.getTypes().add(0, RobotTokenType.COMMENT_CONTINUE);
        }

        final ParsingState commentHolder = findNearestCommentDeclaringModelElement(processingState);
        final RobotFile fileModel = robotFileOutput.getFileModel();
        final IHashCommentMapper commentMapper = findApplicableMapper(commentHolder);
        if (commentHolder != ParsingState.TRASH && commentMapper != null) {
            commentMapper.map(currentLine, rt, commentHolder, fileModel);
        }

        if (addToStack) {
            processingState.push(ParsingState.COMMENT);
        }

        return rt;
    }

    @VisibleForTesting
    public IHashCommentMapper findApplicableMapper(final ParsingState state) {
        IHashCommentMapper mapperApplicable = null;
        for (final IHashCommentMapper mapper : COMMENT_MAPPERS) {
            if (mapper.isApplicable(state)) {
                mapperApplicable = mapper;
                break;
            }
        }

        return mapperApplicable;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result = false;

        final ParsingState nearestState = stateHelper.getCurrentStatus(processingState);
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                || RobotExecutableRow.isTsvComment(rt.getText(), robotFileOutput.getFileFormat())) {
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
        return (state == ParsingState.TEST_CASE_INSIDE_ACTION || state == ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT
                || state == ParsingState.TEST_CASE_DECLARATION);
    }

    @VisibleForTesting
    protected boolean isInsideKeyword(final ParsingState state) {
        return (state == ParsingState.KEYWORD_INSIDE_ACTION || state == ParsingState.KEYWORD_INSIDE_ACTION_ARGUMENT
                || state == ParsingState.KEYWORD_DECLARATION);
    }

    @VisibleForTesting
    protected ParsingState findNearestCommentDeclaringModelElement(final Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.TRASH;

        final int capacity = processingState.size();
        for (int i = capacity - 1; i >= 0; i--) {
            final ParsingState s = processingState.get(i);
            if (ParsingState.getSettingsStates().contains(s)) {
                state = s;
                break;
            } else if (s == ParsingState.SETTING_TABLE_HEADER || s == ParsingState.VARIABLE_TABLE_HEADER
                    || s == ParsingState.TEST_CASE_TABLE_HEADER || s == ParsingState.KEYWORD_TABLE_HEADER) {
                state = s;
                break;
            } else if (s == ParsingState.VARIABLE_TABLE_INSIDE) {
                state = s;
                break;
            }
        }

        return state;
    }
}
