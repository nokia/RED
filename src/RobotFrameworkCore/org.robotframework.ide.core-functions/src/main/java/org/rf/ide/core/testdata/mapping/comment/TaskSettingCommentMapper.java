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
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TaskSettingCommentMapper implements IHashCommentMapper {
    
    public static TaskSettingCommentMapper forDocumentation() {
        return new TaskSettingCommentMapper(EnumSet.of(ParsingState.TASK_SETTING_DOCUMENTATION_DECLARATION,
                ParsingState.TASK_SETTING_DOCUMENTATION_TEXT), Task::getLastDocumentation);
    }

    public static TaskSettingCommentMapper forSetup() {
        return new TaskSettingCommentMapper(EnumSet.of(ParsingState.TASK_SETTING_SETUP,
                ParsingState.TASK_SETTING_SETUP_KEYWORD, ParsingState.TASK_SETTING_SETUP_KEYWORD_ARGUMENT),
                Task::getLastSetup);
    }

    public static TaskSettingCommentMapper forTeardown() {
        return new TaskSettingCommentMapper(EnumSet.of(ParsingState.TASK_SETTING_TEARDOWN,
                ParsingState.TASK_SETTING_TEARDOWN_KEYWORD, ParsingState.TASK_SETTING_TEARDOWN_KEYWORD_ARGUMENT),
                Task::getLastTeardown);
    }

    public static TaskSettingCommentMapper forTags() {
        return new TaskSettingCommentMapper(
                EnumSet.of(ParsingState.TASK_SETTING_TAGS, ParsingState.TASK_SETTING_TAGS_TAG_NAME), Task::getLastTags);
    }

    public static TaskSettingCommentMapper forTemplate() {
        return new TaskSettingCommentMapper(
                EnumSet.of(ParsingState.TASK_SETTING_TASK_TEMPLATE, ParsingState.TASK_SETTING_TASK_TEMPLATE_KEYWORD,
                        ParsingState.TASK_SETTING_TASK_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS),
                Task::getLastTemplate);
    }

    public static TaskSettingCommentMapper forTimeout() {
        return new TaskSettingCommentMapper(EnumSet.of(ParsingState.TASK_SETTING_TASK_TIMEOUT,
                ParsingState.TASK_SETTING_TASK_TIMEOUT_VALUE, ParsingState.TASK_SETTING_TASK_TIMEOUT_MESSAGE_ARGUMENTS),
                Task::getLastTimeout);
    }

    private final Set<ParsingState> applicableStates;

    private final Function<Task, ICommentHolder> commentHolderSupplier;

    public TaskSettingCommentMapper(final Set<ParsingState> applicableStates,
            final Function<Task, ICommentHolder> commentHolderSupplier) {
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
        final List<Task> tasks = fileModel.getTasksTable().getTasks();
        final Task task = tasks.get(tasks.size() - 1);

        commentHolderSupplier.apply(task).addCommentPart(rt);
    }
}
