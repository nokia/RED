/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class UserKeywordSettingCommentMapper implements IHashCommentMapper {

    public static UserKeywordSettingCommentMapper forDocumentation() {
        return new UserKeywordSettingCommentMapper(EnumSet.of(ParsingState.KEYWORD_SETTING_DOCUMENTATION_DECLARATION,
                ParsingState.KEYWORD_SETTING_DOCUMENTATION_TEXT), UserKeyword::getLastDocumentation);
    }

    public static UserKeywordSettingCommentMapper forTags() {
        return new UserKeywordSettingCommentMapper(
                EnumSet.of(ParsingState.KEYWORD_SETTING_TAGS, ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME),
                UserKeyword::getLastTags);
    }

    public static UserKeywordSettingCommentMapper forArgument() {
        return new UserKeywordSettingCommentMapper(EnumSet.of(ParsingState.KEYWORD_SETTING_ARGUMENTS,
                ParsingState.KEYWORD_SETTING_ARGUMENTS_ARGUMENT_VALUE), UserKeyword::getLastArgument);
    }

    public static UserKeywordSettingCommentMapper forReturn() {
        return new UserKeywordSettingCommentMapper(
                EnumSet.of(ParsingState.KEYWORD_SETTING_RETURN, ParsingState.KEYWORD_SETTING_RETURN_VALUE),
                UserKeyword::getLastReturn);
    }

    public static UserKeywordSettingCommentMapper forTeardown() {
        return new UserKeywordSettingCommentMapper(EnumSet.of(ParsingState.KEYWORD_SETTING_TEARDOWN,
                ParsingState.KEYWORD_SETTING_TEARDOWN_KEYWORD, ParsingState.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT),
                UserKeyword::getLastTeardown);
    }

    public static UserKeywordSettingCommentMapper forTimeout() {
        return new UserKeywordSettingCommentMapper(EnumSet.of(ParsingState.KEYWORD_SETTING_TIMEOUT,
                ParsingState.KEYWORD_SETTING_TIMEOUT_VALUE, ParsingState.KEYWORD_SETTING_TIMEOUT_MESSAGE_ARGUMENTS),
                UserKeyword::getLastTimeout);
    }

    private final Set<ParsingState> applicableStates;

    private final Function<UserKeyword, ICommentHolder> commentHolderSupplier;

    public UserKeywordSettingCommentMapper(final Set<ParsingState> applicableStates,
            final Function<UserKeyword, ICommentHolder> commentHolderSupplier) {
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
        final List<UserKeyword> keywords = fileModel.getKeywordTable().getKeywords();
        final UserKeyword keyword = keywords.get(keywords.size() - 1);

        commentHolderSupplier.apply(keyword).addCommentPart(rt);
    }
}
