/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestTimeout extends AModelElement<SettingTable> {

    private final RobotToken declaration;

    private RobotToken timeout;

    private final List<RobotToken> message = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    public TestTimeout(final RobotToken declaration) {
        this.declaration = declaration;
    }

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    public RobotToken getDeclaration() {
        return declaration;
    }

    public RobotToken getTimeout() {
        return timeout;
    }

    public void setTimeout(final RobotToken timeout) {
        fixForTheType(timeout, RobotTokenType.SETTING_TEST_TIMEOUT_VALUE, true);
        this.timeout = timeout;
    }

    public List<RobotToken> getMessageArguments() {
        return Collections.unmodifiableList(message);
    }

    public void addMessageArgument(final RobotToken messageArgument) {
        fixForTheType(messageArgument, RobotTokenType.SETTING_TEST_TIMEOUT_MESSAGE, true);
        this.message.add(messageArgument);
    }

    public List<RobotToken> getComment() {
        return comment;
    }

    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(rt);
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
}
