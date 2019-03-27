/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotEmptyRow<T> extends AModelElement<T> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = -3838003731139030172L;

    private RobotToken emptyToken = new RobotToken();

    private final List<RobotToken> comments = new ArrayList<>();

    public void setEmpty(final RobotToken empty) {
        empty.setType(RobotTokenType.EMPTY_CELL);
        emptyToken = empty;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        if (comments.isEmpty() && emptyToken.getText().isEmpty()) {
            emptyToken = new RobotToken();
        }
        fixComment(getComment(), rt);
        comments.add(rt);
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public void setComment(final String comment) {
        setComment(RobotToken.create(comment));
    }

    @Override
    public void setComment(final RobotToken comment) {
        comments.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(final int index) {
        comments.remove(index);
    }

    @Override
    public void clearComment() {
        comments.clear();
    }

    @Override
    public ModelType getModelType() {
        return ModelType.EMPTY_LINE;
    }

    @Override
    public FilePosition getBeginPosition() {
        if (emptyToken.getFilePosition().isNotSet() && !comments.isEmpty()) {
            return comments.get(0).getFilePosition();
        } else {
            return emptyToken.getFilePosition();
        }
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        tokens.add(emptyToken);
        tokens.addAll(comments);
        return tokens;
    }

    @Override
    public boolean removeElementToken(final int index) {
        // do nothing
        return false;
    }

    @Override
    public RobotToken getDeclaration() {
        return emptyToken;
    }

    @Override
    public void insertValueAt(final String value, final int position) {

        if (position == 0 && emptyToken.getFilePosition().isNotSet()) {
            final RobotToken tokenToInsert = RobotToken.create("\\");
            setEmpty(tokenToInsert);

        } else if (!comments.isEmpty() && position - 1 <= comments.size()) {
            final RobotToken tokenToInsert = RobotToken.create("");
            tokenToInsert.setType(RobotTokenType.COMMENT_CONTINUE);

            final int shift = emptyToken.getFilePosition().isNotSet() ? 1 : 0;
            comments.add(position - 1 + shift, tokenToInsert);
        }
    }

    public static boolean isEmpty(final String text) {
        return Pattern.matches("^[\\s]*$", text);
    }
}
