/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.comment.SettingCommentMapper;
import org.rf.ide.core.testdata.mapping.comment.TableHeaderCommentMapper;
import org.rf.ide.core.testdata.mapping.comment.TaskSettingCommentMapper;
import org.rf.ide.core.testdata.mapping.comment.TestCaseSettingCommentMapper;
import org.rf.ide.core.testdata.mapping.comment.UserKeywordSettingCommentMapper;
import org.rf.ide.core.testdata.mapping.comment.VariablesDeclarationCommentMapper;
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

public class HashCommentMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    private static final List<IHashCommentMapper> COMMENT_MAPPERS = new ArrayList<>();

    static {
        COMMENT_MAPPERS.add(new TableHeaderCommentMapper());

        COMMENT_MAPPERS.add(SettingCommentMapper.forLibrary());
        COMMENT_MAPPERS.add(SettingCommentMapper.forVariables());
        COMMENT_MAPPERS.add(SettingCommentMapper.forResource());
        COMMENT_MAPPERS.add(SettingCommentMapper.forDocumentation());
        COMMENT_MAPPERS.add(SettingCommentMapper.forMetadata());
        COMMENT_MAPPERS.add(SettingCommentMapper.forSuiteSetup());
        COMMENT_MAPPERS.add(SettingCommentMapper.forSuiteTeardown());
        COMMENT_MAPPERS.add(SettingCommentMapper.forForceTags());
        COMMENT_MAPPERS.add(SettingCommentMapper.forDefaultTags());
        COMMENT_MAPPERS.add(SettingCommentMapper.forTestSetup());
        COMMENT_MAPPERS.add(SettingCommentMapper.forTestTeardown());
        COMMENT_MAPPERS.add(SettingCommentMapper.forTestTemplate());
        COMMENT_MAPPERS.add(SettingCommentMapper.forTestTimeout());
        COMMENT_MAPPERS.add(SettingCommentMapper.forTaskSetup());
        COMMENT_MAPPERS.add(SettingCommentMapper.forTaskTeardown());
        COMMENT_MAPPERS.add(SettingCommentMapper.forTaskTemplate());
        COMMENT_MAPPERS.add(SettingCommentMapper.forTaskTimeout());

        COMMENT_MAPPERS.add(new VariablesDeclarationCommentMapper());

        COMMENT_MAPPERS.add(TestCaseSettingCommentMapper.forDocumentation());
        COMMENT_MAPPERS.add(TestCaseSettingCommentMapper.forSetup());
        COMMENT_MAPPERS.add(TestCaseSettingCommentMapper.forTeardown());
        COMMENT_MAPPERS.add(TestCaseSettingCommentMapper.forTags());
        COMMENT_MAPPERS.add(TestCaseSettingCommentMapper.forTemplate());
        COMMENT_MAPPERS.add(TestCaseSettingCommentMapper.forTimeout());

        COMMENT_MAPPERS.add(TaskSettingCommentMapper.forDocumentation());
        COMMENT_MAPPERS.add(TaskSettingCommentMapper.forSetup());
        COMMENT_MAPPERS.add(TaskSettingCommentMapper.forTeardown());
        COMMENT_MAPPERS.add(TaskSettingCommentMapper.forTags());
        COMMENT_MAPPERS.add(TaskSettingCommentMapper.forTemplate());
        COMMENT_MAPPERS.add(TaskSettingCommentMapper.forTimeout());

        COMMENT_MAPPERS.add(UserKeywordSettingCommentMapper.forDocumentation());
        COMMENT_MAPPERS.add(UserKeywordSettingCommentMapper.forTags());
        COMMENT_MAPPERS.add(UserKeywordSettingCommentMapper.forArgument());
        COMMENT_MAPPERS.add(UserKeywordSettingCommentMapper.forReturn());
        COMMENT_MAPPERS.add(UserKeywordSettingCommentMapper.forTeardown());
        COMMENT_MAPPERS.add(UserKeywordSettingCommentMapper.forTimeout());
    }
    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState nearestState = stateHelper.getCurrentState(processingState);
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                || RobotExecutableRow.isTsvComment(rt.getText(), robotFileOutput.getFileFormat())) {

            if (isInsideTestCase(nearestState) || isInsideTask(nearestState) || isInsideKeyword(nearestState)) {
                return false;
            } else if (!processingState.isEmpty()) {
                processingState.push(ParsingState.COMMENT);
                return true;
            } else {
                return false;
            }

        } else if (!processingState.isEmpty()) {
            return processingState.peek() == ParsingState.COMMENT;

        } else {
            return false;
        }
    }

    private boolean isInsideTestCase(final ParsingState state) {
        return state == ParsingState.TEST_CASE_INSIDE_ACTION || state == ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT
                || state == ParsingState.TEST_CASE_DECLARATION;
    }

    private boolean isInsideTask(final ParsingState state) {
        return state == ParsingState.TASK_INSIDE_ACTION || state == ParsingState.TASK_INSIDE_ACTION_ARGUMENT
                || state == ParsingState.TASK_DECLARATION;
    }

    private boolean isInsideKeyword(final ParsingState state) {
        return state == ParsingState.KEYWORD_INSIDE_ACTION || state == ParsingState.KEYWORD_INSIDE_ACTION_ARGUMENT
                || state == ParsingState.KEYWORD_DECLARATION;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        boolean addToStack = false;
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

    private IHashCommentMapper findApplicableMapper(final ParsingState state) {
        for (final IHashCommentMapper mapper : COMMENT_MAPPERS) {
            if (mapper.isApplicable(state)) {
                return mapper;
            }
        }
        return null;
    }

    private ParsingState findNearestCommentDeclaringModelElement(final Stack<ParsingState> processingState) {

        for (int i = processingState.size() - 1; i >= 0; i--) {
            final ParsingState state = processingState.get(i);
            if (ParsingState.getSettingsStates().contains(state)) {
                return state;
            } else if (state == ParsingState.SETTING_TABLE_HEADER || state == ParsingState.VARIABLE_TABLE_HEADER
                    || state == ParsingState.TEST_CASE_TABLE_HEADER || state == ParsingState.TASKS_TABLE_HEADER
                    || state == ParsingState.KEYWORD_TABLE_HEADER) {
                return state;
            } else if (state == ParsingState.VARIABLE_TABLE_INSIDE) {
                return state;
            }
        }
        return ParsingState.TRASH;
    }
}
