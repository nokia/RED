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

import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public abstract class ATags<T> extends AModelElement<T> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = 1L;

    private final RobotToken declaration;

    private final List<RobotToken> tags = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    protected ATags(final RobotToken declaration) {
        this.declaration = declaration;
        fixForTheType(declaration, getDeclarationTagType());
    }

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    @Override
    public RobotToken getDeclaration() {
        return declaration;
    }

    public List<RobotToken> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public void addTag(final String tag) {
        final RobotToken rt = new RobotToken();
        rt.setText(tag);

        addTag(rt);
    }

    public void addTag(final RobotToken tag) {
        fixForTheType(tag, getTagType());

        tags.add(tag);
    }

    public void setTag(final int index, final String tag) {
        updateOrCreateTokenInside(tags, index, tag, getTagType());
    }

    public void setTag(final int index, final RobotToken tag) {
        updateOrCreateTokenInside(tags, index, tag, getTagType());
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
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
    public void addCommentPart(final RobotToken rt) {
        this.fixComment(comment, rt);
        this.comment.add(rt);
    }

    @Override
    public FilePosition getBeginPosition() {
        return declaration.getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            tokens.addAll(compact(tags));
            tokens.addAll(getComment());
        }

        return tokens;
    }

    private List<RobotToken> compact(final List<RobotToken> elementsSingleType) {
        final int size = elementsSingleType.size();
        for (int i = size - 1; i >= 0; i--) {
            if (elementsSingleType.size() == 0) {
                break;
            }

            final RobotToken t = elementsSingleType.get(i);
            if (t.getText() == null || t.getText().isEmpty()) {
                elementsSingleType.remove(i);
            } else {
                break;
            }
        }

        return elementsSingleType;
    }

    @Override
    public boolean removeElementToken(int index) {
        return super.removeElementFromList(tags, index);
    }

    @Override
    public void insertValueAt(String value, int position) {
        RobotToken tokenToInsert = new RobotToken();
        tokenToInsert.setText(value);
        if (position - 1 <= tags.size()) { // new argument
            fixForTheType(tokenToInsert, getTagType());
            tags.add(position - 1, tokenToInsert);
        } else if (position - 1 - tags.size() <= comment.size()) { // new comment part
            fixComment(comment, tokenToInsert);
            comment.add(position - 1 - tags.size(), tokenToInsert);
        }
    }

    public abstract IRobotTokenType getTagType();

    public abstract IRobotTokenType getDeclarationTagType();
}
