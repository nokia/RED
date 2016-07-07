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
        RobotToken rt = new RobotToken();
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
        RobotToken tok = new RobotToken();
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
            tokens.addAll(getTags());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    @Override
    public boolean removeElementToken(int index) {
        return super.removeElementFromList(tags, index);
    }

    public abstract IRobotTokenType getTagType();
}
