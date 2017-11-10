/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public abstract class AModelElement<T> implements IOptional, IChildElement<T> {

    private T parent;

    public abstract ModelType getModelType();

    public abstract FilePosition getBeginPosition();

    public abstract List<RobotToken> getElementTokens();

    public abstract boolean removeElementToken(final int index);

    protected boolean removeElementFromList(final List<?> l, final int index) {
        if (index >= 0 && index < l.size()) {
            l.remove(index);

            return true;
        }

        return false;
    }

    public abstract RobotToken getDeclaration();

    public void setParent(final T parent) {
        this.parent = parent;
    }

    @Override
    public T getParent() {
        return parent;
    }

    public FilePosition getEndPosition() {
        FilePosition pos = FilePosition.createNotSet();
        if (isPresent()) {
            final List<RobotToken> elementTokens = getElementTokens();

            final RobotToken lastToken = Collections.max(elementTokens, new Comparator<RobotToken>() {
                @Override
                public int compare(final RobotToken o1, final RobotToken o2) {
                    return Integer.compare(o1.getEndOffset(), o2.getEndOffset());
                }
            });
            pos = new FilePosition(lastToken.getLineNumber(), lastToken.getEndColumn(), lastToken.getEndOffset());
        }

        return pos;
    }

    public FilePosition findEndPosition(final RobotFile fileModel) {
        FilePosition pos = FilePosition.createNotSet();
        if (isPresent()) {
            for (final RobotToken tok : getElementTokens()) {
                final FilePosition tokFilePosition = tok.getFilePosition();
                if (tokFilePosition.getOffset() >= 0) {
                    if (pos.getOffset() < tokFilePosition.getOffset()) {
                        pos = tokFilePosition.copy();
                    }
                }
            }

            if (pos.getOffset() >= 0) {
                final Optional<Integer> line = fileModel.getRobotLineIndexBy(pos.getOffset());
                if (line.isPresent()) {
                    pos = fileModel.getFileContent().get(line.get()).getEndOfLine().getFilePosition().copy();
                }
            }
        }

        return pos;
    }

    protected void fixComment(final List<RobotToken> comment, final RobotToken rt) {
        if (comment.isEmpty() && !rt.getText().startsWith("#")) {
            rt.setText("#" + rt.getText());
        }

        if (!rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                && !rt.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)) {
            if (comment.isEmpty()) {
                rt.setType(RobotTokenType.START_HASH_COMMENT);
            } else {
                rt.setType(RobotTokenType.COMMENT_CONTINUE);
            }
        }
    }

    public void insertValueAt(final String value, final int position) {
        throw new UnsupportedOperationException("Operation not supported for " + this.getClass().getName() + " type");
    }

    protected void updateOrCreateTokenInside(final List<RobotToken> toModify, final int index, final String value) {
        updateOrCreateTokenInside(toModify, index, value, null);
    }

    protected void updateOrCreateTokenInside(final List<RobotToken> toModify, final int index, final String value,
            final IRobotTokenType expectedType) {
        final RobotToken token = new RobotToken();
        if (expectedType != null) {
            fixForTheType(token, expectedType);
        }
        token.setText(value);
        updateOrCreateTokenInside(toModify, index, token);
    }

    protected void updateOrCreateTokenInside(final List<RobotToken> toModify, final int index, final RobotToken token) {
        updateOrCreateTokenInside(toModify, index, token, null);
    }

    protected void updateOrCreateTokenInside(final List<RobotToken> toModify, final int index, final RobotToken token,
            final IRobotTokenType expectedType) {
        if (expectedType != null) {
            fixForTheType(token, expectedType);
        }
        final int size = toModify.size();
        if (size > index) {
            toModify.get(index).setText(token.getText());
        } else if (size == index) {
            toModify.add(token);
        } else {
            for (int i = size; i < index + 1; i++) {
                final RobotToken tempToken = new RobotToken();
                tempToken.getTypes().clear();
                if (expectedType == null) {
                    tempToken.getTypes().addAll(token.getTypes());
                } else {
                    tempToken.setType(expectedType);
                }
                toModify.add(tempToken);
            }

            toModify.set(index, token);
        }
    }

    protected RobotToken updateOrCreate(final RobotToken current, final RobotToken newValue,
            final IRobotTokenType expectedType) {
        if (newValue == null) {
            return null;
        } else if (current == null || current.getFilePosition().isNotSet()) {
            fixForTheType(newValue, expectedType, true);
            return newValue;
        } else {
            return updateOrCreate(current, newValue.getText(), expectedType);
        }
    }

    protected RobotToken updateOrCreate(final RobotToken current, final String newValue,
            final IRobotTokenType expectedType) {
        final RobotToken toReturn = current != null ? current : new RobotToken();
        toReturn.setText(newValue);

        if (expectedType != null) {
            fixForTheType(toReturn, expectedType);
        }
        return toReturn;
    }

    protected void fixForTheType(final RobotToken token, final IRobotTokenType expectedMainType) {
        final List<IRobotTokenType> types = token.getTypes();
        if (!types.contains(expectedMainType)) {
            types.add(0, expectedMainType);
        }
    }

    protected void fixForTheType(final RobotToken token, final IRobotTokenType expectedMainType,
            final boolean shouldNullCheck) {
        if (shouldNullCheck && token == null) {
            return;
        }
        fixForTheType(token, expectedMainType);
    }
}
