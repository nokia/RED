/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.exec.descs.ExecutableRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotExecutableRow<T> extends AModelElement<T> {

    private RobotToken action;

    private final List<RobotToken> arguments = new ArrayList<>();

    private final List<RobotToken> comments = new ArrayList<>();

    public RobotExecutableRow() {
        this.action = new RobotToken();
    }

    public RobotToken getAction() {
        return action;
    }

    public void setAction(final RobotToken action) {
        this.action = action;
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void addArgument(final RobotToken argument) {
        arguments.add(argument);
    }

    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comments);
    }

    public void addComment(final RobotToken commentWord) {
        this.comments.add(commentWord);
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public RobotToken getDeclaration() {
        return action;
    }

    @Override
    public ModelType getModelType() {
        ModelType type = ModelType.UNKNOWN;

        final List<IRobotTokenType> types = getAction().getTypes();
        if (types.contains(RobotTokenType.TEST_CASE_ACTION_NAME)) {
            type = ModelType.TEST_CASE_EXECUTABLE_ROW;
        } else if (types.contains(RobotTokenType.KEYWORD_ACTION_NAME)) {
            type = ModelType.USER_KEYWORD_EXECUTABLE_ROW;
        }

        return type;
    }

    @Override
    public FilePosition getBeginPosition() {
        return getAction().getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        tokens.add(getAction());
        tokens.addAll(getArguments());
        tokens.addAll(getComment());

        return tokens;
    }

    public boolean isExecutable() {
        final RobotToken action = getAction();
        return (action != null && !action.getTypes().contains(RobotTokenType.START_HASH_COMMENT))
                && isNotEmptyForContinoue();
    }

    private boolean isNotEmptyForContinoue() {
        return getElementTokens().size() > 1 || !"\\".equals(action.getRaw().toString().trim());
    }

    public IExecutableRowDescriptor<T> buildLineDescription() {
        return new ExecutableRowDescriptorBuilder().buildLineDescriptor(this);
    }
}
