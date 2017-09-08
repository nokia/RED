/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static com.google.common.collect.Lists.newArrayList;

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

    private RobotToken empty;

    private static final long serialVersionUID = -3838003731139030172L;

    public RobotEmptyRow() {
        empty = new RobotToken();
    }

    public boolean setEmptyToken(final RobotToken empty) {
        final boolean isEmpty = isEmpty(empty.getText());
        if (isEmpty) {
            this.empty = empty;
        }
        return isEmpty;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(new ArrayList<>());
    }

    @Override
    public void setComment(final String comment) {
        // do nothing
    }

    @Override
    public void setComment(final RobotToken comment) {
        // do nothing
    }

    @Override
    public void addCommentPart(final RobotToken cmPart) {
        // do nothing
    }

    @Override
    public void removeCommentPart(final int index) {
        // do nothing
    }

    @Override
    public void clearComment() {
        // do nothing
    }

    @Override
    public ModelType getModelType() {
        ModelType modelType = ModelType.UNKNOWN;
        final T parent = getParent();
        if (parent != null) {
            final ModelType parentType = ((AModelElement<?>) parent).getModelType();
            if (parentType == ModelType.TEST_CASE) {
                modelType = ModelType.TEST_CASE_EMPTY_LINE;
            } else if (parentType == ModelType.USER_KEYWORD) {
                modelType = ModelType.USER_KEYWORD_EMPTY_LINE;
            }
        }
        return modelType;
    }

    @Override
    public FilePosition getBeginPosition() {
        return empty.getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        empty.setType(RobotTokenType.EMPTY_CELL);
        return newArrayList(empty);
    }

    @Override
    public boolean removeElementToken(final int index) {
        // do nothing
        return false;
    }

    @Override
    public RobotToken getDeclaration() {
        return empty;
    }

    public static boolean isEmpty(final String text) {
        return Pattern.matches("^[\\s]*$", text);
    }

}
