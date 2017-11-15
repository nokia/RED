/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestTimeout extends AModelElement<SettingTable> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = 1L;

    private final RobotToken declaration;

    private RobotToken timeout;

    private final List<RobotToken> message = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    public TestTimeout(final RobotToken declaration) {
        this.declaration = declaration;
        fixForTheType(declaration, RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION);
    }

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    @Override
    public RobotToken getDeclaration() {
        return declaration;
    }

    public RobotToken getTimeout() {
        return timeout;
    }

    public void setTimeout(final RobotToken timeout) {
        this.timeout = updateOrCreate(this.timeout, timeout, RobotTokenType.SETTING_TEST_TIMEOUT_VALUE);
    }

    public void setTimeout(final String timeout) {
        this.timeout = updateOrCreate(this.timeout, timeout, RobotTokenType.SETTING_TEST_TIMEOUT_VALUE);
    }

    public List<RobotToken> getMessageArguments() {
        return Collections.unmodifiableList(message);
    }

    public void addMessageArgument(final String messageArgument) {
        final RobotToken rt = new RobotToken();
        rt.setText(messageArgument);

        addMessageArgument(rt);
    }

    public void addMessageArgument(final RobotToken messageArgument) {
        fixForTheType(messageArgument, RobotTokenType.SETTING_TEST_TIMEOUT_MESSAGE, true);
        this.message.add(messageArgument);
    }

    public void setMessageArgument(final int index, final String argument) {
        updateOrCreateTokenInside(message, index, argument, RobotTokenType.SETTING_TEST_TIMEOUT_MESSAGE);
    }

    public void setMessageArgument(final int index, final RobotToken argument) {
        updateOrCreateTokenInside(message, index, argument, RobotTokenType.SETTING_TEST_TIMEOUT_MESSAGE);
    }

    @Override
    public List<RobotToken> getComment() {
        return comment;
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(rt);
    }

    @Override
    public void setComment(String comment) {
        final RobotToken tok = new RobotToken();
        tok.setText(comment);
        setComment(tok);
    }

    @Override
    public void setComment(RobotToken comment) {
        this.comment.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(int index) {
        this.comment.remove(index);
    }

    @Override
    public void clearComment() {
        this.comment.clear();
    }

    @Override
    public ModelType getModelType() {
        return ModelType.SUITE_TEST_TIMEOUT;
    }

    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            if (getTimeout() != null) {
                tokens.add(getTimeout());
            }
            tokens.addAll(getMessageArguments());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    @Override
    public boolean removeElementToken(int index) {
        return super.removeElementFromList(message, index);
    }

    @Override
    public void insertValueAt(String value, int position) {
        final RobotToken tokenToInsert = new RobotToken();
        tokenToInsert.setText(value);
        if (position - 2 <= message.size()) { // new argument
            fixForTheType(tokenToInsert, RobotTokenType.SETTING_TEST_TIMEOUT_VALUE, true);
            message.add(position - 2, tokenToInsert);
        } else if (position - 2 - message.size() <= comment.size()) { // new comment part
            fixComment(comment, tokenToInsert);
            comment.add(position - 2 - message.size(), tokenToInsert);
        }
    }
}
