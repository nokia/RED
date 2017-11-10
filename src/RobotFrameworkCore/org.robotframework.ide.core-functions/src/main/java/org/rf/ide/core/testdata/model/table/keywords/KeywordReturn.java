/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordReturn extends AModelElement<UserKeyword> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = -8606325717298405655L;

    private final RobotToken declaration;

    private final List<RobotToken> values = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    public KeywordReturn(final RobotToken declaration) {
        this.declaration = declaration;
        fixForTheType(declaration, RobotTokenType.KEYWORD_SETTING_RETURN);
    }

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    @Override
    public RobotToken getDeclaration() {
        return declaration;
    }

    public List<RobotToken> getReturnValues() {
        return Collections.unmodifiableList(values);
    }

    public void addReturnValue(final RobotToken returnValue) {
        fixForTheType(returnValue, RobotTokenType.KEYWORD_SETTING_RETURN_VALUE, true);
        values.add(returnValue);
    }

    public void addReturnValue(final int index, final String value) {
        updateOrCreateTokenInside(values, index, value, RobotTokenType.KEYWORD_SETTING_RETURN_VALUE);
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
    public ModelType getModelType() {
        return ModelType.USER_KEYWORD_RETURN;
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
            tokens.addAll(getReturnValues());
            tokens.addAll(getComment());
        }

        return tokens;
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

    public RobotExecutableRow<UserKeyword> asExecutableRow() {
        final RobotExecutableRow<UserKeyword> execRow = new RobotExecutableRow<>();
        execRow.setParent(getParent());
        execRow.setAction(getDeclaration().copy());
        for (final RobotToken returns : values) {
            execRow.addArgument(returns.copy());
        }
        for (final RobotToken commentPart : comment) {
            execRow.addCommentPart(commentPart.copy());
        }

        return execRow;
    }

    @Override
    public boolean removeElementToken(final int index) {
        return super.removeElementFromList(values, index);
    }

    @Override
    public void insertValueAt(String value, int position) {
        RobotToken tokenToInsert = new RobotToken();
        tokenToInsert.setText(value);
        if (position - 1 <= values.size()) { // new argument
            fixForTheType(tokenToInsert, RobotTokenType.KEYWORD_SETTING_RETURN_VALUE, true);
            values.add(position - 1, tokenToInsert);
        } else if (position - 1 - values.size() <= comment.size()) { // new comment part
            fixComment(comment, tokenToInsert);
            comment.add(position - 1 - values.size(), tokenToInsert);
        }
    }

    public KeywordReturn copy() {
        final KeywordReturn keywordReturn = new KeywordReturn(this.getDeclaration().copyWithoutPosition());
        for (final RobotToken value : getReturnValues()) {
            keywordReturn.addReturnValue(value.copyWithoutPosition());
        }
        for (final RobotToken commentToken : getComment()) {
            keywordReturn.addCommentPart(commentToken.copyWithoutPosition());
        }
        return keywordReturn;
    }
}
