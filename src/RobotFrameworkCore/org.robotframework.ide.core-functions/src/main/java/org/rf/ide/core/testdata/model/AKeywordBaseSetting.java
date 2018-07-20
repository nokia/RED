/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public abstract class AKeywordBaseSetting<T> extends AModelElement<T> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = 1L;

    private final RobotToken declaration;

    private RobotToken keywordName;

    private final List<RobotToken> arguments = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    protected AKeywordBaseSetting(final RobotToken declaration) {
        this.declaration = declaration;
        fixForTheType(this.declaration, getDeclarationType());
    }

    protected abstract RobotTokenType getDeclarationType();

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    @Override
    public RobotToken getDeclaration() {
        return declaration;
    }

    public RobotToken getKeywordName() {
        return keywordName;
    }

    public void setKeywordName(final String keywordName) {
        this.keywordName = updateOrCreate(this.keywordName, keywordName, getKeywordNameType());
    }

    public void setKeywordName(final RobotToken keywordName) {
        this.keywordName = updateOrCreate(this.keywordName, keywordName, getKeywordNameType());
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void addArgument(final String argument) {
        final RobotToken rt = new RobotToken();
        rt.setText(argument);

        addArgument(rt);
    }

    public void addArgument(final RobotToken argument) {
        fixForTheType(argument, getArgumentType(), true);
        arguments.add(argument);
    }

    public void setArgument(final int index, final String argument) {
        updateOrCreateTokenInside(arguments, index, argument, getArgumentType());
    }

    public void setArgument(final int index, final RobotToken argument) {
        updateOrCreateTokenInside(arguments, index, argument, getArgumentType());
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
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
    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(rt);
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
            if (getKeywordName() != null) {
                if (!getKeywordName().isDirty() && !getKeywordName().isNotEmpty()) {
                    keywordName.setText("\\");
                    setKeywordName(keywordName);
                }
                tokens.add(getKeywordName());
            } else if (!getArguments().isEmpty()) {
                setKeywordName(RobotToken.create("\\"));
                tokens.add(getKeywordName());
            }
            tokens.addAll(getArguments());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    public RobotExecutableRow<T> asExecutableRow() {
        final RobotExecutableRow<T> execRow = new RobotExecutableRow<>();
        execRow.setParent(getParent());

        execRow.setAction(getKeywordName().copy());
        for (final RobotToken arg : getArguments()) {
            execRow.addArgument(arg.copy());
        }
        for (final RobotToken c : getComment()) {
            execRow.addCommentPart(c.copy());
        }
        return execRow;
    }

    @Override
    public boolean removeElementToken(final int index) {
        return super.removeElementFromList(arguments, index);
    }

    @Override
    public void insertValueAt(final String value, final int position) {
        final RobotToken tokenToInsert = new RobotToken();
        tokenToInsert.setText(value);
        if (position == 1) { // new keyword name
            fixForTheType(keywordName, getArgumentType());
            arguments.add(0, keywordName.copyWithoutPosition());
            setKeywordName(tokenToInsert);
        } else if (position - 2 <= arguments.size()) { // new argument
            fixForTheType(tokenToInsert, getArgumentType());
            arguments.add(position - 2, tokenToInsert);
        } else if (position - 2 - arguments.size() <= comment.size()) { // new comment part
            fixComment(comment, tokenToInsert);
            comment.add(position - 2 - arguments.size(), tokenToInsert);
        }
    }

    public abstract IRobotTokenType getKeywordNameType();

    public abstract IRobotTokenType getArgumentType();
}
