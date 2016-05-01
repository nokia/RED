/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public abstract class ATags<T> extends AModelElement<T> {

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

    public void addTag(final RobotToken tag) {
        final List<IRobotTokenType> tagTypes = tag.getTypes();
        if (!tagTypes.contains(getTagType())) {
            if (tagTypes.isEmpty()) {
                tagTypes.add(getTagType());
            } else {
                tagTypes.add(0, getTagType());
            }
        }

        tags.add(tag);
    }

    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

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

    public abstract IRobotTokenType getTagType();
}
