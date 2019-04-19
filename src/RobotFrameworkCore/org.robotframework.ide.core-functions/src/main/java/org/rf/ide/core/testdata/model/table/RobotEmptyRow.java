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

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotEmptyRow<T> extends CommonStep<T> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = -3838003731139030172L;

    public static boolean isEmpty(final String text) {
        return Pattern.matches("^[\\s]*$", text);
    }

    private RobotToken emptyToken = new RobotToken();

    private final List<RobotToken> comments = new ArrayList<>();

    public void setEmpty(final RobotToken empty) {
        empty.setType(RobotTokenType.EMPTY_CELL);
        emptyToken = empty;
    }

    public RobotToken getEmptyToken() {
        return emptyToken;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    public boolean isCommentOnly() {
        return emptyToken.getFilePosition().isNotSet() && emptyToken.getText().isEmpty() && !emptyToken.isDirty();
    }

    public boolean isEmptyLine() {
        return comments.isEmpty() && emptyToken.getFilePosition().isNotSet() && emptyToken.getText().isEmpty()
                && !emptyToken.isDirty();
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
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
        if (isCommentOnly() && !comments.isEmpty()) {
            return comments.get(0).getFilePosition();
        } else {
            return emptyToken.getFilePosition();
        }
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (!isCommentOnly()) {
            tokens.add(emptyToken);
        }
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
    public void createToken(final int index) {
        final int commentsIndex = isCommentOnly() ? index : index - 1;

        if (isCommentOnly() && index == 0) {
            setEmpty(RobotToken.create(""));
            emptyToken.markAsDirty();

        } else if (!isCommentOnly() && (index == 0 || index == 1)) {
            throw new IllegalArgumentException();

        } else if (1 <= commentsIndex && commentsIndex <= comments.size()) {
            comments.add(commentsIndex, RobotToken.create("", RobotTokenType.COMMENT_CONTINUE));
        }
    }

    @Override
    public void updateToken(final int index, final String newValue) {
        final int commentsIndex = isCommentOnly() ? index : index - 1;

        if (index == 0) {
            if (isCommentOnly() && newValue.trim().startsWith("#")) {
                if (comments.isEmpty()) {
                    addCommentPart(RobotToken.create(newValue));
                } else {
                    comments.get(0).setText(newValue);
                }
                fixCommentsTypes();

            } else if (isCommentOnly() && comments.size() > 1 && !comments.get(1).getText().trim().startsWith("#")) {
                throw new IllegalArgumentException();

            } else if (isCommentOnly() && !newValue.isEmpty() && !newValue.equals("\\")) {
                throw new IllegalArgumentException();

            } else if (isCommentOnly()) {
                setEmpty(RobotToken.create(newValue));
                emptyToken.markAsDirty();
                comments.remove(0);
                fixCommentsTypes();

            } else if (newValue.trim().startsWith("#")) {
                emptyToken = new RobotToken();
                comments.add(0, RobotToken.create(newValue));
                fixCommentsTypes();

            } else if (!newValue.isEmpty() && !newValue.equals("\\")) {
                throw new IllegalArgumentException();

            } else {
                emptyToken.setText(newValue);
            }

        } else if (0 <= commentsIndex && commentsIndex < comments.size()) {
            if (commentsIndex == 0 && !newValue.trim().startsWith("#") && comments.size() > 1
                    && !comments.get(1).getText().trim().startsWith("#")) {
                throw new IllegalArgumentException();

            }
            comments.get(commentsIndex).setText(newValue);
            fixCommentsTypes();

        } else if (comments.size() <= commentsIndex) {
            final int repeat = commentsIndex - comments.size() + 1;
            for (int i = 0; i < repeat; i++) {
                addCommentPart(RobotToken.create("\\"));
            }
            comments.get(commentsIndex).setText(newValue);
        }
    }

    @Override
    public void deleteToken(final int index) {
        final int commentsIndex = isCommentOnly() ? index : index - 1;

        if (index == 0 && !isCommentOnly()) {
            emptyToken = new RobotToken();

        } else if (0 <= commentsIndex && commentsIndex < comments.size()) {
            if (commentsIndex == 0 && comments.size() > 1) {
                if (isCommentOnly() && comments.get(1).getText().trim().equals("")
                        || comments.get(1).getText().trim().equals("\\") && (comments.size() == 2
                                || comments.size() > 2 && comments.get(2).getText().trim().startsWith("#"))) {
                    comments.remove(0);
                    setEmpty(comments.remove(0));
                    fixCommentsTypes();
                    return;

                } else if (!comments.get(1).getText().trim().startsWith("#")) {
                    throw new IllegalArgumentException();
                }
            }
            comments.remove(commentsIndex);
            fixCommentsTypes();
        }
    }

    private void fixCommentsTypes() {
        for (int i = 0; i < comments.size(); i++) {
            final RobotToken token = comments.get(i);
            if (i == 0) {
                token.setType(RobotTokenType.START_HASH_COMMENT);

            } else if (i > 0) {
                token.setType(RobotTokenType.COMMENT_CONTINUE);
            }
        }
    }

    @Override
    public void rewriteFrom(final CommonStep<?> other) {
        final RobotEmptyRow<?> otherRow = (RobotEmptyRow<?>) other;
        this.emptyToken = otherRow.emptyToken;
        this.comments.clear();
        this.comments.addAll(otherRow.comments);
    }
}
