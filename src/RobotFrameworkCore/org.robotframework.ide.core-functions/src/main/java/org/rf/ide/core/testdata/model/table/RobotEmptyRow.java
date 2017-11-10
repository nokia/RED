/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotEmptyRow<T> extends AModelElement<T> implements ICommentHolder, Serializable {

    private RobotToken empty;

    private static final long serialVersionUID = -3838003731139030172L;

    public RobotEmptyRow() {
        empty = new RobotToken();
    }

    @Override
    public void setParent(T parent) {
        super.setParent(parent);
        fixMissingType();
    }

    public boolean setEmptyToken(final RobotToken empty) {
        final boolean isEmpty = isEmpty(empty.getText());
        if (isEmpty) {
            this.empty = empty;
            fixMissingType();
        }
        return isEmpty;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.emptyList();
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
        final List<IRobotTokenType> types = empty.getTypes();
        if (types.contains(RobotTokenType.TEST_CASE_EMPTY_CELL)) {
            return ModelType.TEST_CASE_EMPTY_LINE;
        } else if (types.contains(RobotTokenType.KEYWORD_EMPTY_CELL)) {
            return ModelType.USER_KEYWORD_EMPTY_LINE;
        }
        return ModelType.UNKNOWN;
    }

    @Override
    public FilePosition getBeginPosition() {
        return empty.getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
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

    @Override
    public void insertValueAt(String value, int position) {
        // do nothing
    }

    public static boolean isEmpty(final String text) {
        return Pattern.matches("^[\\s]*$", text);
    }

    private void fixMissingType() {
        empty.setType(getRobotTokenType());
    }

    private RobotTokenType getRobotTokenType() {
        final AModelElement<?> parent = (AModelElement<?>) getParent();
        if (parent != null && parent.getModelType() == ModelType.TEST_CASE) {
            return RobotTokenType.TEST_CASE_EMPTY_CELL;
        } else if (parent != null && parent.getModelType() == ModelType.USER_KEYWORD) {
            return RobotTokenType.KEYWORD_EMPTY_CELL;
        }
        return RobotTokenType.UNKNOWN;
    }

}
