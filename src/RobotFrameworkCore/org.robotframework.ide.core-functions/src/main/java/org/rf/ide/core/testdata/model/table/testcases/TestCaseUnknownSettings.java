/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseUnknownSettings extends AModelElement<TestCase> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = 1287278852243033746L;

    private final RobotToken declaration;

    private final List<RobotToken> arguments = new ArrayList<>(0);

    private final List<RobotToken> comment = new ArrayList<>();

    public TestCaseUnknownSettings(final RobotToken declaration) {
        this.declaration = declaration;
        fixForTheType(declaration, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION);
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE_SETTING_UNKNOWN;
    }

    public void addArgument(final String argument) {
        addArgument(arguments.size(), argument);
    }

    public void addArgument(final int index, final String argument) {
        updateOrCreateTokenInside(arguments, index, argument, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS);
    }

    public void addArgument(final RobotToken argument) {
        fixForTheType(argument, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, true);
        this.arguments.add(argument);
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(rt);
    }

    @Override
    public void setComment(final String comment) {
        final RobotToken tok = new RobotToken();
        tok.setText(comment);
        setComment(tok);
    }

    @Override
    public void setComment(final RobotToken comment) {
        this.comment.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(final int index) {
        this.comment.remove(index);
    }

    @Override
    public void clearComment() {
        this.comment.clear();
    }

    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> elems = new LinkedList<>();
        if (isPresent()) {
            elems.add(declaration);
            elems.addAll(arguments);
            elems.addAll(comment);
        }
        return elems;
    }

    @Override
    public RobotToken getDeclaration() {
        return this.declaration;
    }

    @Override
    public boolean removeElementToken(final int index) {
        return super.removeElementFromList(arguments, index);
    }

    @Override
    public void insertValueAt(String value, int position) {
        final RobotToken tokenToInsert = new RobotToken();
        tokenToInsert.setText(value);
        if (position - 1 <= arguments.size()) { // new argument
            fixForTheType(tokenToInsert, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, true);
            arguments.add(position - 1, tokenToInsert);
        } else if (position - 1 - arguments.size() <= comment.size()) { // new comment part
            fixComment(comment, tokenToInsert);
            comment.add(position - 1 - arguments.size(), tokenToInsert);
        }
    }
}
